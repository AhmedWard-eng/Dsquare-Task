package com.dsquares.library.data.network.api

import com.dsquares.library.data.network.model.BaseRequest
import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.items.CouponsRequestData
import com.dsquares.library.data.network.model.items.ItemResult
import com.dsquares.library.data.network.model.login.LoginRequestData
import com.dsquares.library.data.network.model.login.LoginResult
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/DynamicApp/v1/Integration/Token")
    suspend fun login(
        @Body body: BaseRequest<LoginRequestData>
    ): BaseResponse<LoginResult>

    @POST("api/DynamicApp/v1/Integration/Items")
    suspend fun getItems(
        @Body body: BaseRequest<CouponsRequestData>
    ): BaseResponse<ItemResult>
}