package com.dsquares.library.data.network

import com.dsquares.library.DSquareSDK
import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.items.ItemResult
import com.dsquares.library.data.network.model.login.LoginResult
import kotlinx.coroutines.delay
import java.io.IOException

class MockRemoteSource : IRemoteSource {

    private val pageCallCounts = mutableMapOf<Int, Int>()
    private var lastSearchQuery: String = ""

    override suspend fun login(userId: String): BaseResponse<LoginResult> {
        throw UnsupportedOperationException("Mock login not implemented")
    }

    override suspend fun getItems(
        page: Int,
        pageSize: Int,
        name: String,
        categoryCode: String?,
        rewardTypes: List<String>?
    ): BaseResponse<ItemResult> {
        // Reset retry tracking when search query changes
        if (name != lastSearchQuery) {
            pageCallCounts.clear()
            lastSearchQuery = name
        }

        // Track call count for this page
        val callCount = pageCallCounts.getOrDefault(page, 0) + 1
        pageCallCounts[page] = callCount

        // Simulate delays
        when (page) {
            1 -> delay(2000L)
            else -> delay(8000L)
        }

        // Simulate error on page 3 first attempt
        if (page == FAIL_ON_PAGE && callCount == 1) {
            throw IOException("Simulated network error on page $page")
        }

        // Get locale-appropriate data
        val locale = DSquareSDK.appContainer.appContext.resources?.configuration?.locales?.get(0)
        val isArabic = locale?.language == "ar"
        val allItems = if (isArabic) MockCouponData.arabicItems else MockCouponData.englishItems

        // Apply search filter
        val filtered = if (name.isBlank()) {
            allItems
        } else {
            allItems.filter { it.name?.contains(name, ignoreCase = true) == true }
        }

        // Paginate
        val totalItems = filtered.size
        val totalPages = if (totalItems == 0) 0 else (totalItems + pageSize - 1) / pageSize
        val startIndex = (page - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, totalItems)
        val pageItems = if (startIndex < totalItems) {
            filtered.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return BaseResponse(
            errors = null,
            message = null,
            referenceCode = null,
            result = ItemResult(
                items = pageItems,
                totalItems = totalItems,
                totalPages = totalPages
            ),
            statusCode = 200,
            statusName = "OK"
        )
    }

    companion object {
        private const val FAIL_ON_PAGE = 3
    }
}