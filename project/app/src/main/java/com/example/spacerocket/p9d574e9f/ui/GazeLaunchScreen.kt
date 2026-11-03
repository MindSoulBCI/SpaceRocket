package com.example.spacerocket.p9d574e9f.ui
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.pico.spatial.core.ecs.*
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.tracking.eye.EyeTrackingProvider
import com.example.spacerocket.p9d574e9f.domain.usecase.GazeDwell
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
private fun Context.activity():Activity? = when(this) { is Activity -> this; is ContextWrapper -> baseContext.activity(); else -> null }
@Composable fun GazeLaunchScreen(viewModel:FlightViewModel) {
 val context=LocalContext.current
 val navigator=LocalSpatialNavigator.current
 val scope=rememberCoroutineScope()
 val eyes=remember { EyeTrackingProvider() }
 val dwell=remember { GazeDwell() }
 var status by remember {mutableStateOf("请启用眼动权限，然后凝视发射按钮 2 秒")}
 var progress by remember {mutableStateOf(0f)}
 var started by remember {mutableStateOf(false)}
 var fired by remember {mutableStateOf(false)}
 var root by remember {mutableStateOf<Entity?>(null)}
 var target by remember {mutableStateOf<Entity?>(null)}
 var targetBounds by remember {mutableStateOf<BoundingBox?>(null)}
 fun launchRocket() { if(!fired) {fired=true;viewModel.requestLaunch();scope.launch {navigator.closeStage()} } }
 DisposableEffect(Unit) {onDispose {eyes.stop();root?.destroy()} }
 LaunchedEffect(Unit) {
  while(!fired) {
   delay(50)
   val granted=ContextCompat.checkSelfPermission(context,"com.picovr.permission.EYE_TRACKING")==PackageManager.PERMISSION_GRANTED
   if(granted && !started) {runCatching {eyes.start()}.onSuccess {started=true}.onFailure {status="眼动启动失败，可返回使用点击发射"} }
   val sample=if(started) eyes.latestData else null
   val fresh=sample!=null && kotlin.math.abs(java.lang.System.currentTimeMillis()-sample.timestamp)<250
   val box=targetBounds
   val owner=root
   var hit=false
   if(fresh && sample!=null && owner!=null && box!=null) {
    val origin=owner.convertPositionFrom(sample.eyePose.position,null)
    val rotation=owner.convertRotationFrom(sample.eyePose.rotation,null)
    val direction=rotation.rotateVector(Vector3(0f,0f,-1f))
    if(kotlin.math.abs(direction.z)>0.0001f) {
     val t=(box.center.z-origin.z)/direction.z
     val x=origin.x+direction.x*t;val y=origin.y+direction.y*t
     hit=t>0f && x in box.min.x..box.max.x && y in box.min.y..box.max.y
    }
   }
   val complete=dwell.update(hit,fresh,0.05f);progress=dwell.seconds/2f
   if(started) status=if(!fresh) "没有有效眼动数据 · 请使用支持眼动的头显" else if(hit) "保持凝视 ${(progress*100).toInt()}%" else "凝视下方发射按钮 2 秒，移开视线会取消"
   if(complete) launchRocket()
  }
 }
 SpatialView(modifier=Modifier.fillMaxSize(),attachments={
  AttachmentPanel("launch") { Button(onClick={launchRocket()},modifier=Modifier.width(360.dp).height(100.dp)) {Text("发射火箭 · ${(progress*100).toInt()}%") } }
  AttachmentPanel("status") { Column(Modifier.width(640.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Text("眼动发射",color=androidx.compose.ui.graphics.Color.White)
   Text(status,color=androidx.compose.ui.graphics.Color.White)
   Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
    Button(onClick={context.activity()?.let {ActivityCompat.requestPermissions(it,arrayOf("com.picovr.permission.EYE_TRACKING"),2401)}}) {Text("启用眼动")}
    Button(onClick={scope.launch {navigator.closeStage()}}) {Text("返回桌面")}
   }
  } }
 },initial={content,attachments ->
  val owner=Entity();root=owner;content.addEntity(owner)
  attachments.entity("launch")?.let {owner.addChild(it);it.components.get<TransformComponent>()?.position=Vector3(0f,1.1f,-1.8f);target=it}
  attachments.entity("status")?.let {owner.addChild(it);it.components.get<TransformComponent>()?.position=Vector3(0f,1.55f,-1.8f)}
  val rocket=Entity.loadSuspend("asset://starship.glb");owner.addChild(rocket)
  val bounds=rocket.getVisualBounds(owner,recursive=true,enabledOnly=true)
  val scale=0.8f/bounds.size.y
  rocket.components.get<TransformComponent>()?.apply {scaleVector=Vector3(scale,scale,scale);position=Vector3(-0.7f-bounds.center.x*scale,0.8f-bounds.min.y*scale,-2f-bounds.center.z*scale)}
 },update={_,_ ->
  val owner=root;val button=target
  if(owner!=null && button!=null) targetBounds=button.getVisualBounds(owner,recursive=true,enabledOnly=true)
 })
}
