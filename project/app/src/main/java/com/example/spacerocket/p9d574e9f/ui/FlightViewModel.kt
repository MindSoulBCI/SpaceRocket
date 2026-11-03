package com.example.spacerocket.p9d574e9f.ui
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.example.spacerocket.p9d574e9f.domain.model.*
import com.example.spacerocket.p9d574e9f.domain.usecase.AdvanceFlightUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlightViewModel(
 private val readInput:()->Float?,
 private val writeInput:(Float)->Unit = {},
 private val holdInput:(Boolean)->Unit = {},
 private val advanceInput:(Float)->Unit = {},
 private val resetInput:()->Unit = {},
): ViewModel() {
 private var pendingLaunch=false
 fun requestLaunch() { pendingLaunch=true }
 private var prefs: SharedPreferences?=null
 private var clock:Job?=null
 private val visible=mutableSetOf<String>()
 fun attachStorage(context:Context) {
  if(prefs!=null) return
  prefs=context.applicationContext.getSharedPreferences("launch_history",Context.MODE_PRIVATE)
  _uiState.value=FlightUiState(_uiState.value.flight.copy(launchedCount=prefs!!.getInt("count",0)))
 }
 fun setVisible(surface:String,value:Boolean) {
  if(value) visible.add(surface) else visible.remove(surface)
  if(value && pendingLaunch) { pendingLaunch=false;onEvent(FlightEvent.Start) }
  if(visible.isNotEmpty() && clock==null) clock=viewModelScope.launch {
   var last=android.os.SystemClock.elapsedRealtime()
   while(true) {
    delay(50)
    val now=android.os.SystemClock.elapsedRealtime()
    var remaining=((now-last)/1000f).coerceIn(0f,0.5f);last=now
    while(remaining>0f) {val step=remaining.coerceAtMost(0.05f);onEvent(FlightEvent.Tick(step));remaining-=step}
   }
  }
  if(visible.isEmpty()) { clock?.cancel();clock=null;onEvent(FlightEvent.Pause) }
 }
 private val advance=AdvanceFlightUseCase()
 private val _uiState=MutableStateFlow(FlightUiState(FlightState(focus=readInput() ?: 0f)))
 val uiState: StateFlow<FlightUiState> = _uiState.asStateFlow()
 fun onUiEvent(event:FlightUiEvent) { when(event) { is FlightUiEvent.Action -> onEvent(event.event) } }
 fun onEvent(event:FlightEvent) {
  var state=_uiState.value.flight
  if(event is FlightEvent.SetFocus) writeInput(event.value)
  if(event is FlightEvent.HoldFocus) holdInput(event.held && (state.active || state.phase==Phase.READY))
  if(event in listOf(FlightEvent.Pause,FlightEvent.Retry,FlightEvent.InputLost,FlightEvent.AssetError)) {
   holdInput(false);state=state.copy(engaged=false)
  }
  if(event==FlightEvent.Retry) { resetInput();state=state.copy(focus=readInput() ?: 0f) }
  if(event is FlightEvent.Tick && (state.active || state.phase==Phase.READY)) {
   advanceInput(event.seconds)
   val input=readInput()
   state=advance(state,if(input==null) FlightEvent.InputLost else FlightEvent.SetFocus(input))
  }
  val oldCount=state.launchedCount
  state=advance(state,event)
  if(state.launchedCount!=oldCount) prefs?.edit()?.putInt("count",state.launchedCount)?.apply()
  if(!state.active && state.phase!=Phase.READY) { holdInput(false);state=state.copy(engaged=false) }
  _uiState.value=FlightUiState(state)
 }
}
