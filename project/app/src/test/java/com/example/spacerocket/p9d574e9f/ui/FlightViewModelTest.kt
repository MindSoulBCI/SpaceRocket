package com.example.spacerocket.p9d574e9f.ui
import com.example.spacerocket.p9d574e9f.domain.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
class FlightViewModelTest {
 @Test fun inputLossStopsMotion()=runTest {
  var input:Float?=75f;val vm=FlightViewModel({input},{input=it})
  vm.onEvent(FlightEvent.SceneReady);vm.onEvent(FlightEvent.Start);input=null
  vm.onEvent(FlightEvent.Tick(.05f));assertEquals(Phase.INPUT_LOST,vm.uiState.value.flight.phase)
 }
 @Test fun simulationValuePropagates()=runTest {
  var input=75f;val vm=FlightViewModel({input},{input=it})
  vm.onEvent(FlightEvent.SetFocus(55f));assertEquals(55f,input,0f);assertEquals(55f,vm.uiState.value.flight.focus,0f)
 }
 @Test fun assetFailureRemainsUntilRetry()=runTest {
  val vm=FlightViewModel({75f},{})
  vm.onEvent(FlightEvent.AssetError);vm.onEvent(FlightEvent.Tick(.05f))
  assertEquals(Phase.ASSET_ERROR,vm.uiState.value.flight.phase)
  vm.onEvent(FlightEvent.Retry);assertEquals(Phase.READY,vm.uiState.value.flight.phase);assertFalse(vm.uiState.value.flight.sceneReady)
 }
 @Test fun pauseAndResumeRetainCharge()=runTest {
  val vm=FlightViewModel({75f},{})
  vm.onEvent(FlightEvent.SceneReady);vm.onEvent(FlightEvent.Start)
  repeat(30){vm.onEvent(FlightEvent.Tick(.05f))};val elapsed=vm.uiState.value.flight.elapsed
  vm.onEvent(FlightEvent.Pause);repeat(30){vm.onEvent(FlightEvent.Tick(.05f))}
  assertEquals(elapsed,vm.uiState.value.flight.elapsed,0f)
  vm.onEvent(FlightEvent.Resume);assertEquals(Phase.CHARGING,vm.uiState.value.flight.phase)
 }
}
