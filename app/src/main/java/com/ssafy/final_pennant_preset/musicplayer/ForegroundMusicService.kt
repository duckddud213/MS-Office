//package com.ssafy.final_pennant_preset.musicplayer
//
//import android.app.Service
//import android.content.Intent
//import android.media.MediaPlayer
//import android.net.Uri
//import android.os.IBinder
//import android.provider.MediaStore
//import android.util.Log
//import com.google.android.exoplayer2.MediaItem
//import com.ssafy.component_3.MusicNotification
//import com.ssafy.final_pennant.R
//import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
//
//private const val TAG = "ForegroundMusicService_싸피"
//
//class ForegroundMusicService : Service() {
//    lateinit var mp : MediaPlayer
//    var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//
//    override fun onCreate() {
//        super.onCreate()
////
////        mediaItem = MediaItem.fromUri("${uri}/${musicviewmodel.selectedMusic.id}")
////        player.setMediaItem(mediaItem, 0)
////        binding.playControlImageView.setImageResource(R.drawable.img_pause)
////        player.prepare()
////        player.play()
//        mp = MediaItem.fromUri("${uri}/${musicviewmodel.selectedMusic.id}")
//        Log.d(TAG, "onCreate()")
//    }
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, "Action Received = ${intent?.action}")
//        when (intent?.action) {
//            Actions.START_FOREGROUND -> {
//                Log.d(TAG, "Start Foreground 인텐트를 받음")
//                if( !mp.isPlaying ){
//                    mp.isLooping = true
//                    mp.start()
//                    startForegroundService()
//                }
//            }
//            Actions.STOP_FOREGROUND -> {
//                Log.d(TAG, "Stop Foreground 인텐트를 받음")
//                if( mp.isPlaying ) {
//                    stopForegroundService()
//                    mp.stop() //음악 중지
//                }
//            }
//            Actions.PLAY ->{
//                Log.d(TAG, "start music from notification : ${mp.isPlaying}")
//                if( !mp.isPlaying ){
//                    Log.d(TAG, "start music from notification")
//                    mp.start()
//                }
//            }
//            Actions.STOP ->{
//                Log.d(TAG, "stop music from notification")
//                if( mp.isPlaying ) mp.pause() //음악 중지
//            }
//        }; return START_STICKY
//    }
//
//    private fun startForegroundService() {
//        val notification = MusicNotification.createNotification(this)
//        startForeground(NOTIFICATION_ID, notification)
//    }
//
//    private fun stopForegroundService() {
//        stopForeground(true)
//        stopSelf()
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        // bindservice가 아니므로 null
//        return null
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(TAG, "onDestroy()")
//    }
//
//    companion object {
//        const val NOTIFICATION_ID = 20
//    }
//}