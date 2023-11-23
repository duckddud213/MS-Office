package com.ssafy.final_pennant_preset

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.util.Log
import android.widget.Toast

private const val TAG = "DownloadCompletedReceiv_싸피"
class DownloadCompletedReceiver{

//    private lateinit var downloadManager: DownloadManager
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//
//        downloadManager = context?.getSystemService(DownloadManager::class.java)!!
//        if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
//            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
//            val query = DownloadManager.Query()
//            val cursor: Cursor = downloadManager.query(query)
//            cursor.moveToFirst()
//            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
//            val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
//            val status = cursor.getInt(columnIndex)
//            val reason = cursor.getInt(columnReason)
//            cursor.close()
//            when (status) {
//                DownloadManager.STATUS_SUCCESSFUL -> Toast.makeText(
//                    context,
//                    "다운로드를 완료하였습니다.",
//                    Toast.LENGTH_SHORT
//                    // 여기에서 다운로드 완료 처리하는 코드 작성하면 됨!
//
//                ).show()
//
//                DownloadManager.STATUS_PAUSED -> Toast.makeText(
//                    context,
//                    "다운로드가 중단되었습니다.",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                DownloadManager.STATUS_FAILED -> Toast.makeText(
//                    context,
//                    "다운로드가 취소되었습니다.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
}