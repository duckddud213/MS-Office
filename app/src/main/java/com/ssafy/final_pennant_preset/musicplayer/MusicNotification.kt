//package com.ssafy.component_3
//
//import android.app.*
//import android.app.PendingIntent.FLAG_IMMUTABLE
//import android.app.PendingIntent.FLAG_UPDATE_CURRENT
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.ssafy.final_pennant.R
//import com.ssafy.final_pennant_preset.MainActivity
//import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
//import com.ssafy.final_pennant_preset.musicplayer.Actions
//import com.ssafy.final_pennant_preset.musicplayer.ForegroundMusicService
//
//object MusicNotification {
//    const val CHANNEL_ID =aa
//        "foreground_service_channel" // 임의의 채널 ID
//
//    fun createNotification(context: Context): Notification {
//        // 알림 클릭시 MainActivity로 이동됨
//        val notificationIntent = Intent(context, MainActivity::class.java)
//        notificationIntent.action = Actions.MAIN
//        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val pendingIntent = PendingIntent.getActivity(context,0,notificationIntent,FLAG_IMMUTABLE)
//
//        // 각 버튼들에 관한Intent
//        val prevIntent = Intent(context, ForegroundMusicService::class.java)
//        prevIntent.action = Actions.PREV
//        val prevPendingIntent = PendingIntent.getService(context, 0, prevIntent, FLAG_IMMUTABLE)
//
//        val playIntent = Intent(context, ForegroundMusicService::class.java)
//        playIntent.action = Actions.PLAY
//        val playPendingIntent = PendingIntent.getService(context, 0, playIntent, FLAG_IMMUTABLE)
//
//        val nextIntent = Intent(context, ForegroundMusicService::class.java)
//        nextIntent.action = Actions.STOP
//        val nextPendingIntent = PendingIntent.getService(context, 0, nextIntent, FLAG_IMMUTABLE)
//
//        // 알림
//        val notification =
//            NotificationCompat.Builder(context, CHANNEL_ID).setContentTitle("MS Office")
//                .setContentText("My Music").setSmallIcon(R.drawable.music_ssafy_office)
//                .setOngoing(true) // true 일경우 알림 리스트에서 클릭하거나 좌우로 드래그해도 사라지지 않음
//                .addAction(
//                    NotificationCompat.Action(
//                        android.R.drawable.ic_media_previous,
//                        "Prev",
//                        prevPendingIntent
//                    )
//                ).addAction(
//                    NotificationCompat.Action(
//                        android.R.drawable.ic_media_play,
//                        "Play",
//                        playPendingIntent
//                    )
//                ).addAction(
//                    NotificationCompat.Action(
//                        android.R.drawable.ic_media_next,
//                        "STOP",
//                        nextPendingIntent
//                    )
//                ).setContentIntent(pendingIntent)
//                .build()
//
//        // Oreo 부터는 Notification Channel을 만들어야 함
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                CHANNEL_ID,
//                "Music Player Channel", // 채널표시명
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            val manager = context.getSystemService(NotificationManager::class.java)
//            manager?.createNotificationChannel(serviceChannel)
//        }; return notification
//    }
//}
//
//
//
//
