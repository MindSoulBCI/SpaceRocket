package com.example.spacerocket.p9d574e9f.ui
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.pico.spatial.ui.platform.containers.StageStyle
import com.example.spacerocket.p9d574e9f.R
import kotlinx.coroutines.launch
object AppEntry {var preview by mutableStateOf(false);var rocketRequested by mutableStateOf(false)}
@Composable fun WelcomeScreen() {
 val nav=LocalSpatialNavigator.current;val scope=rememberCoroutineScope()
 LaunchedEffect(AppEntry.rocketRequested) {
  if(AppEntry.rocketRequested) {nav.closeWindowContainer("drone-control",tag="flight-controls");nav.openWindowContainer("rocket-scene",tag="rocket");AppEntry.rocketRequested=false}
 }
 LaunchedEffect(DroneSession.openRequested) {
  if(DroneSession.openRequested) {nav.openStage("drone-flight",style=StageStyle.Mixed);DroneSession.openRequested=false}
 }
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(40.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
  Image(painterResource(R.mipmap.ic_spatial_launcher),contentDescription="空间火箭 Logo",modifier=Modifier.size(128.dp))
  Text("空间火箭",fontSize=36.sp)
  Text("每个人都能玩的空间玩具",fontSize=22.sp)
  if(!AppEntry.preview) {
   Text("欢迎来到你的空间游乐场")
   Button(onClick={nav.closeWindowContainer("email-login");nav.openWindowContainer("email-register")}) {Text("邮箱注册")}
   Button(onClick={nav.closeWindowContainer("email-register");nav.openWindowContainer("email-login")}) {Text("邮箱验证码登录")}
   Text("账号服务尚未接入，可先体验功能。")
   Button(onClick={AppEntry.preview=true}) {Text("进入体验模式")}
  } else {
   Text("主看板 · 体验模式")
   Button(onClick={AppEntry.rocketRequested=true}) {Text("选择火箭 · 打开火箭窗口")}
   Button(onClick={nav.closeWindowContainer("rocket-scene",tag="rocket");nav.closeWindowContainer("mission-control",tag="flight-controls");scope.launch {nav.openStage("drone-flight",style=StageStyle.Mixed)}}) {Text("选择无人机 · 打开飞行窗口")}
   Button(onClick={nav.openWindowContainer("eeg-monitor",tag="mindsoul-eeg")}) {Text("打开脑电看板 · 右侧")}
   Button(onClick={AppEntry.preview=false;nav.closeWindowContainer("rocket-scene",tag="rocket");nav.closeWindowContainer("mission-control",tag="flight-controls");nav.closeWindowContainer("eeg-monitor",tag="mindsoul-eeg")}) {Text("返回欢迎页")}
  }
  Text("由成都市脑云矩阵科技有限公司提供")
  Text("开源 · 非商业演示用途；第三方许可保留")
 }
}
