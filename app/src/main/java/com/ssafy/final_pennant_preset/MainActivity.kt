package com.ssafy.final_pennant_preset

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.ActivityMainBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass

private const val TAG = "MainActivity_싸피"
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            supportFragmentManager.beginTransaction().replace(R.id.framecontainer,fragment_totallist()).commit()

            bottomNavView.setOnItemSelectedListener {
                Log.d(TAG, "onCreate: Clicked!${it.itemId}")
                val transaction = supportFragmentManager.beginTransaction()
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
                bottomNavView.menu.findItem(R.id.btnTotalFile).isCheckable = false
                bottomNavView.menu.findItem(R.id.btnPlayList).isCheckable = false
                bottomNavView.menu.findItem(R.id.btnCurrentList).isCheckable = false
                bottomNavView.menu.findItem(R.id.btnConnectServer).isCheckable = false
                bottomNavView.labelVisibilityMode =NavigationBarView.LABEL_VISIBILITY_UNLABELED

                supportFragmentManager.beginTransaction().replace(R.id.framecontainer,fragment_song()).commit()
            }
        }
        setContentView(binding.root)


        //pref 에서 조회 여부에 따른 처리 필요
        val userID = ApplicationClass.sSharedPreferences.getString("userID") ?: ""
        Log.d(TAG, "onCreate: $userID")
        if (userID.isNullOrEmpty()) {
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

    // firebase 로그인 관련

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
            }
        } else {
            Log.d(TAG, "onActivityResult: fail")
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
                }
            }
    }
    // [END auth_with_google]

    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
//            binding.loginTv.text = "인증 실패"
//            Toast.makeText(this, "인증 실패", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "updateUI 실패")
        } else {
            user.displayName?.let {
                ApplicationClass.sSharedPreferences.putID(it)
                Log.d(TAG, "updateUI: $it")
            }

//            startActivity(Intent(this, MainActivity::class.java).apply {
//                Log.d(TAG, "onCreate: ${user.photoUrl.toString()}")
//                Log.d(TAG, "onCreate: ${user.displayName}")
//                putExtra("userImg", user.photoUrl.toString())
//                putExtra("userName", user.displayName)
//            })
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
