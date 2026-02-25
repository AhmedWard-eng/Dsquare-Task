package com.dsquares.library.domain.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dsquares.library.domain.ICouponsRepo
import com.dsquares.library.domain.model.DomainCoupon
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class GetCouponsUseCaseTest {

    private val couponsRepo = mockk<ICouponsRepo>()
    private lateinit var useCase: GetCouponsUseCase

    @Before
    fun setup() {
        useCase = GetCouponsUseCase(couponsRepo)
    }

    private fun fakePager(): Pager<Int, DomainCoupon> = Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = {
            object : PagingSource<Int, DomainCoupon>() {
                override fun getRefreshKey(state: PagingState<Int, DomainCoupon>): Int? = null
                override suspend fun load(params: LoadParams<Int>) =
                    LoadResult.Page<Int, DomainCoupon>(emptyList(), null, null)
            }
        }
    )

    // ── Delegation tests ─────────────────────────────────────────────────

    @Test
    fun `given all parameters, when invoked, then delegates to repo with same parameters`() {
        val pager = fakePager()
        every { couponsRepo.getItems("coffee", "food", listOf("points")) } returns pager

        val result = useCase("coffee", "food", listOf("points"))

        assertSame(pager, result)
        verify { couponsRepo.getItems("coffee", "food", listOf("points")) }
    }

    @Test
    fun `given null categoryCode, when invoked, then delegates null categoryCode to repo`() {
        val pager = fakePager()
        every { couponsRepo.getItems("coffee", null, listOf("points")) } returns pager

        val result = useCase("coffee", null, listOf("points"))

        assertSame(pager, result)
        verify { couponsRepo.getItems("coffee", null, listOf("points")) }
    }

    @Test
    fun `given null rewardTypes, when invoked, then delegates null rewardTypes to repo`() {
        val pager = fakePager()
        every { couponsRepo.getItems("coffee", "food", null) } returns pager

        val result = useCase("coffee", "food", null)

        assertSame(pager, result)
        verify { couponsRepo.getItems("coffee", "food", null) }
    }

    @Test
    fun `given all nulls for optional params, when invoked, then delegates to repo`() {
        val pager = fakePager()
        every { couponsRepo.getItems("", null, null) } returns pager

        val result = useCase("", null, null)

        assertSame(pager, result)
        verify { couponsRepo.getItems("", null, null) }
    }

    @Test
    fun `given empty rewardTypes list, when invoked, then delegates empty list to repo`() {
        val pager = fakePager()
        every { couponsRepo.getItems("test", "cat", emptyList()) } returns pager

        val result = useCase("test", "cat", emptyList())

        assertSame(pager, result)
        verify { couponsRepo.getItems("test", "cat", emptyList()) }
    }
}