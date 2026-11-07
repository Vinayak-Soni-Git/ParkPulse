package com.sonisoft.parkpulse.feature.parks.presentation.map

import android.Manifest
import android.content.Context
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonisoft.parkpulse.core.designsystem.theme.CardWhite
import com.sonisoft.parkpulse.core.designsystem.theme.Charcoal
import com.sonisoft.parkpulse.core.designsystem.theme.CharcoalLight
import com.sonisoft.parkpulse.core.designsystem.theme.ErrorRed
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreen
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreenDark
import com.sonisoft.parkpulse.core.designsystem.theme.OffWhite
import com.sonisoft.parkpulse.core.designsystem.theme.LightGrey
import com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.view.MotionEvent

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onStartSurvey: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(granted)
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Location permission denied. Cannot show your location.")
            }
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        viewModel.checkLocationPermission()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.End) {
                if (!uiState.isDrawingMode) {
                    FloatingActionButton(onClick = { viewModel.toggleDrawingMode() }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Park")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                FloatingActionButton(
                    onClick = {
                        if (!uiState.hasLocationPermission) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "My Location")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OsmMapView(
                parks = uiState.parks,
                selectedPark = uiState.selectedPark,
                userLocation = uiState.userLocation?.let { com.sonisoft.parkpulse.feature.parks.domain.model.GeoPoint(it.latitude, it.longitude) },
                hasLocationPermission = uiState.hasLocationPermission,
                onParkClick = { viewModel.selectPark(it) },
                isDrawingMode = uiState.isDrawingMode,
                drawingPoints = uiState.drawingPoints,
                onAddDrawingPoint = { viewModel.addDrawingPoint(it) },
                onUpdateDrawingPoint = { index, point -> viewModel.updateDrawingPoint(index, point) }
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.selectedPark?.let { park ->
                ParkDetailsBottomSheet(
                    park = park,
                    distance = uiState.distanceToSelectedPark,
                    bearing = uiState.bearingToSelectedPark,
                    onDismiss = { viewModel.selectPark(null) },
                    onStartSurvey = onStartSurvey,
                    onEditPark = { viewModel.toggleDrawingMode(it) },
                    onExportGeoJson = { viewModel.exportParkGeoJson(it, context) }
                )
            }
            
            if (uiState.isDrawingMode) {
                DrawingControls(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    drawingPointsCount = uiState.drawingPoints.size,
                    drawingArea = uiState.drawingArea,
                    isWalkMode = uiState.isWalkMode,
                    isWalking = uiState.isWalking,
                    onUndo = { viewModel.undoLastDrawingPoint() },
                    onClear = { viewModel.clearDrawing() },
                    onCancel = { viewModel.toggleDrawingMode() },
                    onFinish = { viewModel.setShowCreateParkForm(true) },
                    onToggleWalkMode = { viewModel.toggleWalkMode() },
                    onToggleWalking = { viewModel.toggleWalking() },
                    onSetManualMode = { viewModel.setManualMode() }
                )
            }

            if (uiState.showCreateParkForm) {
                val initialName = if (uiState.editingParkId != null) {
                    uiState.parks.find { it.id == uiState.editingParkId }?.name ?: ""
                } else ""
                val initialType = if (uiState.editingParkId != null) {
                    uiState.parks.find { it.id == uiState.editingParkId }?.type ?: com.sonisoft.parkpulse.feature.parks.domain.model.ParkType.GARDEN
                } else com.sonisoft.parkpulse.feature.parks.domain.model.ParkType.GARDEN
                
                CreateParkBottomSheet(
                    initialName = initialName,
                    initialType = initialType,
                    areaInSquareMeters = uiState.drawingArea,
                    perimeterInMeters = uiState.drawingPerimeter,
                    pointsCount = uiState.drawingPoints.size,
                    drawingError = uiState.drawingError,
                    onDismiss = { viewModel.setShowCreateParkForm(false) },
                    onSave = { name, type -> viewModel.saveNewPark(name, type) }
                )
            }
        }
    }
}

@Composable
private fun OsmMapView(
    parks: List<Park>,
    selectedPark: Park?,
    userLocation: GeoPoint?,
    hasLocationPermission: Boolean,
    onParkClick: (Park) -> Unit,
    isDrawingMode: Boolean,
    drawingPoints: List<GeoPoint>,
    onAddDrawingPoint: (GeoPoint) -> Unit,
    onUpdateDrawingPoint: (Int, GeoPoint) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                setMultiTouchControls(true)

                // Center on Jaipur by default
                controller.setZoom(12.0)
                controller.setCenter(OsmGeoPoint(26.9124, 75.7873))
            }
        },
        update = { map ->
            map.overlays.clear()

            // Add user location overlay
            if (hasLocationPermission) {
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                locationOverlay.enableMyLocation()
                map.overlays.add(locationOverlay)

                // If we want to strictly follow user location clicks, we'd manage the center here.
                // However, the location overlay has its own follow location mode.
            }

            // Draw parks
            parks.forEach { park ->
                if (park.boundaryPoints.isNotEmpty()) {
                    val polygon = Polygon(map).apply {
                        points = park.boundaryPoints.map { OsmGeoPoint(it.latitude, it.longitude) }

                        val isSelected = park.id == selectedPark?.id
                        
                        val parkColor = when (park.status) {
                            ParkStatus.OPEN -> Color(0x664CAF50)
                            ParkStatus.NEEDS_REPAIR -> Color(0x66FF9800)
                            ParkStatus.CLOSED -> Color(0x66F44336)
                            ParkStatus.UNKNOWN -> Color(0x669E9E9E)
                        }

                        if (isSelected) {
                            fillPaint.color = parkColor.copy(alpha = 0.6f).toArgb()
                            outlinePaint.color = Color.White.toArgb()
                            outlinePaint.strokeWidth = 6f
                        } else {
                            fillPaint.color = parkColor.toArgb()
                            outlinePaint.color = parkColor.copy(alpha = 1f).toArgb()
                            outlinePaint.strokeWidth = 3f
                        }

                        setOnClickListener { _, _, _ ->
                            if (!isDrawingMode) {
                                onParkClick(park)
                                true
                            } else {
                                false
                            }
                        }
                    }
                    map.overlays.add(polygon)
                }
            }

            if (isDrawingMode) {
                // Taps on map add points
                val mapEventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: OsmGeoPoint): Boolean {
                        onAddDrawingPoint(GeoPoint(p.latitude, p.longitude))
                        return true
                    }
                    override fun longPressHelper(p: OsmGeoPoint): Boolean = false
                }
                map.overlays.add(MapEventsOverlay(mapEventsReceiver))

                // Drawing polygon
                if (drawingPoints.isNotEmpty()) {
                    val drawingPolygon = Polygon(map).apply {
                        points = drawingPoints.map { OsmGeoPoint(it.latitude, it.longitude) }
                        
                        // Close visual polygon if >= 3 points
                        if (drawingPoints.size >= 3) {
                            points.add(OsmGeoPoint(drawingPoints.first().latitude, drawingPoints.first().longitude))
                        }

                        val drawingColor = Color(0xFF2196F3)
                        fillPaint.color = drawingColor.copy(alpha = 0.3f).toArgb()
                        outlinePaint.color = drawingColor.toArgb()
                        outlinePaint.strokeWidth = 5f
                    }
                    map.overlays.add(drawingPolygon)

                    // Drawing vertices
                    drawingPoints.forEachIndexed { index, point ->
                        val marker = Marker(map).apply {
                            position = OsmGeoPoint(point.latitude, point.longitude)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            icon = context.getDrawable(android.R.drawable.ic_menu_edit)
                            isDraggable = true
                            setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                override fun onMarkerDrag(marker: Marker) {
                                    onUpdateDrawingPoint(index, GeoPoint(marker.position.latitude, marker.position.longitude))
                                }
                                override fun onMarkerDragEnd(marker: Marker) {
                                    onUpdateDrawingPoint(index, GeoPoint(marker.position.latitude, marker.position.longitude))
                                }
                                override fun onMarkerDragStart(marker: Marker) {}
                            })
                            setOnMarkerClickListener { _, _ -> true }
                        }
                        map.overlays.add(marker)
                    }
                }
            }

            map.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun DrawingControls(
    modifier: Modifier = Modifier,
    drawingPointsCount: Int,
    drawingArea: Double,
    isWalkMode: Boolean,
    isWalking: Boolean,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onCancel: () -> Unit,
    onFinish: () -> Unit,
    onToggleWalkMode: () -> Unit,
    onToggleWalking: () -> Unit,
    onSetManualMode: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ForestGreen, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Drawing Park ($drawingPointsCount pts)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                }

                if (drawingPointsCount >= 3) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForestGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = String.format(java.util.Locale.getDefault(), "%,.0f m²", drawingArea),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.FilterChip(
                    selected = !isWalkMode,
                    onClick = onSetManualMode,
                    label = { Text("Manual Draw") },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreen, selectedLabelColor = OffWhite)
                )
                androidx.compose.material3.FilterChip(
                    selected = isWalkMode,
                    onClick = onToggleWalkMode,
                    label = { Text("Walk Mode") },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreen, selectedLabelColor = OffWhite)
                )
            }
            
            if (isWalkMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Walk the boundary to collect points", color = CharcoalLight, style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = onToggleWalking,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isWalking) ErrorRed else ForestGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(if (isWalking) "Pause" else "Start", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = CharcoalLight)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Cancel", style = MaterialTheme.typography.labelMedium, color = CharcoalLight)
                }

                OutlinedButton(
                    onClick = onUndo,
                    enabled = drawingPointsCount > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Undo", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onClear,
                    enabled = drawingPointsCount > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Clear", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onFinish,
                    enabled = drawingPointsCount >= 3,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = CardWhite,
                        disabledContainerColor = LightGrey,
                        disabledContentColor = CharcoalLight
                    ),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Finish", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
