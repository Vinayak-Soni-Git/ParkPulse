package com.sonisoft.parkpulse.feature.auth.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.sonisoft.parkpulse.feature.auth.domain.model.AuthUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AuthSessionManagerTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var authSessionManager: AuthSessionManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val testFile = tmpFolder.newFile("test_datastore.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { testFile }
        )

        authSessionManager = AuthSessionManager(dataStore)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when session is saved, user is logged in and data is retrievable`() = runTest {
        val user = AuthUser("123", "Admin", "admin@parkpulse.com")
        
        authSessionManager.saveSession(user)
        
        assertEquals(true, authSessionManager.isLoggedIn.first())
        
        val retrievedUser = authSessionManager.authUser.first()
        assertEquals(user.id, retrievedUser?.id)
        assertEquals(user.name, retrievedUser?.name)
        assertEquals(user.email, retrievedUser?.email)
    }

    @Test
    fun `when session is cleared, user is logged out and data is null`() = runTest {
        val user = AuthUser("123", "Admin", "admin@parkpulse.com")
        authSessionManager.saveSession(user)
        
        authSessionManager.clearSession()
        
        assertEquals(false, authSessionManager.isLoggedIn.first())
        assertEquals(null, authSessionManager.authUser.first())
    }
}