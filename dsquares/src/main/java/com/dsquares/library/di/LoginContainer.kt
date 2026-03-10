package com.dsquares.library.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dsquares.library.data.repo.LoginRepo
import com.dsquares.library.domain.usecase.LoginByPhoneNumberUseCase
import com.dsquares.library.domain.usecase.LoginByUuidUseCase
import com.dsquares.library.domain.usecase.ValidatePhoneUseCase
import com.dsquares.library.ui.screens.login.LoginViewModel

internal class LoginContainer(appContainer: AppContainer) {

    private val loginRepo = LoginRepo(appContainer.remoteSource, appContainer.tokenManager)

    private val validatePhoneUseCase  = ValidatePhoneUseCase()

    private val loginByUuidUseCase = LoginByUuidUseCase(loginRepo)

    private val loginByPhoneNumberUseCase = LoginByPhoneNumberUseCase(loginByUuidUseCase, validatePhoneUseCase)

    val loginViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(loginByPhoneNumberUseCase) as T
        }
    }
}