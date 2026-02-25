package com.dsquares.library.ui.screens.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.dsquares.library.domain.model.DomainCoupon
import com.dsquares.library.domain.usecase.GetCouponsUseCase
import com.dsquares.library.ui.models.coupons.CouponUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class CouponsViewModel(private val getCouponsUseCase: GetCouponsUseCase = GetCouponsUseCase()) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val coupons = _searchQuery
        .debounce { query -> if (query.isEmpty()) 0L else 300L }
        .flatMapLatest { query ->
            getCouponsUseCase(name = query, categoryCode = null, rewardTypes = null).flow
        }
        .map { pagingData -> pagingData.map { it.toCouponUiModel() } }
        .cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun DomainCoupon.toCouponUiModel(): CouponUiModel {
        return CouponUiModel(
            code = code,
            name = name,
            imageUrl = imageUrl,
            isLocked = locked,
            points = points,
            discount = discount,
            categories = categories
        )
    }
}