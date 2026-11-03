package com.example.spacerocket.p9d574e9f.ui.components
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
@Composable fun EegPanel() {
 val navigator=LocalSpatialNavigator.current
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
  MissionText("MindSoul",32)
  MissionText("32 通道脑电看板",24)
  MissionText("蓝牙 · 未连接",20)
  MissionText("尚未接入设备协议 · 无实时数据",14)
  repeat(32) { channel ->
   MissionText("CH%02d · — μV · 暂无数据".format(channel+1),14)
   androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(48.dp)) {
    val grid=androidx.compose.ui.graphics.Color.Gray.copy(alpha=0.25f)
    repeat(5) { i -> val x=size.width*i/4f; drawLine(grid,androidx.compose.ui.geometry.Offset(x,0f),androidx.compose.ui.geometry.Offset(x,size.height)) }
    repeat(3) { i -> val y=size.height*i/2f; drawLine(grid,androidx.compose.ui.geometry.Offset(0f,y),androidx.compose.ui.geometry.Offset(size.width,y)) }
   }
  }
  MissionText("时间趋势 · 等待真实采样，不绘制虚构信号",14)
  MissionText("连接状态：未连接　采样率：—　电量：—",14)
  Button(onClick={navigator.closeWindowContainer()}) {Text("隐藏看板")}
 }
}
