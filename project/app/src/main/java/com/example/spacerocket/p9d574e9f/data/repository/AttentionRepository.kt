package com.example.spacerocket.p9d574e9f.data.repository

/** Input adapters return a normalized game-control value, or null when unavailable.
 * Future gaze/EEG adapters implement this boundary with explicit permissions and calibration.
 * This interface does not imply a measured psychological attention score. */
interface AttentionRepository {
 fun read(): Float?
 fun setEngaged(engaged: Boolean) {}
 fun advance(seconds: Float) {}
 fun reset() {}
}
class SustainedAttentionRepository: AttentionRepository {
 private var value=0f
 private var held=false
 override fun read(): Float = value
 override fun setEngaged(engaged:Boolean) { held=engaged }
 override fun advance(seconds:Float) {
  if(!seconds.isFinite() || seconds<=0f) return
  value=(value+(if(held) 18f else -12f)*seconds.coerceAtMost(0.1f)).coerceIn(0f,100f)
 }
 override fun reset() { held=false;value=0f }
}
