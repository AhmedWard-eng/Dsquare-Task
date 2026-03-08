package com.dsquares.library.data.repo

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dsquares.library.data.network.IRemoteSource
import com.dsquares.library.data.network.interceptor.NoConnectivityException
import com.dsquares.library.data.network.model.items.Item
import com.dsquares.library.di.ServiceLocator.TAG
import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.model.DomainCoupon
import retrofit2.HttpException
import java.io.IOException

class CouponsPagingSource(
    private val remoteSource: IRemoteSource,
    private val name: String,
    private val categoryCode: String?,
    private val rewardTypes: List<String>?
) : PagingSource<Int, DomainCoupon>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DomainCoupon> {
        val page = params.key ?: STARTING_PAGE
        return try {
            val response = remoteSource.getItems(
                page = page,
                pageSize = params.loadSize,
                name = name,
                categoryCode = categoryCode,
                rewardTypes = rewardTypes
            )

            val result = response.result
                ?: return LoadResult.Error(
                    Exception(response.errors ?: response.message ?: "Failed to fetch items")
                )

            val domainItems = result.items
                ?.mapNotNull { it.toDomainItem() }
                ?: emptyList()

            val totalPages = result.totalPages ?: 0
            val nextKey = if (page < totalPages) page + 1 else null
            val prevKey = if (page > STARTING_PAGE) page - 1 else null

            LoadResult.Page(
                data = domainItems,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (_: NoConnectivityException) {
            LoadResult.Error(DomainException.NoConnectivityException())
        } catch (e: IOException) {
            LoadResult.Error(DomainException.NetworkException(e))
        } catch (e: HttpException) {
            LoadResult.Error(DomainException.HttpException(e.extractErrorMessage()))
        } catch (e: Exception){
            Log.d(TAG, "Failed to fetch items: ${e.message}")
            LoadResult.Error(DomainException.UnknownException(e))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DomainCoupon>): Int? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        const val STARTING_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 5
    }
}

private fun Item.toDomainItem(): DomainCoupon? {
    val code = code ?: return null
    val name = name ?: return null
    val firstDenomination = denominations?.firstOrNull()
    val points = firstDenomination?.points ?: return null

    return DomainCoupon(
        code = code,
        name = name,
        imageUrl = imageUrl.orEmpty(),
        locked = locked ?: false,
        rewardType = rewardType.orEmpty(),
        points = points,
        discount = firstDenomination.discount,
        categories = firstDenomination.categories.orEmpty()
    )
}
