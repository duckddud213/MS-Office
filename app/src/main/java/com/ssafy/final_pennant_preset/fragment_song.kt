package com.ssafy.final_pennant_preset

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel

private const val TAG = "fragment_song_싸피"
class fragment_song : Fragment() {

    val musicviewmodel : MusicFileViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
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

    override fun onDetach() {
        super.onDetach()
    }
}
