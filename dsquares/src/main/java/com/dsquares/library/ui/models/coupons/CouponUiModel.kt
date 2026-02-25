package com.dsquares.library.ui.models.coupons

data class CouponUiModel(
    val code: String,
    val name: String,
    val imageUrl: String,
    val isLocked: Boolean,
    val points: Int,
    val discount: String?,
    val categories: List<String>
)
