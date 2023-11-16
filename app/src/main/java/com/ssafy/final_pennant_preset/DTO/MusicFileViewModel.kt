package com.ssafy.final_pennant_preset.DTO

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MusicFileViewModel:ViewModel() {
//    private var _MusicList = MutableLiveData<MutableList<MusicDTO>>()
//    val MusicList : LiveData<MutableList<MusicDTO>>
//        get() = _MusicList
//
//    fun setMusicList(musicFile : MutableList<MusicDTO>){
//        _MusicList.value = musicFile
//    }

    var initialized :Boolean = false

    var MusicList = mutableListOf<MusicDTO>()
        private set
}
