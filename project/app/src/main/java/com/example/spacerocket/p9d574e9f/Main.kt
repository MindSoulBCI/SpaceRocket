package com.example.spacerocket.p9d574e9f
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.*
import com.pico.spatial.ui.platform.resize.*
import com.example.spacerocket.p9d574e9f.content.HomeVolume
import com.example.spacerocket.p9d574e9f.ui.*
fun mainApp(scope: SpatialAppScope) = with(scope) {
 val session=FlightViewModelFactory.session
 Stage(id="drone-flight") { PicoTheme { DroneScreen() } }
 for (windowId in listOf("drone-control","email-login","email-register")) {
  WindowContainer(id=windowId,form=Form.Planar,defaultSize=WindowContainerSize(width=700.dp,height=800.dp),placement={containers.firstOrNull {it.id=="home"}?.let {if(windowId=="email-register") Placement.right(it,120.dp) else Placement.left(it,120.dp)} ?: Placement.none()},enableMaterialBackground=true) {
   PicoTheme { if(windowId=="drone-control") DroneControls() else AccountPanel(windowId=="email-register") }
  }
 }
 WindowContainer(id="eeg-monitor",form=Form.Planar,defaultSize=WindowContainerSize(width=800.dp,height=900.dp),
  placement={containers.firstOrNull {it.id=="home"}?.let {Placement.right(it,160.dp)} ?: Placement.none()},
  resizeType=ContainerResizeType.ContentSize,enableMaterialBackground=true) {
  PicoTheme {com.example.spacerocket.p9d574e9f.ui.components.EegPanel()}
 }
 Stage(id="gaze-launch") { PicoTheme { GazeLaunchScreen(session) } }
 DefaultWindowContainer { PicoTheme { WelcomeScreen() } }
 WindowContainer(id="rocket-scene",form=Form.Volumetric,defaultSize=WindowContainerSize(width=1440.dp,height=1440.dp,depth=1440.dp),placement={containers.firstOrNull {it.id=="home"}?.let {Placement.left(it,160.dp)} ?: Placement.none()},enableMaterialBackground=false) {
  PicoTheme { HomeVolume(session,Modifier.windowConstraints(minWidth=1200.dp,minHeight=1200.dp,maxWidth=1800.dp,maxHeight=1800.dp,minDepth=1200.dp,maxDepth=1800.dp)) }
 }
 WindowContainer(id="mission-control",form=Form.Planar,
  defaultSize=WindowContainerSize(width=640.dp,height=640.dp),
  placement={ containers.firstOrNull { it.id=="rocket-scene" }?.let { Placement.bottom(it,120.dp) } ?: containers.firstOrNull {it.id=="home"}?.let {Placement.bottom(it,120.dp)} ?: Placement.none() },
  resizeType=ContainerResizeType.ContentSize,
  defaultResizeRestriction=ContainerResizeRestriction.UniformResizable,
  enableMaterialBackground=true) {
  PicoTheme { FlightScreen(session,Modifier.windowConstraints(minWidth=560.dp,minHeight=560.dp,maxWidth=800.dp,maxHeight=800.dp)) }
 }
}
