package com.dsquares.library.data.network

import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.items.ItemResult
import com.dsquares.library.data.network.model.login.LoginResult

interface IRemoteSource {
    suspend fun login(userId: String): BaseResponse<LoginResult>

    suspend fun getItems(
        page: Int,
        pageSize: Int,
        name: String,
        categoryCode: String?,
        rewardTypes: List<String>?
    ): BaseResponse<ItemResult>
}