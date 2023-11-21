package com.ssafy.final_pennant_preset

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.ServerMusicDTO
import com.ssafy.final_pennant_preset.util.RetrofitUtil
import kotlinx.coroutines.launch
import java.io.File


private const val TAG = "ServerGenreViewModel_싸피"
class ServerViewModel : ViewModel() {

    private val _musicWithGenre = MutableLiveData<List<ServerMusicDTO>>()
    val musicWithGenre: LiveData<List<ServerMusicDTO>>
        get() = _musicWithGenre

    fun getListWithGenre(genre: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "getListWithGenre: $genre")
                _musicWithGenre.value = RetrofitUtil.musicService.getMusicListByGenre(genre)
            } catch (e: Exception) {
                Log.d(TAG, "getListWithGenre: 데이터 조회 실패")
                Log.d(TAG, "getListWithGenre: ${e.printStackTrace()}")
            }
        }
    }

    fun deleteMusic(filename: String) {
        val uid = ApplicationClass.sSharedPreferences.getUID()!!

        viewModelScope.launch {
            try {
                RetrofitUtil.musicService.deleteMusic(filename, uid)
            } catch (e: Exception) {
                Log.d(TAG, "deleteMusic: 파일 삭제 실패")
            }
        }
    }

    fun uploadMusic(genre: String, fileUri: String) {
        File(fileUri)
    }

}
