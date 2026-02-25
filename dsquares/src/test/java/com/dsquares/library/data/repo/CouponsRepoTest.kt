package com.dsquares.library.data.repo

import androidx.paging.testing.asSnapshot
import com.dsquares.library.data.network.IRemoteSource
import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.items.Denomination
import com.dsquares.library.data.network.model.items.Item
import com.dsquares.library.data.network.model.items.ItemResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class CouponsRepoTest {

    private val remoteSource = mockk<IRemoteSource>()
    private lateinit var couponsRepo: CouponsRepo

    @Before
    fun setup() {
        couponsRepo = CouponsRepo(remoteSource)
    }

    @Test
    fun `given valid response, when getItems is collected, then domain items are emitted`() = runTest {
        val items = listOf(
            Item(
                code = "C1",
                name = "Item 1",
                description = null,
                imageUrl = "https://img.test/1.png",
                locked = false,
                rewardType = "REWARD",
                denominations = listOf(
                    Denomination(
                        brand = null, categories = listOf("cat1"), code = null,
                        denominationType = null, description = null, discount = "10%",
                        from = null, imageUrl = null, inStock = null, name = null,
                        points = 100, redemptionChannel = null, redemptionFactor = null,
                        termsAndConditions = null, to = null, usageInstructions = null, value = null
                    )
                )
            )
        )
        coEvery {
            remoteSource.getItems(1, any(), "", "", emptyList())
        } returns BaseResponse(
            errors = null, message = null, referenceCode = null,
            result = ItemResult(items = items, totalItems = 1, totalPages = 1),
            statusCode = 200, statusName = null
        )

        val pager = couponsRepo.getItems("", "", emptyList())
        val snapshot = pager.flow.asSnapshot()

        assertEquals(1, snapshot.size)
        assertEquals("C1", snapshot[0].code)
        assertEquals("Item 1", snapshot[0].name)
        assertEquals(100, snapshot[0].points)
    }

    @Test
    fun `given null result, when getItems is collected, then error is propagated`() = runTest {
        coEvery {
            remoteSource.getItems(any(), any(), any(), any(), any())
        } returns BaseResponse(
            errors = "Server error", message = null, referenceCode = null,
            result = null, statusCode = 500, statusName = "Error"
        )

        val pager = couponsRepo.getItems("", "", emptyList())
        val result = runCatching { pager.flow.asSnapshot() }

        val exception = result.exceptionOrNull()
        if (exception == null) {
            fail("Expected an exception to be thrown")
            return@runTest
        }
        assertEquals("Server error", exception.message)
    }
}
