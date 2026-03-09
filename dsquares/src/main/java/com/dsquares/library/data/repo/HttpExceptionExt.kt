package com.dsquares.library.data.repo

import android.util.Log
import com.dsquares.library.constants.TAG
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException

fun HttpException.extractErrorMessage(): String {
    val body = response()?.errorBody()?.string()
    if (body.isNullOrBlank()) return message()

    return try {
        val errorJson = JSONObject(body)

        val errorMessage = errorJson.optString("message").ifEmpty { null }
            ?: errorJson.optString("errors").ifEmpty { null }
        val statusName = errorJson.optString("statusName").ifEmpty { null }

        when {
            statusName != null && errorMessage != null -> "$statusName: $errorMessage"
            errorMessage != null -> errorMessage
            else -> message()
        }

    } catch (e: JSONException) {
        Log.d(TAG, "Failed to parse message from error body: ${e.message}")
        message()
    }
}