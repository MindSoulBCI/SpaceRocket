package com.example.spacerocket.p9d574e9f.content
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.pico.spatial.ui.foundation.layout.depth
import com.example.spacerocket.p9d574e9f.ui.*
import com.example.spacerocket.p9d574e9f.ui.components.*
import com.example.spacerocket.p9d574e9f.ui.scene.RocketScene
import com.example.spacerocket.p9d574e9f.domain.model.*
@Composable fun HomeVolume(viewModel:FlightViewModel,modifier:Modifier=Modifier) {
 FlightSession(viewModel,"volume")
 val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 val state=uiState.flight
 val navigator=LocalSpatialNavigator.current
 var rootSize by remember { mutableStateOf(IntSize.Zero) }
 LaunchedEffect(Unit) { navigator.openWindowContainer("mission-control",tag="flight-controls") }

 BoxWithConstraints(modifier.fillMaxSize().onSizeChanged { rootSize=it }) {
  val sceneDepth=maxWidth-48.dp
  Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   MissionText("空间火箭",40)
   MissionText("SPACE ROCKET / 01",14)
   MissionText(FlightCopy.labels[state.phase.ordinal],28)
   key(state.attempt) {
    RocketScene(state,rootSize,Modifier.weight(1f).fillMaxWidth().depth(sceneDepth),viewModel::onEvent)
   }
  }
 }
}
