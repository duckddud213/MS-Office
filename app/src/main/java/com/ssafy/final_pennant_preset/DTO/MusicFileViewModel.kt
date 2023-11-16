package com.ssafy.final_pennant_preset.DTO

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MusicFileViewModel(private val handle:SavedStateHandle):ViewModel() {
    var MusicList = mutableListOf<MusicDTO>()

    fun setMusicList(musicFile : MusicDTO){
        MusicList.add(musicFile)
    }
}