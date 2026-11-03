package com.example.spacerocket.p9d574e9f.ui
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.spacerocket.p9d574e9f.domain.model.*
import com.example.spacerocket.p9d574e9f.ui.components.MissionPanel
@Composable fun FlightSession(viewModel:FlightViewModel,id:String) {
 val owner=LocalLifecycleOwner.current
 val context=LocalContext.current
 DisposableEffect(owner,viewModel,id) {
  viewModel.attachStorage(context)
  val observer=LifecycleEventObserver { _,_ -> viewModel.setVisible(id,owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
  owner.lifecycle.addObserver(observer)
  viewModel.setVisible(id,owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
  onDispose { owner.lifecycle.removeObserver(observer);viewModel.setVisible(id,false) }
 }
}
@Composable fun FlightScreen(viewModel:FlightViewModel,modifier:Modifier=Modifier) {
 FlightSession(viewModel,"controls")
 val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 MissionPanel(uiState.flight,{ viewModel.onUiEvent(FlightUiEvent.Action(it)) },modifier)
}
