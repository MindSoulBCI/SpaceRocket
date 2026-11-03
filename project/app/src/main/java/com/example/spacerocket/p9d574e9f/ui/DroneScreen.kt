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
import com.pico.spatial.tracking.hmd.HMDTrackingProvider
import com.example.spacerocket.p9d574e9f.domain.usecase.GazeDwell
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
/** Device adapter must supply normalized focus only when a real MindSoul sample is valid. */
interface MindSoulFocusSource { fun currentFocus():Float? }
object DisconnectedMindSoul:MindSoulFocusSource {override fun currentFocus():Float?=null}
object DroneSession {
 var focusSource:MindSoulFocusSource=DisconnectedMindSoul
 var simulatedFocus by mutableStateOf(false)
 var muted by mutableStateOf(false)
 var rotorCount by mutableIntStateOf(0)
 var rotorAngle by mutableFloatStateOf(0f)
 var openRequested by mutableStateOf(false)
 var altitude by mutableFloatStateOf(0f)
 var airborne by mutableStateOf(false)
 var error by mutableStateOf<String?>(null)
 var ready by mutableStateOf(false)
 var calibrate by mutableIntStateOf(0)
 var status by mutableStateOf("正在加载无人机")
 var eyeProgress by mutableFloatStateOf(0f)
}
private fun Context.host():Activity?=when(this){is Activity->this;is ContextWrapper->baseContext.host();else->null}
@Composable fun DroneControls() {
 val nav=LocalSpatialNavigator.current; val scope=rememberCoroutineScope();val context=LocalContext.current
 Column(Modifier.fillMaxSize().padding(32.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
  Text("FPV 无人机 · 操作窗口")
  Text(DroneSession.status)
  Text("头部向左 / 右转控制左右，低头前进、抬头后退。先保持正视并校准。")
  Text("凝视无人机 2 秒起飞 · ${(DroneSession.eyeProgress*100).toInt()}%")
  Button(onClick={DroneSession.calibrate++}) {Text("校准头部中立位置")}
  Button(onClick={DroneSession.airborne=!DroneSession.airborne},enabled=DroneSession.ready) {Text(if(DroneSession.airborne) "降落" else "起飞")}
  Button(onClick={context.host()?.let {ActivityCompat.requestPermissions(it,arrayOf("com.picovr.permission.EYE_TRACKING"),2402)}}) {Text("启用眼动凝视")}
  Button(onClick={DroneSession.simulatedFocus=!DroneSession.simulatedFocus}) {Text(if(DroneSession.simulatedFocus) "手动模拟专注：高 · 点击关闭" else "手动模拟专注：关闭 · 点击加速")}
  Button(onClick={DroneSession.muted=!DroneSession.muted}) {Text(if(DroneSession.muted) "声音：关闭" else "声音：开启")}
  Text("真实脑电接口：MindSoul 未连接；手动模拟不代表测量值")
  Button(onClick={DroneSession.airborne=false;scope.launch {nav.closeStage()}}) {Text("返回火箭")}
  Text("由成都市脑云矩阵科技有限公司提供")
  Text("开源 · 非商业演示用途；保留第三方原许可")
 }
}
@Composable fun DroneScreen() {
 val context=LocalContext.current;val nav=LocalSpatialNavigator.current
 val audio=remember {DroneAudio()}
 LaunchedEffect(audio) {audio.run()}
 val rotors=remember {mutableListOf<Entity>()}
 val head=remember {HMDTrackingProvider()}; val eyes=remember {EyeTrackingProvider()};val dwell=remember {GazeDwell()}
 var headReceived by remember {mutableLongStateOf(0L)}
 LaunchedEffect(head) {head.dataFlow.collect {headReceived=android.os.SystemClock.elapsedRealtime()}}
 var root by remember {mutableStateOf<Entity?>(null)};var drone by remember {mutableStateOf<Entity?>(null)}
 var base by remember {mutableStateOf(Vector3(0f,0f,-2f))}
 var x by remember {mutableFloatStateOf(0f)};var y by remember {mutableFloatStateOf(0f)};var z by remember {mutableFloatStateOf(0f)}
 DisposableEffect(Unit) {
  DroneSession.airborne=false;DroneSession.ready=false;DroneSession.error=null;DroneSession.simulatedFocus=false
  runCatching {head.start()}.onFailure {DroneSession.status="头部追踪不可用"}
  onDispose {head.stop();eyes.stop();root?.destroy();DroneSession.ready=false;DroneSession.airborne=false;DroneSession.eyeProgress=0f}
 }
 LaunchedEffect(Unit) {nav.openWindowContainer("drone-control",tag="flight-controls")}
 LaunchedEffect(Unit) {
  var neutral:Vector3?=null;var calibration=-1;var eyeStarted=false;var vx=0f;var vz=0f
  var last=android.os.SystemClock.elapsedRealtime()
  while(true) {
   delay(16);val now=android.os.SystemClock.elapsedRealtime();val dt=((now-last)/1000f).coerceIn(0f,0.1f);last=now
   val owner=root ?: continue
   val h=head.latestData
   val fresh=h!=null && now-headReceived<250
   var inputX=0f;var inputZ=0f
   if(fresh && h!=null) {
    val direction=owner.convertRotationFrom(h.hmdPose.rotation,null).rotateVector(Vector3(0f,0f,-1f))
    if(neutral==null || calibration!=DroneSession.calibrate) {neutral=direction;calibration=DroneSession.calibrate}
    fun axis(v:Float)=if(abs(v)<0.07f) 0f else (v*3f).coerceIn(-1f,1f)
    inputX=axis(direction.x-neutral!!.x);inputZ=axis(direction.y-neutral!!.y)
   }
   // Bounded native adaptation of splatfpv's substepped acceleration / drag integration (MIT).
   var remaining=dt
   while(remaining>0f) {
    val step=min(remaining,1f/240f);remaining-=step
    val flying=DroneSession.airborne && fresh
    val focus=if(DroneSession.simulatedFocus) 1f else DroneSession.focusSource.currentFocus()?.takeIf {it.isFinite()}?.coerceIn(0f,1f) ?: 0f
    val acceleration=2f*(1f+focus)
    vx+=((if(flying) inputX*acceleration else 0f)-vx*3f)*step
    vz+=((if(flying) inputZ*acceleration else 0f)-vz*3f)*step
    x=(x+vx*step).coerceIn(-1.5f,1.5f);z=(z+vz*step).coerceIn(-1f,1f)
    val target=if(DroneSession.airborne) 1.1f else 0f
    y+=(target-y).coerceIn(-0.6f*step,0.6f*step)
   }
   DroneSession.altitude=y
   val motorPower=if(DroneSession.airborne || y>0.02f) (if(DroneSession.simulatedFocus) 1f else 0.5f) else 0f
   audio.power=motorPower;audio.muted=DroneSession.muted
   DroneSession.rotorAngle=(DroneSession.rotorAngle+dt*motorPower*65f)%(2f*PI.toFloat())
   rotors.forEachIndexed {i,rotor -> rotor.components.get<TransformComponent>()?.quaternion=com.pico.spatial.core.math.Quat(Vector3(0f,1f,0f),DroneSession.rotorAngle*if(i%2==0) 1f else -1f)}
   drone?.components?.get<TransformComponent>()?.position=Vector3(base.x+x,base.y+y,base.z+z)
   val permission=ContextCompat.checkSelfPermission(context,"com.picovr.permission.EYE_TRACKING")==PackageManager.PERMISSION_GRANTED
   if(permission && !eyeStarted) runCatching {eyes.start()}.onSuccess {eyeStarted=true}
   val e=if(eyeStarted) eyes.latestData else null
   val eyeFresh=e!=null && abs(java.lang.System.currentTimeMillis()-e.timestamp)<250
   var hit=false
   if(eyeFresh && e!=null && drone!=null) {
    val bounds=drone!!.getVisualBounds(owner,recursive=true,enabledOnly=true)
    val origin=owner.convertPositionFrom(e.eyePose.position,null)
    val direction=owner.convertRotationFrom(e.eyePose.rotation,null).rotateVector(Vector3(0f,0f,-1f))
    if(abs(direction.z)>0.001f) {val t=(bounds.center.z-origin.z)/direction.z
     hit=t>0 && origin.x+t*direction.x in bounds.min.x..bounds.max.x && origin.y+t*direction.y in bounds.min.y..bounds.max.y}
   }
   if(dwell.update(hit && !DroneSession.airborne,eyeFresh,dt) && DroneSession.ready) DroneSession.airborne=true
   DroneSession.eyeProgress=dwell.seconds/2f
   DroneSession.status=DroneSession.error ?: if(!DroneSession.ready) "正在加载模型" else if(!fresh) "头部追踪无有效数据 · 水平运动已停止" else if(DroneSession.airborne) "飞行中 · 高度 ${"%.1f".format(y)} m" else "待命 · 凝视无人机或点击起飞"
  }
 }
 SpatialView(modifier=Modifier.fillMaxSize(),attachments={
  AttachmentPanel("controls") {Button(onClick={nav.openWindowContainer("drone-control",tag="flight-controls")}) {Text("打开无人机操作窗口")}}
 },initial={content,attachments ->
  val owner=Entity();root=owner;content.addEntity(owner)
  attachments.entity("controls")?.let {owner.addChild(it);it.components.get<TransformComponent>()?.position=Vector3(0f,1.7f,-2f)}
  try {
   val model=Entity.loadSuspend("asset://drone.glb");owner.addChild(model)
   repeat(4) {i -> model.findEntity("Rotor$i")?.let {rotors.add(it)} };DroneSession.rotorCount=rotors.size
   val b=model.getVisualBounds(owner,recursive=true,enabledOnly=true);val scale=0.65f/max(b.size.x,b.size.z)
   base=Vector3(-b.center.x*scale,-b.min.y*scale,-2f-b.center.z*scale)
   model.components.get<TransformComponent>()?.apply {scaleVector=Vector3(scale,scale,scale);position=base};drone=model
   val rocket=Entity.loadSuspend("asset://starship.glb");owner.addChild(rocket)
   val rb=rocket.getVisualBounds(owner,recursive=true,enabledOnly=true);val rs=1.2f/rb.size.y
   rocket.components.get<TransformComponent>()?.apply {scaleVector=Vector3(rs,rs,rs);position=Vector3(-1.2f-rb.center.x*rs,-rb.min.y*rs,-2.5f-rb.center.z*rs)}
   DroneSession.ready=true
  } catch(e:Exception) {android.util.Log.e("DroneScene","Model load failed",e);DroneSession.error="模型加载失败：${e.message}"}
 },update={_,_->})
}
