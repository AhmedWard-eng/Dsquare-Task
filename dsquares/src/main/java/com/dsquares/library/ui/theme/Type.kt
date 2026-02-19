package com.dsquares.library.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.dsquares.library.R

val CairoFontFamily = FontFamily(
    Font(resId = R.font.cairo_regular, weight = FontWeight.Normal),
    Font(resId = R.font.cairo_medium, weight = FontWeight.Medium),
    Font(resId = R.font.cairo_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.cairo_bold, weight = FontWeight.Bold),
)
private fun cairoStyle(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit
) = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight
)

val Typography = Typography(
    titleMedium = cairoStyle(FontWeight.Bold, 16.sp, 24.sp),
    bodyMedium = cairoStyle(FontWeight.Medium, 14.sp, 24.sp),
    bodySmall = cairoStyle(FontWeight.Normal, 14.sp, 18.sp),
    titleSmall = cairoStyle(FontWeight.Bold, 16.sp, 16.sp),
    headlineSmall = cairoStyle(FontWeight.Normal, 16.sp, 16.sp),
    labelLarge = cairoStyle(FontWeight.Medium, 15.sp, 24.sp),
    labelMedium = cairoStyle(FontWeight.Normal, 14.sp, 14.sp),
    labelSmall = cairoStyle(FontWeight.Medium, 12.sp, 12.sp),
    bodyLarge = cairoStyle(FontWeight.SemiBold, 16.sp, 24.sp)
)