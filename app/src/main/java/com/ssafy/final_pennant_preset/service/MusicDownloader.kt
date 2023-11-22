package com.ssafy.final_pennant_preset.service

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri

private const val TAG = "MusicDownloader_싸피"
class MusicDownloader (
    private val context: Context
) : IMusicDownloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String, fileName: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("audio/*")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI) // 네트워크 설정
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            .addRequestHeader("Authorization", "Bearer <token>")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "/msOffice/$fileName"
            )
        Log.d(TAG, "downloadFile: ${url.toUri()}")

        return downloadManager.enqueue(request)
    }

}