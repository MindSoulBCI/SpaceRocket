package com.example.spacerocket.p9d574e9f
import android.content.Intent
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.spacerocket.p9d574e9f.ui.FlightViewModelFactory
import com.example.spacerocket.p9d574e9f.domain.model.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
@RunWith(AndroidJUnit4::class)
class LaunchRuntimeTest {
 @Test fun loadedSceneLaunchesWithRealClock() {
  val ins=InstrumentationRegistry.getInstrumentation()
  val context=ins.targetContext
  context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
  Thread.sleep(3000)
  ins.runOnMainSync {com.example.spacerocket.p9d574e9f.ui.AppEntry.preview=true;com.example.spacerocket.p9d574e9f.ui.AppEntry.rocketRequested=true}
  val vm=FlightViewModelFactory.session
  val readyDeadline=System.currentTimeMillis()+25000
  while(!vm.uiState.value.flight.sceneReady && System.currentTimeMillis()<readyDeadline) Thread.sleep(100)
  assertTrue("Native model must report ready",vm.uiState.value.flight.sceneReady)
  val baseline=vm.uiState.value.flight.launchedCount
  ins.runOnMainSync {vm.onEvent(FlightEvent.Start)}
  val deadline=System.currentTimeMillis()+90000
  var moved=false;var peaked=false
  while(System.currentTimeMillis()<deadline) {
   val s=vm.uiState.value.flight
   if(s.altitude>1f)moved=true
   if(s.altitude>=29f)peaked=true
   if(s.phase==Phase.ASSET_ERROR)fail("Native scene reported asset/placement failure")
   if(s.phase==Phase.LANDED)break
   Thread.sleep(100)
  }
  assertTrue("Rocket must actually rise",moved)
  assertTrue("Rocket must reach target",peaked)
  assertEquals(Phase.LANDED,vm.uiState.value.flight.phase)
  assertEquals(baseline+1,vm.uiState.value.flight.launchedCount)
  Log.i("SpaceRocketAcceptance","PASS native scene + real clock + ascent + landing + launch history")
 }
}
