package com.dsquares.library.data.network

import com.dsquares.library.data.network.api.ApiService
import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.items.ItemResult
import com.dsquares.library.data.network.model.login.LoginResult
import com.dsquares.library.di.ServiceLocator

class RemoteSource(private val apiService: ApiService = ServiceLocator.apiService) : IRemoteSource {

    override suspend fun login(userId: String): BaseResponse<LoginResult> {
        val body = mapOf("UserIdentifier" to userId)
        return apiService.login(body)
    }

    override suspend fun getItems(
        page: Int,
        pageSize: Int,
        name: String,
        categoryCode: String?,
        rewardTypes: List<String>?
    ): BaseResponse<ItemResult> {
        return apiService.getItems(page, pageSize, name, categoryCode, rewardTypes)
    }
}