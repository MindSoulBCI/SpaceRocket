package com.example.spacerocket.p9d574e9f
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.spacerocket.p9d574e9f.ui.DroneSession
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
@RunWith(AndroidJUnit4::class)
class DroneRuntimeTest {
 @Test fun nativeDroneLoadsAndTakesOff() {
  val ins=InstrumentationRegistry.getInstrumentation();val context=ins.targetContext
  context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
  Thread.sleep(6000)
  ins.runOnMainSync {DroneSession.openRequested=true}
  val until=System.currentTimeMillis()+60000
  while(!DroneSession.ready && System.currentTimeMillis()<until) Thread.sleep(100)
  assertTrue("GLB model and parked rocket must load in native Stage: ${DroneSession.status}",DroneSession.ready)
  assertEquals("Four independent rotor nodes",4,DroneSession.rotorCount)
  ins.runOnMainSync {DroneSession.airborne=true}
  Thread.sleep(3000)
  assertTrue("Rotors must animate",DroneSession.rotorAngle!=0f)
  assertTrue("Drone must rise",DroneSession.altitude>0.9f)
  ins.runOnMainSync {DroneSession.airborne=false}
  Thread.sleep(3000)
  assertTrue("Drone must land",DroneSession.altitude<0.05f)
 }
}
