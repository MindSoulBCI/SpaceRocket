package com.example.spacerocket.p9d574e9f.domain.usecase
import com.example.spacerocket.p9d574e9f.domain.model.*
import org.junit.Assert.*
import org.junit.Test
class AdvanceFlightTest {
 private val rules=AdvanceFlightUseCase()
 private fun tick(s:FlightState,n:Int):FlightState { var r=s; repeat(n){r=rules(r,FlightEvent.Tick(.05f))}; return r }
 @Test fun completeLaunchAndLanding() {
  var s=rules(FlightState(sceneReady=true),FlightEvent.Start)
  s=tick(s,80);assertEquals(Phase.ASCENDING,s.phase)
  s=tick(s,201);assertEquals(Phase.HOLDING,s.phase);assertEquals(30f,s.altitude,0.001f)
  s=tick(s,80);assertEquals(Phase.DESCENDING,s.phase)
  s=rules(s,FlightEvent.SetFocus(55f));s=tick(s,301)
  assertEquals(Phase.LANDED,s.phase);assertEquals(0f,s.altitude,0.001f);assertEquals(0f,s.velocity,0.001f)
 }
 @Test fun zeroInputStillLaunchesAndCountsOnce() {
  var s=rules(FlightState(focus=0f,sceneReady=true),FlightEvent.Start)
  s=tick(s,59);assertEquals(Phase.CHARGING,s.phase);assertEquals(0,s.launchedCount)
  s=tick(s,10);assertTrue(s.altitude>0f);assertEquals(1,s.launchedCount)
  s=tick(s,800);assertEquals(Phase.LANDED,s.phase);assertEquals(1,s.launchedCount)
  s=rules(s,FlightEvent.Retry);assertEquals(1,s.launchedCount)
 }
 @Test fun gazeMustBeContinuousAndFresh() {
  val g=GazeDwell();repeat(30){assertFalse(g.update(true,true,.05f))}
  assertFalse(g.update(false,true,.05f));assertEquals(0f,g.seconds,0f)
  repeat(30){g.update(true,true,.05f)}
  assertFalse(g.update(true,false,.05f));assertEquals(0f,g.seconds,0f)
  repeat(41){g.update(true,true,.05f)};assertTrue(g.seconds>=2f)
 }
 @Test fun pausePreservesDescendingTargetAndProgress() {
  val s=FlightState(phase=Phase.DESCENDING,focus=55f,altitude=20f,elapsed=3f,sceneReady=true)
  val paused=rules(s,FlightEvent.Pause);assertEquals(45f..65f,paused.target)
  assertEquals(paused,tick(paused,200));val resumed=rules(paused,FlightEvent.Resume)
  assertEquals(3f,resumed.elapsed,0.001f);assertEquals(Phase.DESCENDING,resumed.phase)
 }
 @Test fun assetRequiredAndRetryReloadsAsset() {
  assertEquals(Phase.READY,rules(FlightState(),FlightEvent.Start).phase)
  val retry=rules(FlightState(phase=Phase.ASSET_ERROR),FlightEvent.Retry)
  assertFalse(retry.sceneReady);assertEquals(1,retry.attempt)
 }
}
