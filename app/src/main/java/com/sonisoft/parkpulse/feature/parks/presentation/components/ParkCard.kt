package com.sonisoft.parkpulse.feature.parks.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import com.sonisoft.parkpulse.core.designsystem.theme.CardWhite
import com.sonisoft.parkpulse.core.designsystem.theme.Charcoal
import com.sonisoft.parkpulse.core.designsystem.theme.CharcoalLight
import com.sonisoft.parkpulse.core.designsystem.theme.ErrorRed
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreen
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreenDark
import com.sonisoft.parkpulse.core.designsystem.theme.LightGrey
import com.sonisoft.parkpulse.core.designsystem.theme.OffWhite
import com.sonisoft.parkpulse.feature.parks.domain.model.BoundaryQuality
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParkCard(
    park: Park,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = park.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Charcoal
                    )
                    Text(
                        text = park.type.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalLight
                    )
                }

                StatusBadge(status = park.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Area",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalLight
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,.1f sq.m", park.areaInSquareMeters),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Last Survey",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalLight
                    )
                    Text(
                        text = park.lastSurveyDate?.let {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "Not surveyed yet",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (park.lastSurveyDate == null) ErrorRed else Charcoal
                    )
                }
            }

            if (park.boundaryQuality == BoundaryQuality.NEEDS_REVIEW || park.boundaryQuality == BoundaryQuality.OSM_MAPPED_UNVERIFIED) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(LightGrey, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = "Warning",
                        tint = CharcoalLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Boundary needs review",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalLight,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                val syncIcon = when (park.syncStatus) {
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.SYNCED -> Icons.Rounded.CloudDone
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.PENDING -> Icons.Rounded.CloudOff
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.SYNCING -> Icons.Rounded.CloudUpload
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.FAILED -> Icons.Rounded.ErrorOutline
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.CONFLICT -> Icons.Rounded.WarningAmber
                }
                val syncColor = when (park.syncStatus) {
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.SYNCED -> ForestGreen
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.PENDING -> CharcoalLight
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.SYNCING -> Charcoal
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.FAILED -> ErrorRed
                    com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.CONFLICT -> ErrorRed
                }

                Icon(
                    imageVector = syncIcon,
                    contentDescription = park.syncStatus.name,
                    tint = syncColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val syncText = if (park.syncStatus == com.sonisoft.parkpulse.feature.parks.domain.model.SyncStatus.CONFLICT) "Conflict" else park.syncStatus.name.lowercase().replaceFirstChar { it.titlecase() }
                Text(
                    text = syncText,
                    style = MaterialTheme.typography.labelSmall,
                    color = syncColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: ParkStatus) {
    val (backgroundColor, textColor) = when (status) {
        ParkStatus.OPEN -> Pair(ForestGreen, OffWhite)
        ParkStatus.NEEDS_REPAIR -> Pair(androidx.compose.ui.graphics.Color(0xFFF57C00), OffWhite)
        ParkStatus.CLOSED -> Pair(ErrorRed, OffWhite)
        ParkStatus.UNKNOWN -> Pair(LightGrey, Charcoal)
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.name.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
