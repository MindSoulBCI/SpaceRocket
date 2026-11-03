package com.example.spacerocket.p9d574e9f.domain.usecase

import com.example.spacerocket.p9d574e9f.domain.model.*
import kotlin.math.max
import kotlin.math.min

class AdvanceFlightUseCase {
 operator fun invoke(s: FlightState, event: FlightEvent): FlightState = when(event) {
  is FlightEvent.HoldFocus -> s.copy(engaged=event.held && (s.active || s.phase==Phase.READY))
  is FlightEvent.SetFocus -> if(event.value.isFinite()) s.copy(focus=event.value.coerceIn(0f,100f)) else s.copy(phase=Phase.INPUT_LOST,velocity=0f)
  FlightEvent.Start -> if(s.phase==Phase.READY && s.sceneReady) s.copy(phase=Phase.CHARGING) else s
  FlightEvent.Pause -> if(s.active) s.copy(previousPhase=s.phase,phase=Phase.PAUSED,velocity=0f) else s
  FlightEvent.Resume -> if(s.phase==Phase.PAUSED) s.copy(phase=s.previousPhase) else s
  FlightEvent.Retry -> FlightState(focus=s.focus,attempt=s.attempt+1,sceneReady=false,launchedCount=s.launchedCount)
  FlightEvent.InputLost -> s.copy(phase=Phase.INPUT_LOST,velocity=0f)
  FlightEvent.AssetError -> s.copy(phase=Phase.ASSET_ERROR,sceneReady=false,velocity=0f)
  FlightEvent.SceneReady -> s.copy(sceneReady=true)
  is FlightEvent.Tick -> advance(s,event.seconds)
 }
 private fun advance(s: FlightState, dt: Float): FlightState {
  if(!s.active || !dt.isFinite() || dt<=0f) return s
  // Ignore long scheduling gaps: lifecycle pause must never fast-forward a flight.
  val step = min(dt,0.1f)
  val t=s.elapsed+step
  val stable=s.copy(unstable=0f,elapsed=t,flightTime=s.flightTime+step)
  return when(s.phase) {
   Phase.CHARGING -> if(t>=3f-0.0001f) stable.copy(phase=Phase.ASCENDING,elapsed=0f) else stable
   Phase.ASCENDING -> { val y=min(30f,s.altitude+3f*step); stable.copy(launchedCount=if(!s.recorded && y>0f) s.launchedCount+1 else s.launchedCount,recorded=true,altitude=y,velocity=if(y>=30f) 0f else 3f,phase=if(y>=30f) Phase.HOLDING else s.phase,elapsed=if(y>=30f) 0f else t) }
   Phase.HOLDING -> if(t>=4f-0.0001f) stable.copy(phase=Phase.DESCENDING,elapsed=0f) else stable
   Phase.DESCENDING -> { val y=max(0f,s.altitude-2f*step); stable.copy(altitude=y,velocity=if(y<=0f) 0f else -2f,phase=if(y<=0f) Phase.LANDED else s.phase) }
   else -> s
  }
 }
}
