package com.dsquares.library.ui.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryUiModel(val name: String, val code: Int) : Parcelable