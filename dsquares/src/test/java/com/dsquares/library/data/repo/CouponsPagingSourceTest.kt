package com.dsquares.library.data.repo

import android.util.Log
import androidx.paging.PagingSource
import com.dsquares.library.constants.TAG
import com.dsquares.library.data.network.IRemoteSource
import com.dsquares.library.data.network.interceptor.NoConnectivityException
import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.items.Denomination
import com.dsquares.library.data.network.model.items.Item
import com.dsquares.library.data.network.model.items.ItemResult
import com.dsquares.library.domain.DomainException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CouponsPagingSourceTest {

    private val remoteSource = mockk<IRemoteSource>()


    @Before
    fun setup(){
        mockkStatic(Log::class)
    }

    @After
    fun teardown(){
        unmockkStatic(Log::class)
    }

    private fun createPagingSource(
        name: String = "",
        categoryCode: String? = "",
        rewardTypes: List<String>? = emptyList()
    ) = CouponsPagingSource(
        remoteSource = remoteSource,
        name = name,
        categoryCode = categoryCode,
        rewardTypes = rewardTypes
    )

    private fun refreshParams(key: Int? = null, loadSize: Int = 20) =
        PagingSource.LoadParams.Refresh(key = key, loadSize = loadSize, placeholdersEnabled = false)

    private fun createDenomination(
        points: Int? = 100,
        discount: String? = null,
        categories: List<String>? = emptyList()
    ) = Denomination(
        brand = null, categories = categories, code = null,
        denominationType = null, description = null, discount = discount,
        from = null, imageUrl = null, inStock = null, name = null,
        points = points, redemptionChannel = null, redemptionFactor = null,
        termsAndConditions = null, to = null, usageInstructions = null, value = null
    )

    private fun createItem(
        code: String? = "C1",
        name: String? = "Item1",
        imageUrl: String? = null,
        locked: Boolean? = false,
        rewardType: String? = "REWARD",
        denominations: List<Denomination>? = listOf(createDenomination())
    ) = Item(
        code = code,
        name = name,
        description = null,
        imageUrl = imageUrl,
        locked = locked,
        rewardType = rewardType,
        denominations = denominations
    )

    private fun successResponse(
        items: List<Item>? = listOf(createItem()),
        totalItems: Int = 1,
        totalPages: Int = 1
    ) = BaseResponse(
        errors = null, message = null, referenceCode = null,
        result = ItemResult(items = items, totalItems = totalItems, totalPages = totalPages),
        statusCode = 200, statusName = null
    )

    // ── Pagination keys ─────────────────────────────────────────────────

    @Test
    fun `given first page, when loaded, then prevKey is null and nextKey is correct`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(totalPages = 3)

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `given middle page, when loaded, then both prevKey and nextKey are present`() = runTest {
        coEvery { remoteSource.getItems(2, 20, "", "", emptyList()) } returns
                successResponse(totalPages = 3)

        val result = createPagingSource().load(refreshParams(key = 2))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.prevKey)
        assertEquals(3, page.nextKey)
    }

    @Test
    fun `given last page, when loaded, then nextKey is null`() = runTest {
        coEvery { remoteSource.getItems(2, 20, "", "", emptyList()) } returns
                successResponse(totalPages = 2)

        val result = createPagingSource().load(refreshParams(key = 2))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `given single page, when loaded, then both keys are null`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(totalPages = 1)

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertNull(page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `given null totalPages, when loaded, then nextKey is null`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns BaseResponse(
            errors = null, message = null, referenceCode = null,
            result = ItemResult(items = listOf(createItem()), totalItems = 1, totalPages = null),
            statusCode = 200, statusName = null
        )

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Page)
        assertNull((result as PagingSource.LoadResult.Page).nextKey)
    }

    // ── Null / empty items ──────────────────────────────────────────────

    @Test
    fun `given null items list, when loaded, then returns empty page`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = null)

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Page)
        assertTrue((result as PagingSource.LoadResult.Page).data.isEmpty())
    }

    @Test
    fun `given empty items list, when loaded, then returns empty page`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = emptyList())

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Page)
        assertTrue((result as PagingSource.LoadResult.Page).data.isEmpty())
    }

    // ── toDomainItem mapping ────────────────────────────────────────────

    @Test
    fun `given valid item, when mapped, then all domain fields are correct`() = runTest {
        val item = createItem(
            code = "ABC",
            name = "My Item",
            imageUrl = "https://img.test/1.png",
            locked = true,
            rewardType = "GIFT",
            denominations = listOf(
                createDenomination(points = 250, discount = "15%", categories = listOf("food", "drink"))
            )
        )
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = listOf(item))

        val result = createPagingSource().load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        val domain = page.data[0]
        assertEquals("ABC", domain.code)
        assertEquals("My Item", domain.name)
        assertEquals("https://img.test/1.png", domain.imageUrl)
        assertEquals(true, domain.locked)
        assertEquals("GIFT", domain.rewardType)
        assertEquals(250, domain.points)
        assertEquals("15%", domain.discount)
        assertEquals(listOf("food", "drink"), domain.categories)
    }

    @Test
    fun `given item with null optional fields, when mapped, then defaults are used`() = runTest {
        val item = createItem(
            imageUrl = null,
            locked = null,
            rewardType = null,
            denominations = listOf(createDenomination(discount = null, categories = null))
        )
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = listOf(item))

        val result = createPagingSource().load(refreshParams())

        val domain = (result as PagingSource.LoadResult.Page).data[0]
        assertEquals("", domain.imageUrl)
        assertEquals(false, domain.locked)
        assertEquals("", domain.rewardType)
        assertNull(domain.discount)
        assertEquals(emptyList<String>(), domain.categories)
    }

    // ── toDomainItem filtering ──────────────────────────────────────────

    @Test
    fun `given item with null code, when loaded, then item is filtered out`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = listOf(createItem(code = null)))

        val result = createPagingSource().load(refreshParams())

        assertTrue((result as PagingSource.LoadResult.Page).data.isEmpty())
    }

    @Test
    fun `given item with null name, when loaded, then item is filtered out`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = listOf(createItem(name = null)))

        val result = createPagingSource().load(refreshParams())

        assertTrue((result as PagingSource.LoadResult.Page).data.isEmpty())
    }

    @Test
    fun `given item with null denominations, when loaded, then item is filtered out`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = listOf(createItem(denominations = null)))

        val result = createPagingSource().load(refreshParams())

        assertTrue((result as PagingSource.LoadResult.Page).data.isEmpty())
    }

    @Test
    fun `given item with empty denominations, when loaded, then item is filtered out`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = listOf(createItem(denominations = emptyList())))

        val result = createPagingSource().load(refreshParams())

        assertTrue((result as PagingSource.LoadResult.Page).data.isEmpty())
    }

    @Test
    fun `given item with null points in denomination, when loaded, then item is filtered out`() = runTest {
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = listOf(createItem(denominations = listOf(createDenomination(points = null)))))

        val result = createPagingSource().load(refreshParams())

        assertTrue((result as PagingSource.LoadResult.Page).data.isEmpty())
    }

    @Test
    fun `given mix of valid and invalid items, when loaded, then only valid items are kept`() = runTest {
        val items = listOf(
            createItem(code = "VALID", name = "Good"),
            createItem(code = null, name = "No Code"),
            createItem(code = "NO_NAME", name = null),
            createItem(code = "NO_DENOM", denominations = null),
            createItem(code = "VALID2", name = "Also Good")
        )
        coEvery { remoteSource.getItems(1, 20, "", "", emptyList()) } returns
                successResponse(items = items, totalItems = 5)

        val result = createPagingSource().load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertEquals(2, page.data.size)
        assertEquals("VALID", page.data[0].code)
        assertEquals("VALID2", page.data[1].code)
    }

    // ── Null result error messages ──────────────────────────────────────

    @Test
    fun `given null result with errors field, when loaded, then errors field is used as message`() = runTest {
        coEvery { remoteSource.getItems(any(), any(), any(), any(), any()) } returns BaseResponse(
            errors = "Server error", message = "Other msg", referenceCode = null,
            result = null, statusCode = 500, statusName = null
        )

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals("Server error", (result as PagingSource.LoadResult.Error).throwable.message)
    }

    @Test
    fun `given null result with only message field, when loaded, then message field is used`() = runTest {
        coEvery { remoteSource.getItems(any(), any(), any(), any(), any()) } returns BaseResponse(
            errors = null, message = "Bad request", referenceCode = null,
            result = null, statusCode = 400, statusName = null
        )

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals("Bad request", (result as PagingSource.LoadResult.Error).throwable.message)
    }

    @Test
    fun `given null result with no error fields, when loaded, then default message is used`() = runTest {
        coEvery { remoteSource.getItems(any(), any(), any(), any(), any()) } returns BaseResponse(
            errors = null, message = null, referenceCode = null,
            result = null, statusCode = 500, statusName = null
        )

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals("Failed to fetch items", (result as PagingSource.LoadResult.Error).throwable.message)
    }

    // ── Exception handling ──────────────────────────────────────────────

    @Test
    fun `given NoConnectivityException, when loaded, then returns NoConnectivityException error`() = runTest {
        coEvery { remoteSource.getItems(any(), any(), any(), any(), any()) } throws
                NoConnectivityException()

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
        assertTrue((result as PagingSource.LoadResult.Error).throwable is DomainException.NoConnectivityException)
    }

    @Test
    fun `given IOException, when loaded, then returns NetworkException`() = runTest {
        coEvery { remoteSource.getItems(any(), any(), any(), any(), any()) } throws
                IOException("timeout")

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = (result as PagingSource.LoadResult.Error).throwable
        assertTrue(error is DomainException.NetworkException)
        assertEquals("Network error: timeout", error.message)
    }

    @Test
    fun `given HttpException, when loaded, then returns NetworkException`() = runTest {
        coEvery { remoteSource.getItems(any(), any(), any(), any(), any()) } throws
                HttpException(Response.error<Any>(503, "".toResponseBody()))

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
        assertTrue((result as PagingSource.LoadResult.Error).throwable is DomainException.HttpException)
    }

    @Test
    fun `given generic exception, when loaded, then returns UnknownException`() = runTest {
        every { Log.d(TAG, "Failed to fetch items: unexpected") } returns -1
        coEvery { remoteSource.getItems(any(), any(), any(), any(), any()) } throws
                RuntimeException("unexpected")

        val result = createPagingSource().load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = (result as PagingSource.LoadResult.Error).throwable
        assertTrue(error is DomainException.UnknownException)
        assertEquals("Unexpected error: unexpected", error.message)
    }

    // ── Parameters passed correctly ─────────────────────────────────────

    @Test
    fun `given custom parameters, when loaded, then parameters are forwarded to remoteSource`() = runTest {
        coEvery {
            remoteSource.getItems(1, 10, "shoes", "footwear", listOf("GIFT"))
        } returns successResponse()

        val pagingSource = CouponsPagingSource(
            remoteSource = remoteSource,
            name = "shoes",
            categoryCode = "footwear",
            rewardTypes = listOf("GIFT")
        )
        val result = pagingSource.load(refreshParams(loadSize = 10))

        assertTrue(result is PagingSource.LoadResult.Page)
    }
}