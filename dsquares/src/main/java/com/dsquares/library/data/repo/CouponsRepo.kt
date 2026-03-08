package com.dsquares.library.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.dsquares.library.data.network.IRemoteSource
import com.dsquares.library.data.network.RemoteSource
import com.dsquares.library.domain.ICouponsRepo
import com.dsquares.library.domain.model.DomainCoupon

class CouponsRepo(
    private val remoteSource: IRemoteSource = RemoteSource()
) : ICouponsRepo {

    override fun getItems(
        name: String,
        categoryCode: String?,
        rewardTypes: List<String>?
    ): Pager<Int, DomainCoupon> {
        return Pager(
            config = PagingConfig(
                pageSize = CouponsPagingSource.DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                CouponsPagingSource(
                    remoteSource = remoteSource,
                    name = name,
                    categoryCode = categoryCode,
                    rewardTypes = rewardTypes
                )
            }
        )
    }
}
