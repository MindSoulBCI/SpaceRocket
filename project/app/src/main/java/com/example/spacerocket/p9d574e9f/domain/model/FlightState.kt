package com.example.spacerocket.p9d574e9f.domain.model

enum class Phase { READY, CHARGING, ASCENDING, HOLDING, DESCENDING, LANDED, FAILED, PAUSED, INPUT_LOST, ASSET_ERROR }
data class FlightState(
 val phase: Phase = Phase.READY, val previousPhase: Phase = Phase.READY,
 val focus: Float = 75f, val altitude: Float = 0f, val velocity: Float = 0f,
 val elapsed: Float = 0f, val unstable: Float = 0f, val attempt: Int = 0,
 val launchedCount: Int = 0, val recorded: Boolean = false, val flightTime: Float = 0f,
 val sceneReady: Boolean = false, val engaged: Boolean = false,
) {
 val active get() = phase in setOf(Phase.CHARGING, Phase.ASCENDING, Phase.HOLDING, Phase.DESCENDING)
 val target get() = if (phase == Phase.DESCENDING || (phase == Phase.PAUSED && previousPhase == Phase.DESCENDING)) 45f..65f else 65f..85f
}
sealed interface FlightEvent {
 data class HoldFocus(val held: Boolean): FlightEvent
 data class SetFocus(val value: Float): FlightEvent
 data class Tick(val seconds: Float): FlightEvent
 data object Start: FlightEvent
 data object Pause: FlightEvent
 data object Resume: FlightEvent
 data object Retry: FlightEvent
 data object InputLost: FlightEvent
 data object AssetError: FlightEvent
 data object SceneReady: FlightEvent
}
