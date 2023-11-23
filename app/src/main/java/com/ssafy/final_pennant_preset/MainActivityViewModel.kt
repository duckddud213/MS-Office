package com.ssafy.final_pennant_preset

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.ServerMusicDTO
import com.ssafy.final_pennant_preset.util.RetrofitUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "MainActivityViewModel_싸피"
class MainActivityViewModel : ViewModel() {

    private val _musicWithGenre = MutableLiveData<List<ServerMusicDTO>>()
    val musicWithGenre: LiveData<List<ServerMusicDTO>>
        get() = _musicWithGenre

    fun setListWithGenre(genre: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "getListWithGenre: $genre")
                _musicWithGenre.value = RetrofitUtil.musicService.getMusicListByGenre(genre)
            } catch (e: Exception) {
                Log.d(TAG, "getListWithGenre: 데이터 조회 실패")
                Log.d(TAG, "getListWithGenre: ${e.printStackTrace()}")
            }
        }
    }

    fun deleteMusic(genre: String, musicId: String) {

        CoroutineScope(Dispatchers.Main).launch {
            try {
                RetrofitUtil.musicService.deleteMusic(musicId)
                setListWithGenre(genre)
            } catch (e: Exception) {
                Log.d(TAG, "deleteMusic: 파일 삭제 실패")
            }
        }
    }

    fun updateMusic(dto: ServerMusicDTO) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "updateMusic1: ${dto.musicId}")
                Log.d(TAG, "updateMusic1: ${dto.musicGenre}")
                Log.d(TAG, "updateMusic1: ${dto.musicName}")
                val log = RetrofitUtil.musicService.updateMusic(dto)
                Log.d(TAG, "updateMusic2: $log")
                setListWithGenre(dto.musicGenre)
            } catch (e: Exception) {
                Log.d(TAG, "updateMusic2: 파일 갱신 실패")
                Log.d(TAG, "updateMusic: $e")
            }
        }
    }
    
}