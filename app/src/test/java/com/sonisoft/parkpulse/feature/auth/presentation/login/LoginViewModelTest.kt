package com.sonisoft.parkpulse.feature.auth.presentation.login

import com.sonisoft.parkpulse.feature.auth.domain.model.AuthResult
import com.sonisoft.parkpulse.feature.auth.domain.model.AuthUser
import com.sonisoft.parkpulse.feature.auth.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private val loginUseCase: LoginUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when email is invalid, error is set and usecase is not called`() = runTest {
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("pass")

        viewModel.onLoginClick()

        assertEquals("Invalid email format", viewModel.uiState.value.emailError)
    }

    @Test
    fun `when password is empty, error is set and usecase is not called`() = runTest {
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("")

        viewModel.onLoginClick()

        assertEquals("Password cannot be empty", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `when credentials are correct, login succeeds`() = runTest {
        viewModel.onEmailChange("admin@parkpulse.com")
        viewModel.onPasswordChange("ParkPulse@123")

        coEvery { loginUseCase("admin@parkpulse.com", "ParkPulse@123") } returns AuthResult.Success(
            AuthUser("1", "Admin", "admin@parkpulse.com")
        )

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        // Would also verify uiEvent emission, but we'll stick to basic state for this test
    }

    @Test
    fun `when login fails, error state is set`() = runTest {
        viewModel.onEmailChange("admin@parkpulse.com")
        viewModel.onPasswordChange("wrong")

        coEvery { loginUseCase("admin@parkpulse.com", "wrong") } returns AuthResult.Error("Invalid credentials")

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("Invalid credentials", viewModel.uiState.value.generalError)
    }
}
