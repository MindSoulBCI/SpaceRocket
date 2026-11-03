package com.example.spacerocket.p9d574e9f.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.pico.spatial.ui.platform.containers.StageStyle
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.pico.spatial.ui.design.*
import com.example.spacerocket.p9d574e9f.domain.model.*
import com.example.spacerocket.p9d574e9f.ui.theme.RocketColors

@Composable fun MissionPanel(state:FlightState,onEvent:(FlightEvent)->Unit,modifier:Modifier=Modifier) {
 val navigator=LocalSpatialNavigator.current
 val scope=rememberCoroutineScope()
 LaunchedEffect(com.example.spacerocket.p9d574e9f.ui.DroneSession.openRequested) {
  if(com.example.spacerocket.p9d574e9f.ui.DroneSession.openRequested) {
   com.example.spacerocket.p9d574e9f.ui.DroneSession.openRequested=false
   onEvent(FlightEvent.Retry)
   navigator.openStage("drone-flight",style=StageStyle.Mixed)
  }
 }

 BoxWithConstraints(modifier.fillMaxSize()) {
  val compact=maxWidth<=580.dp
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if(compact)24.dp else 32.dp),verticalArrangement=Arrangement.spacedBy(if(compact)12.dp else 16.dp)) {
   MissionText("任务 01 · 垂直起降",14)
   MissionText(FlightCopy.labels[state.phase.ordinal],28)
   MissionText(FlightCopy.instructions[state.phase.ordinal],18)
   MissionText("高度 ${"%.1f".format(state.altitude)} m  ·  速度 ${"%.1f".format(state.velocity)} m/s",18)
   MissionText(FlightCopy.result(state),14)
   Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
    if(state.phase==Phase.READY) Button(onClick={scope.launch {navigator.openStage("gaze-launch",style=StageStyle.Mixed)}},enabled=state.sceneReady) {Text("凝视发射")}
    if(state.phase==Phase.READY) Button(onClick={onEvent(FlightEvent.Start)},enabled=state.sceneReady,size=ButtonDefaults.Max) { Text(if(state.sceneReady)"发射火箭" else "加载火箭…") }
    if(state.active) Button(onClick={onEvent(FlightEvent.Pause)},size=ButtonDefaults.Max) { Text("暂停") }
    if(state.phase==Phase.PAUSED) Button(onClick={onEvent(FlightEvent.Resume)},size=ButtonDefaults.Max) { Text("继续任务") }
    if(state.phase in setOf(Phase.LANDED,Phase.FAILED,Phase.PAUSED,Phase.INPUT_LOST,Phase.ASSET_ERROR)) Button(onClick={onEvent(FlightEvent.Retry)},size=ButtonDefaults.Max) { Text("再飞一次") }
   }
   Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
    Button(onClick={navigator.openWindowContainer("eeg-monitor",tag="mindsoul-eeg")}) {Text("显示脑电窗口")}
    Button(onClick={navigator.closeWindowContainer("eeg-monitor",tag="mindsoul-eeg")}) {Text("隐藏脑电窗口")}
   }


  }
 }
}
@Composable fun MissionText(text:String,size:Int) {
 Text(text,color=RocketColors.missionInk,fontSize=size.sp,lineHeight=(size+if(size==14)8 else if(size==18)10 else 8).sp,fontWeight=if(size>=28) FontWeight.SemiBold else FontWeight.Medium)
}
