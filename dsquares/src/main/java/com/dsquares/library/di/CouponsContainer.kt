package com.dsquares.library.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dsquares.library.data.repo.CouponsRepo
import com.dsquares.library.domain.usecase.GetCouponsUseCase
import com.dsquares.library.ui.screens.coupons.CouponsViewModel

internal class CouponsContainer(appContainer: AppContainer) {

    private val couponsRepo = CouponsRepo(appContainer.remoteSource)

    private val getCouponsUseCase = GetCouponsUseCase(couponsRepo)

    val couponsViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CouponsViewModel(getCouponsUseCase) as T
        }
    }
}