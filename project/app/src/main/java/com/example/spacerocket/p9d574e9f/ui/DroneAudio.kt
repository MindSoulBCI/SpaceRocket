package com.example.spacerocket.p9d574e9f.ui
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.*
/** Native procedural motor/wind adaptation of splatfpv audio-engine.js (MIT, Roufox 2026). */
class DroneAudio {
 @Volatile var power=0f
 @Volatile var muted=false
 suspend fun run()=withContext(Dispatchers.IO) {
  val rate=24000
  val track=AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
   .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
   .setBufferSizeInBytes(max(4096,AudioTrack.getMinBufferSize(rate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT))).setTransferMode(AudioTrack.MODE_STREAM).build()
  val phases=DoubleArray(4);val pcm=ShortArray(480);var level=0.0;var noise=0.0
  try {track.play();while(isActive) {
   val target=if(muted) 0.0 else power.toDouble()
   for(i in pcm.indices) {
    level+=(target-level)*0.001
    var sample=0.0
    for(m in 0..3) {phases[m]=(phases[m]+(260+720*level)*(1+(m-1.5)*0.003)/rate)%1.0
     sample+=(sin(phases[m]*2*PI)+0.25*sin(phases[m]*4*PI))/4}
    noise=noise*0.94+(Math.random()*2-1)*0.06
    pcm[i]=((sample*0.10+noise*0.08)*level*32767).toInt().coerceIn(-32767,32767).toShort()
   }
   track.write(pcm,0,pcm.size)
  }} finally {track.stop();track.release()}
 }
}
