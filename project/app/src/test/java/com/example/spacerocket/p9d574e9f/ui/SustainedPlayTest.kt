package com.example.spacerocket.p9d574e9f.ui
import com.example.spacerocket.p9d574e9f.data.repository.SustainedAttentionRepository
import com.example.spacerocket.p9d574e9f.domain.model.*
import org.junit.Assert.*
import org.junit.Test
class SustainedPlayTest {
 private fun create():Pair<FlightViewModel,SustainedAttentionRepository> {
  val source=SustainedAttentionRepository()
  return FlightViewModel(readInput=source::read,holdInput=source::setEngaged,advanceInput=source::advance,resetInput=source::reset) to source
 }
 @Test fun holdRisesReleaseFallsAndClamps() {
  val s=SustainedAttentionRepository();s.setEngaged(true);repeat(200){s.advance(.1f)}
  assertEquals(100f,s.read(),.001f);s.setEngaged(false);repeat(10){s.advance(.1f)}
  assertEquals(88f,s.read(),.01f);repeat(200){s.advance(.1f)};assertEquals(0f,s.read(),.001f)
 }
 @Test fun continuousOperationCanCompleteMission() {
  val (vm,_)=create();vm.onEvent(FlightEvent.SceneReady);vm.onEvent(FlightEvent.Start)
  var holding=false
  repeat(1600) {
   val s=vm.uiState.value.flight
   val low=if(s.phase==Phase.DESCENDING)48f else 68f
   val high=if(s.phase==Phase.DESCENDING)61f else 80f
   if(s.focus<low)holding=true else if(s.focus>high)holding=false
   vm.onEvent(FlightEvent.HoldFocus(holding));vm.onEvent(FlightEvent.Tick(.05f))
  }
  assertEquals(Phase.LANDED,vm.uiState.value.flight.phase)
 }
 @Test fun automaticFlightDoesNotRequireHolding() {
  val (vm,_)=create();vm.onEvent(FlightEvent.SceneReady);vm.onEvent(FlightEvent.Start)
  vm.onEvent(FlightEvent.HoldFocus(true));repeat(800){vm.onEvent(FlightEvent.Tick(.05f))}
  assertEquals(Phase.LANDED,vm.uiState.value.flight.phase)
  assertFalse(vm.uiState.value.flight.engaged)
 }
 @Test fun pauseReleasesAndFreezesInput() {
  val (vm,source)=create();vm.onEvent(FlightEvent.SceneReady);vm.onEvent(FlightEvent.Start)
  vm.onEvent(FlightEvent.HoldFocus(true));repeat(50){vm.onEvent(FlightEvent.Tick(.05f))}
  vm.onEvent(FlightEvent.Pause);val before=source.read()
  repeat(50){vm.onEvent(FlightEvent.Tick(.05f))};assertEquals(before,source.read(),0f)
  vm.onEvent(FlightEvent.Resume);vm.onEvent(FlightEvent.Tick(.05f));assertTrue(source.read()<before)
 }
 @Test fun retryClearsHeldInput() {
  val (vm,source)=create();vm.onEvent(FlightEvent.HoldFocus(true));vm.onEvent(FlightEvent.Tick(.1f))
  vm.onEvent(FlightEvent.Retry);vm.onEvent(FlightEvent.Tick(.1f));assertEquals(0f,source.read(),0f)
 }
}
