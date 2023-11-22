package com.ssafy.final_pennant_preset

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.ActivityMainBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.util.RetrofitUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val TAG = "MainActivity_싸피"
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    private lateinit var googleSignInClient: GoogleSignInClient

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            supportFragmentManager.beginTransaction().replace(R.id.framecontainer,fragment_totallist()).commit()

            bottomNavView.setOnItemSelectedListener {
                Log.d(TAG, "onCreate: Clicked!${it.itemId}")
                val transaction = supportFragmentManager.beginTransaction()
                supportFragmentManager.popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE)
                binding.apply {
                    bottomNavView.menu.findItem(R.id.btnTotalFile).isCheckable = true
                    bottomNavView.menu.findItem(R.id.btnPlayList).isCheckable = true
                    bottomNavView.menu.findItem(R.id.btnCurrentList).isCheckable = true
                    bottomNavView.menu.findItem(R.id.btnConnectServer).isCheckable = true
                    bottomNavView.labelVisibilityMode =NavigationBarView.LABEL_VISIBILITY_AUTO
                }
                when (it.itemId) {
                    R.id.btnTotalFile -> {
                        transaction.replace(R.id.framecontainer, fragment_totallist()).commit()
                    }

                    R.id.btnPlayList -> {
                        transaction.replace(R.id.framecontainer, fragment_playlist()).commit()
                    }

                    R.id.btnCurrentList -> {
                        transaction.replace(R.id.framecontainer, fragment_currentlist()).commit()
                    }

                    R.id.btnConnectServer -> {
                        transaction.replace(R.id.framecontainer, fragment_server()).commit()
                    }
                }
                true
            }
            btnPlay.setOnClickListener {
                supportFragmentManager.popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE)
                bottomNavView.menu.findItem(R.id.btnTotalFile).isCheckable = false
                bottomNavView.menu.findItem(R.id.btnPlayList).isCheckable = false
                bottomNavView.menu.findItem(R.id.btnCurrentList).isCheckable = false
                bottomNavView.menu.findItem(R.id.btnConnectServer).isCheckable = false
                bottomNavView.labelVisibilityMode =NavigationBarView.LABEL_VISIBILITY_UNLABELED

                supportFragmentManager.beginTransaction().replace(R.id.framecontainer,fragment_song()).commit()
            }
        }
        setContentView(binding.root)

        checkLogin()
        initNotificationChannel()
    }

    override fun onResume() {
        super.onResume()
        val completeFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//        registerReceiver(downloadCompleteReceiver, completeFilter)
    }

    override fun onPause() {
        super.onPause()
//        unregisterReceiver(downloadCompleteReceiver)
    }

    // firebase 로그인 관련

    private fun checkLogin() {

        val userEmail = ApplicationClass.sSharedPreferences.getEmail()
        val userUID = ApplicationClass.sSharedPreferences.getUID()

        Log.d(TAG, "checkLogin email: $userEmail")
        Log.d(TAG, "checkLogin uid: $userUID")

        if (userUID == null) {
            // firebase 로그인 관련
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            auth = Firebase.auth
            signIn()
            val currentUser = auth.currentUser
            updateUI(currentUser)
        }
    }
    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        updateUI(currentUser)
    }
    // [END on_start_check_user]
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // [START onactivityresult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                Log.d(TAG, "firebaseAuthWithGoogle: ${account.photoUrl}")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "파일 업로드를 위해 로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Log.d(TAG, "onActivityResult: fail")
            finish()
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                    finish()
                }
            }
    }
    // [END auth_with_google]

    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            Toast.makeText(this, "파일 업로드를 위해 로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "updateUI 실패")
        } else {

            user.email?.let {
                ApplicationClass.sSharedPreferences.putEmail(it)
            }

            user.uid.let { //not null값
                ApplicationClass.sSharedPreferences.putUID(it)
            }

            initFirebase()
        }
    }

    // firebase push 관련
    private fun initFirebase() {
        // FCM 토큰 수신
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "FCM 토큰 얻기에 실패하였습니다.", task.exception)
                return@OnCompleteListener
            }
            // token log 남기기
            Log.d(TAG, "token: ${task.result?:"task.result is null"}")
            if(task.result != null){
                uploadToken(task.result!!)
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun initNotificationChannel() {
        createNotificationChannel(ApplicationClass.CHANNEL_DANCE, ApplicationClass.CHANNEL_DANCE)
        createNotificationChannel(ApplicationClass.CHANNEL_IDOL, ApplicationClass.CHANNEL_IDOL)
        createNotificationChannel(ApplicationClass.CHANNEL_BALLAD, ApplicationClass.CHANNEL_BALLAD)
        createNotificationChannel(ApplicationClass.CHANNEL_ROCK, ApplicationClass.CHANNEL_ROCK)
        createNotificationChannel(ApplicationClass.CHANNEL_POP , ApplicationClass.CHANNEL_POP)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String, name: String) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(id, name, importance)
        val notificationManager: NotificationManager
                = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }



    // 파일 다운로드 관련

//    lateinit var mDownloadQueueId: Long

//    private val downloadCompleteReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//            if (mDownloadQueueId === reference) {
//                val query = DownloadManager.Query() // 다운로드 항목 조회에 필요한 정보 포함
//                query.setFilterById(reference)
//                val cursor: Cursor = mDownloadManager.query(query)
//                cursor.moveToFirst()
//                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
//                val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
//                val status = cursor.getInt(columnIndex)
//                val reason = cursor.getInt(columnReason)
//                cursor.close()
//                when (status) {
//                    DownloadManager.STATUS_SUCCESSFUL -> Toast.makeText(
//                        this@MainActivity,
//                        "다운로드를 완료하였습니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                    DownloadManager.STATUS_PAUSED -> Toast.makeText(
//                        this@MainActivity,
//                        "다운로드가 중단되었습니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                    DownloadManager.STATUS_FAILED -> Toast.makeText(
//                        this@MainActivity,
//                        "다운로드가 취소되었습니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }
//    }

    companion object {
        private const val RC_SIGN_IN = 9001

        // main에 1개
        // firebaseservice에 1개 주석 처리되있음
        fun uploadToken(token:String){
            // 새로운 토큰 수신 시 서버로 전송
            val tokenService = RetrofitUtil.firebaseTokenService

            val userUID = ApplicationClass.sSharedPreferences.getUID() ?: return

            Log.d(TAG, "uploadToken: token: $token")
            Log.d(TAG, "uploadToken: uid : $userUID")

            tokenService.uploadToken(userUID, token).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if(response.isSuccessful){
                        val res = response.body()
                        Log.d(TAG, "onResponse: $res")
                    } else {
                        Log.d(TAG, "onResponse: Error Code ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d(TAG, t.message ?: "토큰 정보 등록 중 통신오류")
                }
            })
        }
    }
}
