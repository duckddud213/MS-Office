package com.ssafy.final_pennant_preset.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant_preset.MainActivity
import com.ssafy.final_pennant_preset.config.ApplicationClass

private const val TAG = "MyFirebaseMessageServic"
class MyFirebaseMessageService : FirebaseMessagingService() {

    // 새로운 토큰이 생성될 때 마다 해당 콜백이 호출된다.
    @SuppressLint("LongLogTag")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: $token")
        // 새로운 토큰 수신 시 서버로 전송
        MainActivity.uploadToken(token)
    }

    lateinit var builder : NotificationCompat.Builder

    // Foreground, Background 모두 처리하기 위해서는 data에 값을 담아서 넘긴다.
    //https://firebase.google.com/docs/cloud-messaging/android/receive
    @SuppressLint("LongLogTag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var messageTitle = ""
        var messageContent = ""
        var messageGenre = ""

        if(remoteMessage.notification != null){ // notification이 있는 경우 foreground처리
            //foreground
            messageTitle= remoteMessage.notification!!.title.toString()
            messageContent = remoteMessage.notification!!.body.toString()

        }else{  // background 에 있을경우 혹은 foreground에 있을경우 두 경우 모두
            var data = remoteMessage.data
            Log.d(TAG, "data.message: ${data}")
            Log.d(TAG, "data.message: ${data.get("title")}")
            Log.d(TAG, "data.message: ${data.get("body")}")

            messageTitle = data.get("title").toString()
            messageContent = data.get("body").toString()
            messageGenre = data.get("genre").toString()
            Log.d(TAG, "onMessageReceived: $messageGenre")
        }

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val mainPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_MUTABLE)
        var channelId = 100
        when(messageGenre) {

            ApplicationClass.CHANNEL_DANCE -> {
                if (!ApplicationClass.receive_notification_dance) return

                builder = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_DANCE)
                    .setSmallIcon(R.drawable.noti_dance)
                channelId = 101
            }

            ApplicationClass.CHANNEL_BALLAD -> {
                if (!ApplicationClass.receive_notification_ballad) return
                builder = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_BALLAD)
                    .setSmallIcon(R.drawable.noti_ballad)
                channelId = 102
            }

            ApplicationClass.CHANNEL_IDOL -> {
                if (!ApplicationClass.receive_notification_idol) return
                builder = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_IDOL)
                    .setSmallIcon(R.drawable.noti_idol)
                channelId = 103
            }

            ApplicationClass.CHANNEL_POP -> {
                if (!ApplicationClass.receive_notification_pop) return
                builder = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_POP)
                    .setSmallIcon(R.drawable.noti_pop)
                channelId = 104
            }

            ApplicationClass.CHANNEL_ROCK -> {
                if (!ApplicationClass.receive_notification_rock) return
                builder = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ROCK)
                    .setSmallIcon(R.drawable.noti_rock)
                channelId = 105
            }

            else -> return
        }

        builder.setContentTitle(messageTitle)
                .setContentText(messageContent)
                .setAutoCancel(true)
                .setContentIntent(mainPendingIntent)

        NotificationManagerCompat.from(applicationContext).apply {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(channelId, builder.build())
        }

    }
}