package com.dsquares.library.domain.model

data class DomainCoupon(
    val code: String,
    val name: String,
    val imageUrl: String,
    val locked: Boolean,
    val rewardType: String,
    val points: Int,
    val discount: String?,
    val categories: List<String>
)
