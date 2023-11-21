package com.ssafy.final_pennant_preset.dto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicFileViewModel:ViewModel() {
    var MusicList = mutableListOf<MusicDTO>()
        private set

    var playList = mutableListOf<PlayListDTO>()
        private set

    var checkedPlayList = mutableListOf<checkboxData>()
        private set

    var selectedMusic : MusicDTO = MusicDTO(-1,"",-1,"","")

    var selectedMusicPosition : Int = -1

    var selectedPlayList : PlayListDTO = PlayListDTO("", mutableListOf<MusicDTO>())

    var selectedPlaylistName : String = ""

}
