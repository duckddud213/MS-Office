package com.ssafy.final_pennant_preset

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ssafy.final_pennant_preset.DTO.MusicFileViewModel
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant_preset.DTO.MusicDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val TAG = "SplashActivity_싸피"

@RequiresApi(Build.VERSION_CODES.R)
class SplashActivity : AppCompatActivity() {

    private val musicviewmodel: MusicFileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        if (!checkPermission()) {
            val permissionListener = object : PermissionListener {
                // 권한 얻기에 성공했을 때 동작 처리
                override fun onPermissionGranted() {
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }, 1500)
                }

                // 권한 얻기에 실패했을 때 동작 처리
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    showPermissionCheckDialog()
                    Toast.makeText(
                        this@SplashActivity,
                        "파일 접근 권한이 거부되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("[설정] 에서 파일 접근 권한을 부여해야만 사용이 가능합니다.")
                // 필요한 권한 설정
                .setPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
                .check()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }, 1500)
        }
    }

    private fun showPermissionCheckDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("파일 접근 권한 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 접근 권한이 필요합니다.\n"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { _, _ ->
            val fileSettingIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        }
        builder.setNegativeButton(
            "취소"
        ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    private fun checkPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }



}