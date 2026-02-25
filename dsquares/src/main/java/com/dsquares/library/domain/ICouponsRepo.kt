package com.dsquares.library.domain

import androidx.paging.Pager
import com.dsquares.library.domain.model.DomainCoupon

interface ICouponsRepo {
    fun getItems(
        name: String,
        categoryCode: String?,
        rewardTypes: List<String>?
    ): Pager<Int, DomainCoupon>
}
