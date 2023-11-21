//package com.ssafy.final_pennant_preset
//
//import android.app.Service
//import android.content.Intent
//import android.media.MediaPlayer
//import android.os.IBinder
//import android.util.Log
//import com.google.android.exoplayer2.ExoPlayer
//
//private const val TAG = "ForeMusicService_싸피"
//class ForegroundMusicService : Service() {
//    lateinit var ep : ExoPlayer
//
//    override fun onCreate() {
//        super.onCreate()
//        mp = MediaPlayer.create(this,R.raw.jazzbyrima)
//        Log.d(TAG, "onCreate()")
//    }
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d( TAG, "Action Received = ${intent?.action}" )
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