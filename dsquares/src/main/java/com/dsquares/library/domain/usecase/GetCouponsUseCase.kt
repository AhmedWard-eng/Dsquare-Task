package com.dsquares.library.domain.usecase

import androidx.paging.Pager
import com.dsquares.library.data.repo.CouponsRepo
import com.dsquares.library.domain.ICouponsRepo
import com.dsquares.library.domain.model.DomainCoupon

class GetCouponsUseCase(private val itemsRepo: ICouponsRepo = CouponsRepo()) {

    operator fun invoke(
        name: String,
        categoryCode: String?,
        rewardTypes: List<String>?
    ): Pager<Int, DomainCoupon> {
        return itemsRepo.getItems(name, categoryCode, rewardTypes)
    }
}
