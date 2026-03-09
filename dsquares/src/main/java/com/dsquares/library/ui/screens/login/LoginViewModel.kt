package com.dsquares.library.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.Result
import com.dsquares.library.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun login(phone: String) {
        if (_loginState.value is LoginUiState.Loading) return
        _loginState.value = LoginUiState.Loading
        viewModelScope.launch {
            when (val result = loginUseCase(phone)) {
                is Result.Success -> _loginState.value = LoginUiState.Success
                is Result.Failure -> when (result.exception) {
                    is DomainException.NoConnectivityException -> _loginState.value =
                        LoginUiState.NoInternetConnection

                    is DomainException.InvalidPhoneNumberException -> _loginState.value =
                        LoginUiState.InvalidPhoneNumber

                    else -> _loginState.value = LoginUiState.ServerError(result.exception.message)
                }
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
}

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class ServerError(val message: String?) : LoginUiState
    data object InvalidPhoneNumber : LoginUiState
    data object NoInternetConnection : LoginUiState
}
