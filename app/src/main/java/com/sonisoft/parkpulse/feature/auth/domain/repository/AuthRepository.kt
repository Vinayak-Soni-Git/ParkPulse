package com.sonisoft.parkpulse.feature.auth.domain.repository

import com.sonisoft.parkpulse.feature.auth.domain.model.AuthResult
import com.sonisoft.parkpulse.feature.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(email: String, password: String): AuthResult<AuthUser>
    suspend fun logout()
    fun getAuthSession(): Flow<AuthUser?>
}
