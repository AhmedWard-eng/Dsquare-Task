package com.dsquares.library.ui.screens.coupons

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.dsquares.library.R
import com.dsquares.library.domain.DomainException
import com.dsquares.library.ui.components.common.EmptyState
import com.dsquares.library.ui.components.common.ErrorRetryRow
import com.dsquares.library.ui.components.common.ErrorState
import com.dsquares.library.ui.components.common.DsquareSearchBar
import com.dsquares.library.ui.components.common.DsquareTopBar
import com.dsquares.library.ui.components.coupons.CategoryChipRow
import com.dsquares.library.ui.components.coupons.CouponCard
import com.dsquares.library.ui.models.coupons.CouponUiModel

@Composable
fun CouponsScreen(
    coupons: LazyPagingItems<CouponUiModel>,
    categories: List<String>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory: String? by rememberSaveable {
        mutableStateOf(null)
    }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val retryLabel = stringResource(R.string.retry)

    val refreshError = (coupons.loadState.refresh as? LoadState.Error)?.error
    LaunchedEffect(refreshError) {
        if (refreshError != null && coupons.itemCount > 0) {
            val result = snackbarHostState.showSnackbar(
                message = getErrorMessage(refreshError, context),
                actionLabel = retryLabel
            )
            if (result == SnackbarResult.ActionPerformed) coupons.refresh()
        }
    }

    val isRefreshing = coupons.loadState.refresh is LoadState.Loading && coupons.itemCount > 0

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { coupons.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                DsquareTopBar(
                    title = stringResource(R.string.coupons_title),
                    onBackClick = onBackClick
                )

                DsquareSearchBar(
                    searchText = searchQuery,
                    placeholder = stringResource(R.string.search_coupons),
                    onSearchTextChanged = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CategoryChipRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = {
                        selectedCategory = it
                        // TODO: this should be filter coupons based on category after providing the api to retrieve categories
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    if (coupons.itemCount > 0) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                count = coupons.itemCount,
                                key = coupons.itemKey { it.code }
                            ) { index ->
                                coupons[index]?.let { coupon ->
                                    CouponCard(coupon = coupon)
                                }
                            }

                            if (coupons.loadState.append is LoadState.Loading) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            if (coupons.loadState.append is LoadState.Error) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    val message = getErrorMessage(
                                        (coupons.loadState.append as LoadState.Error).error,
                                        context
                                    )
                                    ErrorRetryRow(
                                        message,
                                        onRetry = { coupons.retry() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    } else when (coupons.loadState.refresh) {
                        is LoadState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        is LoadState.Error -> {
                            ErrorState(
                                message = getErrorMessage(
                                    (coupons.loadState.refresh as LoadState.Error).error,
                                    context
                                ),
                                onRetry = { coupons.retry() },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        is LoadState.NotLoading -> {
                            EmptyState(
                                message = stringResource(R.string.no_coupons_found),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getErrorMessage(error: Throwable, context: Context): String =
    when (error) {
        is DomainException.NoConnectivityException -> context.getString(R.string.no_internet)
        else -> error.message ?: context.getString(R.string.something_went_wrong)
    }