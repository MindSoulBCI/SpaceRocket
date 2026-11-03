package com.example.spacerocket.p9d574e9f.ui.components
import com.example.spacerocket.p9d574e9f.domain.model.*
object FlightCopy {
 val labels=listOf("准备发射","点火倒计时","火箭上升","悬停","返回着陆","安全着陆","任务中止","任务暂停","输入中断","资源加载失败")
 val instructions=listOf("点击发射，3 秒点火后自动起飞。无需连接脑电设备。","发动机点火中，倒计时结束后起飞。","三台发动机工作，正在上升。","到达目标高度，准备返回。","反推减速，正在着陆。","已完成起降，可再次发射。","请重新发射。","飞行已暂停。","输入暂不可用。","请重试加载模型。")
 fun result(s:FlightState)=when(s.phase) {
  Phase.CHARGING -> "倒计时 ${kotlin.math.ceil((3f-s.elapsed).coerceAtLeast(0f)).toInt()} 秒"
  Phase.LANDED -> "任务完成 · 安全着陆"
  else -> "已起飞 ${s.launchedCount} 次 · 最近三次展示在火箭右后方"
 }
}
