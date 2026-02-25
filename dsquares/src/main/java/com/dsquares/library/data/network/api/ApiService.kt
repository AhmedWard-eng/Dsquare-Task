package com.dsquares.library.data.network.api

import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.items.ItemResult
import com.dsquares.library.data.network.model.login.LoginResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/DynamicApp/v1/Integration/Token")
    suspend fun login(
        @Body body: Map<String, String>
    ): BaseResponse<LoginResult>

    @GET("api/DynamicApp/v1/Integration/Items")
    suspend fun getItems(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("name") name: String,
        @Query("categoryCode") categoryCode: String?,
        @Query("rewardTypes") rewardTypes: List<String>?
    ): BaseResponse<ItemResult>
}