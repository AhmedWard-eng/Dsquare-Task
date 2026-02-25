package com.dsquares.library.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

internal val Context.tokenDataStore by preferencesDataStore(name = "dsquare_tokens")

