package com.dsquares.library.domain.model

data class DomainItemsPage(
    val items: List<DomainCoupon>,
    val totalItems: Int?,
    val totalPages: Int?
)