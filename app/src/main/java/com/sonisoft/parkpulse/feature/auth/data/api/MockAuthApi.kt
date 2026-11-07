package com.sonisoft.parkpulse.feature.auth.data.api

import com.sonisoft.parkpulse.feature.auth.domain.model.AuthResult
import com.sonisoft.parkpulse.feature.auth.domain.model.AuthUser
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class MockAuthApi @Inject constructor() {
    suspend fun login(email: String, password: String): AuthResult<AuthUser> {
        // Simulate realistic network latency
        delay(1500.milliseconds)

        // Demo credentials validation
        return if (email == "admin@parkpulse.com" && password == "ParkPulse@123") {
            AuthResult.Success(
                AuthUser(
                    id = "usr_12345",
                    name = "Park Admin",
                    email = "admin@parkpulse.com"
                )
            )
        } else {
            AuthResult.Error("Invalid email or password.")
        }
    }
}
