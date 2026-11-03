package com.example.spacerocket.p9d574e9f.domain.usecase
/** Continuous valid gaze only; lost tracking and leaving the target reset immediately. */
class GazeDwell {
 var seconds=0f; private set
 fun update(hit:Boolean,valid:Boolean,dt:Float):Boolean {
  seconds=if(hit && valid && dt.isFinite() && dt>0f) seconds+dt.coerceAtMost(0.1f) else 0f
  return seconds>=2f
 }
}
