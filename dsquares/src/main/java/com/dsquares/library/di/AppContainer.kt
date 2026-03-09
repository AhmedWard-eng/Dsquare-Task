package com.dsquares.library.di

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.dsquares.library.BuildConfig
import com.dsquares.library.security.CryptoManager
import com.dsquares.library.data.local.TokenManager
import com.dsquares.library.data.network.RemoteSource
import com.dsquares.library.data.network.IRemoteSource
import com.dsquares.library.data.network.api.ApiService
import com.dsquares.library.data.network.interceptor.AuthInterceptor
import com.dsquares.library.data.network.interceptor.ConnectivityInterceptor
import com.dsquares.library.data.network.interceptor.HeadersInterceptor
import com.dsquares.library.data.network.interceptor.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class AppContainer(
    private val appContext: Context,
    private val apiKey: String
) {
    val cryptoManager: CryptoManager by lazy { CryptoManager() }

    val tokenManager: TokenManager by lazy {
        TokenManager(appContext.tokenDataStore, cryptoManager)
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(tokenManager)
    }

    private val tokenAuthenticator: TokenAuthenticator by lazy {
        TokenAuthenticator(tokenManager) { remoteSource }
    }

    private val connectivityInterceptor: ConnectivityInterceptor by lazy {
        ConnectivityInterceptor(appContext)
    }

    private val headersInterceptor: HeadersInterceptor by lazy {
        HeadersInterceptor(apiKey) {
            appContext.resources?.configuration?.locales?.get(0)
                ?: java.util.Locale.getDefault()
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(connectivityInterceptor)
            .addInterceptor(headersInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor { message ->
                if (BuildConfig.DEBUG) Log.d("OkHttp", message)
            }.apply { level = HttpLoggingInterceptor.Level.BODY })
            .authenticator(tokenAuthenticator)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val remoteSource: IRemoteSource by lazy {
        RemoteSource(apiService)
    }
    val Context.tokenDataStore by preferencesDataStore(name = "dsquare_tokens")

    companion object {
        private const val BASE_URL = "https://connect-api.dsquares.com/"
    }
}