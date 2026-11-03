package com.example.spacerocket.p9d574e9f.ui
import com.example.spacerocket.p9d574e9f.domain.model.FlightState
import com.example.spacerocket.p9d574e9f.domain.model.FlightEvent
/** Presentation state is distinct from the pure flight domain. */
data class FlightUiState(val flight: FlightState = FlightState())
sealed interface FlightUiEvent { data class Action(val event: FlightEvent): FlightUiEvent }
