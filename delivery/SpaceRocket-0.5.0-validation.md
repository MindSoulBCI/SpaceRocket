# 0.5.0 验证
- Gradle assembleDebug / testDebugUnitTest / assembleDebugAndroidTest 成功。
- DroneRuntimeTest 通过，36.023秒：原生模型加载、4个旋翼节点、旋翼角度更新、起飞和降落。
- LaunchRuntimeTest 通过，50.397秒：欢迎入口后打开火箭窗口，原生场景完成升空、到达目标、降落及历史计数。
- 已检查 SpaceRocket-0.5.0-layout.png：火箭左侧、主看板居中、操作窗口左下方，未互相堆叠；上升中的火焰喷射明确可见。
- EEG与认证窗口的完整左右视野尚未逐一截图验收。真实头部/眼动与音效听感需要头显体验验证。
- 邮箱后台按用户要求后接，目前仅体验入口可进入主看板；无虚假登录。
- pico-spatial-window-layout 已保存到全局 Codex skills 目录，quick_validate 通过。outputs 中保留可分享副本。
