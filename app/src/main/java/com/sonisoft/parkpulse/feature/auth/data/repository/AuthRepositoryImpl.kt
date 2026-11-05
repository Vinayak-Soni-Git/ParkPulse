package com.sonisoft.parkpulse.feature.auth.data.repository

import com.sonisoft.parkpulse.feature.auth.data.api.MockAuthApi
import com.sonisoft.parkpulse.feature.auth.data.datastore.AuthSessionManager
import com.sonisoft.parkpulse.feature.auth.domain.model.AuthResult
import com.sonisoft.parkpulse.feature.auth.domain.model.AuthUser
import com.sonisoft.parkpulse.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val mockAuthApi: MockAuthApi,
    private val authSessionManager: AuthSessionManager
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = authSessionManager.isLoggedIn

    override suspend fun login(email: String, password: String): AuthResult<AuthUser> {
        val result = mockAuthApi.login(email, password)
        if (result is AuthResult.Success) {
            authSessionManager.saveSession(result.data)
        }
        return result
    }

    override suspend fun logout() {
        authSessionManager.clearSession()
    }

    override fun getAuthSession(): Flow<AuthUser?> {
        return authSessionManager.authUser
    }
}
