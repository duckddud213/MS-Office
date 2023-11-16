package com.ssafy.final_pennant_preset

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant_preset.DTO.MusicFileViewModel
import kotlin.math.log

private const val TAG = "fragment_song_싸피"
class fragment_song : Fragment() {

    val musicviewmodel : MusicFileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d(TAG, "onCreate: ${musicviewmodel.MusicList.get(0)}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song, container, false)
    }
}