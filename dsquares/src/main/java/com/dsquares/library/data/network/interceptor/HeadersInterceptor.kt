package com.dsquares.library.data.network.interceptor

import com.dsquares.library.di.ServiceLocator
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale

class HeadersInterceptor(
    private val apiKey: String,
    private val localeProvider: () -> Locale = {
        ServiceLocator.appContext?.resources?.configuration?.locales?.get(0) ?: Locale.getDefault()
    }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .header("Accept-Language", localeProvider().language)
            .build()
        return chain.proceed(request)
    }
}
