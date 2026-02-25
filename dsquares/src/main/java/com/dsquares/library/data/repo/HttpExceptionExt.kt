package com.dsquares.library.data.repo

import android.util.Log
import com.dsquares.library.di.ServiceLocator.TAG
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException

fun HttpException.extractErrorMessage(): String {
    val body = response()?.errorBody()?.string()
    if (body.isNullOrBlank()) return message()

    return try {
        val errorJson = JSONObject(body)
        errorJson.optString("message").ifEmpty { null }
            ?: message()
    } catch (e: JSONException) {
        Log.d(TAG, "Failed to parse message from error body: ${e.message}")
        message()
    }
}