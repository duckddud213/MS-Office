package com.ssafy.final_pennant_preset.dto

import androidx.lifecycle.ViewModel

class MusicFileViewModel:ViewModel() {
//    private var _MusicList = MutableLiveData<MutableList<MusicDTO>>()
//    val MusicList : LiveData<MutableList<MusicDTO>>
//        get() = _MusicList
//
//    fun setMusicList(musicFile : MutableList<MusicDTO>){
//        _MusicList.value = musicFile
//    }

    var MusicList = mutableListOf<MusicDTO>()
        private set

    var playList = mutableListOf<PlayListDTO>()
        private set

    var checkedPlayList = mutableListOf<checkboxData>()
        private set

    var selectedMusic : MusicDTO = MusicDTO(-1,"",-1,"","")

    var selectedPlayList : PlayListDTO = PlayListDTO("", mutableListOf<MusicDTO>())

    var selectedPlaylistName : String = ""
}
