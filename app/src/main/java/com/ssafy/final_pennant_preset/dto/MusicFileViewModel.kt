package com.ssafy.final_pennant_preset.dto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicFileViewModel:ViewModel() {
    var MusicList = mutableListOf<MusicDTO>()
        private set

    var playList = mutableListOf<PlayListDTO>()
        private set

    var checkedPlayList = mutableListOf<checkboxData>()
        private set

    var checkedSongList = mutableListOf<checkboxSongData>()
        private set

    var selectedMusicToBeAdded : MusicDTO = MusicDTO(-1,"",-1,"","")

    var selectedMusic : MusicDTO = MusicDTO(-1,"",-1,"","")

    var selectedMusicPosition : Int = -1

    var selectedPlayList : PlayListDTO = PlayListDTO("", mutableListOf<MusicDTO>())

    var selectedPlaylistName : String = ""

    var downloadedUri : String = ""

    var isPlaying : Boolean = false
    var isPlayingOn : Long = -1
    var checkSameSong:Boolean = true

    lateinit var playerNotificationManager : PlayerNotificationManager
}
