package com.example.spacerocket.p9d574e9f.ui.scene

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.ModelEntity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.BoundingBox
import com.pico.spatial.core.ecs.resource.MeshResource
import com.pico.spatial.core.ecs.resource.*
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.ui.foundation.content.SpatialView
import com.example.spacerocket.p9d574e9f.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlin.math.*

private class SceneState {
 var owner:Entity?=null
 var root:Entity?=null
 var rocket:Entity?=null
 val flames=mutableListOf<Pair<Entity,UnlitMaterial>>()
 val history=mutableListOf<Entity>()
 var supportY=0f
 var modelWidth=0f
 var lastPhase:Phase?=null
 var launch=Vector3.ZERO
 var travel=0f
 var flameHeight=0f
 var base:BoundingBox?=null
 var sizeKey:IntSize?=null
 var failed=false
 var disposed=false
}
private fun Entity.transform() = checkNotNull(components.get<TransformComponent>()) { "Missing TransformComponent" }
private fun union(a:BoundingBox,b:BoundingBox):BoundingBox {
 val lo=Vector3(min(a.min.x,b.min.x),min(a.min.y,b.min.y),min(a.min.z,b.min.z))
 val hi=Vector3(max(a.max.x,b.max.x),max(a.max.y,b.max.y),max(a.max.z,b.max.z))
 return BoundingBox((lo+hi)*0.5f,(hi-lo)*0.5f)
}
private fun contains(a:BoundingBox,b:BoundingBox):Boolean {
 val e=0.001f
 return b.min.x>=a.min.x-e && b.max.x<=a.max.x+e && b.min.y>=a.min.y-e && b.max.y<=a.max.y+e && b.min.z>=a.min.z-e && b.max.z<=a.max.z+e
}
@Composable fun RocketScene(state:FlightState,windowSize:IntSize,modifier:Modifier,onEvent:(FlightEvent)->Unit) {
 val scene=remember { SceneState() }
 val dispatch by rememberUpdatedState(onEvent)
 var loaded by remember { mutableStateOf(false) }
 SpatialView(modifier=modifier,initial={content,_->
  try {
   val owner=Entity(); val root=Entity()
   scene.owner=owner;scene.root=root
   content.addEntity(owner);owner.addChild(root)
   val rocket=Entity.loadSuspend("asset://starship.glb")
   if(scene.disposed) { rocket.destroy(); return@SpatialView }
   root.addChild(rocket);scene.rocket=rocket
   val native=rocket.getVisualBounds(root,recursive=true,enabledOnly=true)
   check(!native.isEmpty() && native.size.y>0f) { "Empty rocket bounds" }
   // glTF source inspection: Y-up; 18.696 x 50.644 x 9.202m. Preserve authored orientation.
   val radius=max(native.size.x,native.size.z)*0.7f
   val thickness=native.size.y*0.025f
   val pad=ModelEntity(MeshResource.createCylinder(height=thickness,radius=radius),UnlitMaterial.create().apply { setBaseColor(Color4(0.788f,0.361f,0.153f,1f)) })
   root.addChild(pad)
   val support=pad.getVisualBounds(root,recursive=true,enabledOnly=true)
   scene.launch=Vector3(support.center.x-native.center.x,support.max.y-native.min.y,support.center.z-native.center.z)
   rocket.transform().position=scene.launch
   val contact=rocket.getVisualBounds(root,recursive=true,enabledOnly=true)
   check(abs(contact.min.y-support.max.y)<0.001f) { "Rocket pad contact mismatch" }
   check(contact.min.x>=support.min.x && contact.max.x<=support.max.x && contact.min.z>=support.min.z && contact.max.z<=support.max.z) { "Rocket footprint outside pad" }
   scene.supportY=support.max.y
   scene.modelWidth=native.size.x
   scene.travel=native.size.y*0.6f
   val high=BoundingBox(contact.center+Vector3(0f,scene.travel,0f),contact.halfExtent)
   scene.base=union(union(support,contact),high)
   val flameHeight=native.size.y*0.24f
   scene.flameHeight=flameHeight
   // Preserve upstream flame image and additive rendering; bounded reusable sprite pool.
   val texture=TextureResource.load("effects/particle2.png")
   val mesh=MeshResource.createPlane(native.size.x*0.30f,native.size.y*0.10f)
   repeat(36) {
    val material=UnlitMaterial.create().apply {
     setBaseColorTexture(texture);setBlendingMode(BlendingMode.ADD);setDepthWrite(false)
     setCullingMode(MaterialCullingMode.NONE);setApplyToneMapping(false)
     setBaseColor(Color4(1f,0.35f,0.08f,1f))
    }
    val flame=ModelEntity(mesh,material);root.addChild(flame);flame.enabled=false
    scene.flames.add(flame to material)
   }
   // Reserve the complete motion/effect envelope so scale does not jump at ignition.
   scene.base=union(scene.base!!,BoundingBox(Vector3(0f,support.max.y-flameHeight*0.5f,0f),Vector3(native.size.x*0.4f,flameHeight*0.6f,native.size.z)))
   val colors=listOf(Color4(0.15f,0.65f,0.85f,1f),Color4(0.60f,0.35f,0.85f,1f),Color4(0.18f,0.72f,0.45f,1f))
   repeat(3) { i ->
    val group=Entity();root.addChild(group)
    val copy=Entity.loadSuspend("asset://starship.glb")
    if(scene.disposed) { copy.destroy();return@SpatialView }
    group.addChild(copy)
    val copyScale=0.38f
    copy.transform().scaleVector=Vector3(copyScale,copyScale,copyScale)
    val cb=copy.getVisualBounds(group,recursive=true,enabledOnly=true)
    val disk=ModelEntity(MeshResource.createCylinder(height=thickness,radius=radius*copyScale),UnlitMaterial.create().apply {setBaseColor(colors[i])})
    group.addChild(disk)
    val db=disk.getVisualBounds(group,recursive=true,enabledOnly=true)
    copy.transform().position=Vector3(-cb.center.x,db.max.y-cb.min.y,-cb.center.z)
    group.transform().position=Vector3(radius+native.size.x*0.45f+i*native.size.x*0.62f,0f,-radius*1.35f)
    scene.base=union(scene.base!!,group.getVisualBounds(root,recursive=true,enabledOnly=true))
    group.enabled=false;scene.history.add(group)
   }
   loaded=true
   Log.i("SpaceRocketScene","asset_loaded source=starship.glb nativeBounds=$native contact=PASS")
  } catch(e:CancellationException) { throw e }
  catch(e:Exception) { scene.failed=true;Log.e("SpaceRocketScene","asset_error",e);dispatch(FlightEvent.AssetError) }
 },update=update@{content,_->
  if(!loaded || scene.failed || scene.disposed) return@update
  val owner=scene.owner ?: return@update
  val root=scene.root ?: return@update
  val rocket=scene.rocket ?: return@update
  val base=scene.base ?: return@update
  if(windowSize==IntSize.Zero) return@update
  try {
   val regions=content.measureBoundedRegions(windowSize) ?: return@update
   val lo=Vector3(max(regions.spatialView.min.x,regions.windowContainer.min.x),max(regions.spatialView.min.y,regions.windowContainer.min.y),max(regions.spatialView.min.z,regions.windowContainer.min.z))
   val hi=Vector3(min(regions.spatialView.max.x,regions.windowContainer.max.x),min(regions.spatialView.max.y,regions.windowContainer.max.y),min(regions.spatialView.max.z,regions.windowContainer.max.z))
   check(hi.x>lo.x && hi.y>lo.y && hi.z>lo.z) { "Empty safe intersection" }
   val safe=BoundingBox((lo+hi)*0.5f,(hi-lo)*0.5f)
   val scale=minOf(safe.size.x/base.size.x,safe.size.y/base.size.y,safe.size.z/base.size.z)*0.88f
   check(scale.isFinite() && scale>0f)
   val translation=safe.center-base.center*scale
   root.transform().scaleVector=Vector3(scale,scale,scale)
   root.transform().position=translation
   rocket.transform().position=scene.launch+Vector3(0f,scene.travel*(state.altitude/30f),0f)
   val rb=rocket.getVisualBounds(root,recursive=true,enabledOnly=true)
   val ignited=state.active
   scene.flames.forEachIndexed { index,pair ->
    val (flame,material)=pair
    flame.enabled=ignited
    if(ignited) {
     val engine=index%3
     val age=((state.flightTime*1.8f+index/36f)%1f)
     val angle=engine*2f*PI.toFloat()/3f
     val radius=scene.modelWidth*0.10f
     val x=rb.center.x+cos(angle)*radius+sin(index*2.4f+state.flightTime*7f)*age*radius*0.5f
     val z=rb.center.z+sin(angle)*radius
     val y=rb.min.y-age*scene.flameHeight
     flame.transform().position=Vector3(x,y,z)
     val size=(0.45f+sin(age*PI.toFloat())*0.65f)
     flame.transform().scaleVector=Vector3(size,size,1f)
     material.setBaseColor(Color4(12f,3f+age*6f,0.5f+age*2f,1f))
     material.setOpacity((1f-age)*0.95f)
    }
   }
   scene.history.forEachIndexed { index,entity -> entity.enabled=index<state.launchedCount.coerceAtMost(3) }
   if(scene.lastPhase!=state.phase) {
    Log.i("SpaceRocketFlight","phase=${state.phase} altitude=${state.altitude} launches=${state.launchedCount} flames=$ignited texture=effects/particle2.png sprites=36 gain=12")
    scene.lastPhase=state.phase
   }
   val finalVisualBounds=root.getVisualBounds(owner,recursive=true,enabledOnly=true)
   check(contains(safe,finalVisualBounds)) { "Flight outside safe region" }
   if(scene.sizeKey!=windowSize) {
    val planned=BoundingBox(base.center*scale+translation,base.halfExtent*scale)
    Log.i("SpaceRocketScene","boundsOwner=placementOwner revision=$windowSize baseDesignEnvelope=$base safeRegion=$safe uniformScale=$scale translation=$translation finalPlannedBounds=$planned finalVisualBounds=$finalVisualBounds numericStatus=PASS visualStatus=UNOBSERVED")
    scene.sizeKey=windowSize
   }
   if(!state.sceneReady) dispatch(FlightEvent.SceneReady)
  } catch(e:Exception) { scene.failed=true;Log.e("SpaceRocketScene","placement_error",e);dispatch(FlightEvent.AssetError) }
 })
 DisposableEffect(Unit) { onDispose { scene.disposed=true;scene.owner?.destroy();scene.owner=null;scene.root=null;scene.rocket=null;scene.flames.clear();scene.history.clear() } }
}
