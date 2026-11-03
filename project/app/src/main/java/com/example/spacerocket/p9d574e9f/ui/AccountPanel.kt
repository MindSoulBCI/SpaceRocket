package com.example.spacerocket.p9d574e9f.ui
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.pico.spatial.ui.design.*
@Composable fun AccountPanel(register:Boolean) {
 val nav=com.pico.spatial.ui.platform.containers.LocalSpatialNavigator.current
 var email by remember {mutableStateOf("")};var secret by remember {mutableStateOf("")}
 Column(Modifier.fillMaxSize().padding(32.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
  Text(if(register) "邮箱密码注册" else "邮箱验证码登录")
  TextField(value=email,onValueChange={email=it},placeholder={Text("邮箱")},singleLine=true)
  TextField(value=secret,onValueChange={secret=it},placeholder={Text(if(register) "密码" else "邮件验证码")},singleLine=true,visualTransformation=if(register) PasswordVisualTransformation() else VisualTransformation.None)
  if(!register) Button(onClick={},enabled=false) {Text("发送邮件验证码")}
  Button(onClick={},enabled=false) {Text(if(register) "注册账号" else "登录")}
  Text("账号服务未配置 · 等待邮箱服务接口接入")
  Button(onClick={AppEntry.preview=true;nav.closeWindowContainer()}) {Text("暂不登录，进入体验模式")}
  Button(onClick={nav.closeWindowContainer()}) {Text("返回欢迎窗口")}
  Text("由成都市脑云矩阵科技有限公司提供")
  Text("开源 · 非商业演示用途；第三方素材遵循原许可")
 }
}
