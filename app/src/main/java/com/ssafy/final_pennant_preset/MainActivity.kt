package com.ssafy.final_pennant_preset

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationBarView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.ActivityMainBinding

private const val TAG = "MainActivity_싸피"
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            supportFragmentManager.beginTransaction().replace(R.id.framecontainer,fragment_currentlist()).commit()

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
    }
}