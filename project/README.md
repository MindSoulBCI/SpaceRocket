# 空间火箭 0.3.1 · PICO OS6

原生 Kotlin / PICO Spatial SDK 6.1.9。应用 ID `com.example.spacerocket.p9d574e9f`，versionCode 5。

## 使用
- 点击「发射火箭」后点火倒计时 3 秒，自动上升、悬停和降落；不需要专注数值或脑电连接。
- 主火箭显示比例约为旧版的 1.93 倍（相同默认窗口姿态下的运行时缩放比）。
- 右后方最多三枚历史火箭，分别以蓝、紫、绿色底座标记。实际离开发射台后计数，本机保存；无历史时不显示假记录。
- MindSoul 32 通道脑电看板是独立 PICO OS 窗口，可移动、关闭，并从控制窗口「显示脑电窗口／隐藏脑电窗口」操作。默认「蓝牙 · 未连接」，没有设备协议，不扫描、不配对、不生成虚构波形。
- 「凝视发射」进入独立 Mixed Stage（Full Space 能力域），使用 EyeTrackingProvider。授权后连续凝视发射按钮 2 秒触发；离开目标或超过 250ms 无新追踪样本即取消。模拟器无有效眼动时提示，无鼠标模拟眼动。可点击按钮作为替代入口。

## 发射修复
旧版要求 65–85 操作数值，否则开始后停留蓄能。新版取消此门槛，使用倒计时自动起飞。
飞行时钟由应用共享，按 STARTED 可见性而非 RESUMED 焦点运行，跨窗口切换不暂停；所有飞行窗口隐藏时暂停。用单调时钟补偿调度延迟。

## 上游素材和效果
原始模型与粒子图来自 https://github.com/elricdog/SpaceX-StarShip ，版本 `4d017fc53ef13d15fca58453b3f26338d5fccc16`。
保留 starship.glb、particle1.png、particle2.png 与 GPL-3.0/粒子来源说明。
Three.js 粒子脚本未嵌入 WebView，使用 PICO 原生动态贴图粒子重实现三路喷射、颜色渐变和透明度衰减。0.3.1 加强暗贴图的辐射亮度、双面渲染并关闭 tone mapping。
Editor 工作流因 schema 不兼容失败，采用明确标注的原生 sprite pool fallback；不声称与上游着色器逐像素等效。

## 构建
JDK21，Android SDK35：
```
./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest
```
APK: `app/build/outputs/apk/debug/app-debug.apk`。

## 验证边界
15 项 JVM 测试通过。运行时完整起降测试在 `LaunchRuntimeTest`，测试通过与否以随包验证记录为准。
真实眼动必须在支持硬件上验证。当前模拟器不支持 `screencap --spatial`；日志和测试不等同于视觉验收。用户需确认喷焰可见性与窗口布局。

## 0.4.0 无人机模式
新增 Mixed Stage 原生 FPV 无人机（用户提供 CC0 GLB），独立 drone-control 操作窗口，头部中立校准、左右转头横移、低头前进/抬头后退、真实眼动凝视模型 2 秒起飞、点击起降。火箭在无人机 Stage 地面保留。水平范围限制为 ±1.5m / ±1m，失去头部数据停止水平加速。手动模拟专注明确标记，MindSoulFocusSource 保留真实设备适配接口。

参考 Rouf0x/splatfpv (bbdda7dde1cfeb7189ed8bbd14840469afe64b76, MIT) 的固定子步积分思路；本版没有移植 Web Gaussian Splat 渲染器、地图或完整 FPV 物理。对应 MIT 文本保存在 assets/licenses。

32 通道独立 EEG 窗口展示逐通道时间网格。MindSoul 蓝牙协议未提供，保持未连接，无虚构曲线。邮箱验证码登录与邮箱密码注册为两个独立 OS 窗口；按用户确认，后台后接，提交按钮禁用，密码遮蔽且不落盘。

由成都市脑云矩阵科技有限公司提供。界面“非商业演示用途”为本演示用途说明，不改变第三方授权：既有 SpaceX-StarShip GPL-3.0、splatfpv MIT、无人机 CC0 保持各自许可；并非向这些开源许可证增加非商业限制。

## 0.5.0 入口与空间展开
默认 Planar 欢迎窗口 home：APP Logo、“空间火箭”、“每个人都能玩的空间玩具”。注册/登录为独立窗口，后台未接入，只有明确的体验模式可以进入主看板。主看板选择后才打开对应火箭窗口或无人机 Stage。

使用稳定 ID 锚点，不再跟随焦点布置。home 正前方、rocket-scene 左侧160dp、mission-control 在 rocket-scene 下方120dp、eeg-monitor 在 home 右侧160dp。注册右、登录左，互斥按需打开。用户后续仍能移动窗口。

无人机衍生 GLB 将 Propellers 的1296个三角面拆成4个独立 Rotor 节点，每个324面，围绕各自电机中心旋转。起飞和降落途中旋转、落地停止。音效参考 splatfpv 的程序合成方式，以原生 AudioTrack 生成四电机谐波和滤波风噪，提供静音按钮；并非从上游提取录音文件。
