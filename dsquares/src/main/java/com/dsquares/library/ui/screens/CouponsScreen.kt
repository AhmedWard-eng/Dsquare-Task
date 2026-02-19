package com.dsquares.library.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dsquares.library.R
import com.dsquares.library.ui.components.CategoryChipRow
import com.dsquares.library.ui.components.CouponCard
import com.dsquares.library.ui.components.DsquareSearchBar
import com.dsquares.library.ui.components.DsquareTopBar
import com.dsquares.library.ui.models.CategoryUiModel
import com.dsquares.library.ui.models.CouponUiModel
import com.dsquares.library.ui.theme.DsquareTaskTheme

@Composable
fun CouponsScreen(
    coupons: List<CouponUiModel>,
    categories: List<CategoryUiModel>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchState by rememberSaveable { mutableStateOf("") }
    var selectedCategory: CategoryUiModel? by rememberSaveable {
        mutableStateOf(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        DsquareTopBar(
            title = stringResource(R.string.coupons_title),
            onBackClick = onBackClick
        )

        DsquareSearchBar(
            searchText = searchState,
            placeholder = stringResource(R.string.search_coupons),
            onSearchTextChanged = { searchState = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategoryChipRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(coupons, key = { it.code }) { coupon ->
                CouponCard(coupon = coupon)
            }
        }
    }
}

// -- Previews --

private val dummyCategories = listOf(
    CategoryUiModel("Shopping", 0),
    CategoryUiModel("Food & Beverage", 1),
    CategoryUiModel("Entertainment", 2),
    CategoryUiModel("Travel", 3),
    CategoryUiModel("Health & Beauty", 4),
    CategoryUiModel("Electronics", 5),
    CategoryUiModel("Fashion", 6),
    CategoryUiModel("Home & Living", 7),
    CategoryUiModel("Sports & Fitness", 8),
    CategoryUiModel("Education", 9)
)
private val sampleCoupons = listOf(
    CouponUiModel(
        code = "1",
        name = "Amazon",
        imageUrl = "https://www.behance.net/search/projects/ikea%20logo",
        isLocked = false,
        points = 5000,
        discount = "50",
        categories = listOf("Shopping")
    ), CouponUiModel(
        code = "2",
        name = "Noon",
        imageUrl = "",
        isLocked = true,
        points = 3000,
        discount = null,
        categories = listOf("Shopping")
    ), CouponUiModel(
        code = "3",
        name = "Starbucks",
        imageUrl = "",
        isLocked = false,
        points = 1000,
        discount = "20",
        categories = listOf("Food & Beverage")
    ), CouponUiModel(
        code = "4",
        name = "Ikea",
        imageUrl = "",
        isLocked = false,
        points = 8000,
        discount = null,
        categories = listOf("Shopping", "Entertainment")
    )
)

@Preview(showBackground = true)
@Composable
private fun CouponsScreenPreview() {
    DsquareTaskTheme {
        CouponsScreen(
            coupons = sampleCoupons, categories = dummyCategories, onBackClick = {}
        )
    }
}

private val dummyCategoriesAr = listOf(
    CategoryUiModel("تسوق", 0),
    CategoryUiModel("طعام ومشروبات", 1),
    CategoryUiModel("ترفيه", 2),
    CategoryUiModel("سفر", 3),
    CategoryUiModel("صحة وجمال", 4),
    CategoryUiModel("إلكترونيات", 5),
    CategoryUiModel("أزياء", 6),
    CategoryUiModel("منزل ومعيشة", 7),
    CategoryUiModel("رياضة ولياقة", 8),
    CategoryUiModel("تعليم", 9)
)

private val sampleCouponsAr = listOf(
    CouponUiModel(
        code = "1",
        name = "أمازون",
        imageUrl = "https://www.behance.net/search/projects/ikea%20logo",
        isLocked = false,
        points = 5000,
        discount = "50",
        categories = listOf("تسوق")
    ),
    CouponUiModel(
        code = "2",
        name = "نون",
        imageUrl = "",
        isLocked = true,
        points = 3000,
        discount = null,
        categories = listOf("تسوق")
    ),
    CouponUiModel(
        code = "3",
        name = "ستاربكس",
        imageUrl = "",
        isLocked = false,
        points = 1000,
        discount = "20",
        categories = listOf("طعام ومشروبات")
    ),
    CouponUiModel(
        code = "4",
        name = "ايكيا",
        imageUrl = "",
        isLocked = false,
        points = 8000,
        discount = null,
        categories = listOf("تسوق", "ترفيه")
    )
)

@Preview(showBackground = true, locale = "ar")
@Composable
private fun CouponsScreenArabicPreview() {
    DsquareTaskTheme {
        CouponsScreen(
            coupons = sampleCouponsAr,
            categories = dummyCategoriesAr,
            onBackClick = {}
        )
    }
}