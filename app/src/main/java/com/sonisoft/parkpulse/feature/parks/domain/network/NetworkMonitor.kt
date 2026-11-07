package com.sonisoft.parkpulse.feature.parks.domain.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
