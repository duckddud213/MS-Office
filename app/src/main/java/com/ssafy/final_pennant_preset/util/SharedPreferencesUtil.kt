package com.ssafy.final_pennant_preset.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.config.ApplicationClass
import java.lang.StringBuilder

private const val TAG = "SharedPreferencesUtil_싸피"

class SharedPreferencesUtil(context: Context) {

    private val KEY_SONG_LIST_NAME = "songListName"
    private val KEY_CUR_SONG_LIST = "curSongList"
    private val KEY_SELECTED_SONG_POSITION = "selectedSongPosition"

    private var preferences: SharedPreferences =
        context.getSharedPreferences(ApplicationClass.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun addUserCookie(cookies: HashSet<String>) {
        val editor = preferences.edit()
        editor.putStringSet(ApplicationClass.COOKIES_KEY_NAME, cookies)
        editor.apply()
    }

    fun getUserCookie(): MutableSet<String>? {
        return preferences.getStringSet(ApplicationClass.COOKIES_KEY_NAME, HashSet())
    }

    fun getString(key: String): String? {
        return preferences.getString(key, null)
    }

    fun putEmail(value: String) {
        val editor = preferences.edit()
        editor.putString("userEmail", value)
        editor.apply()
    }

    fun getEmail(): String? {
        return preferences.getString("userEmail", null)
    }

    fun putUID(value: String) {
        val editor = preferences.edit()
        editor.putString("userUID", value)
        editor.apply()
    }

    fun getUID(): String? {
        return preferences.getString("userUID", null)
    }

    fun putNotification(genre: String, isPush: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(genre, isPush)
        editor.apply()
    }

    fun getNotification(genre: String): Boolean{
        return preferences.getBoolean(genre, true)
    }

    /**
     * param: 재생목록의 이름, 해당하는 재생목록의 MutableList<MusicDto>
     * return: 저장 여부(중복된 재생목록이 있는 경우)
     *
     * 재생목록을 저장하는 함수
     * 수정인 경우 덮어씌우고, 새로운 재생목록을 만드는 경우 동일한 이름의 기존 재생목록이 있다면 return false
     *
     * 구분인자
     * || -> MusicDTO 내부의 요소들을 구분
     * && -> List 내의 MusicDTO을 구분
     */
    fun putSongList(
        playListName: String,
        songList: MutableList<MusicDTO>,
    ) {

        val editor = preferences.edit()
        val sb = StringBuilder()

        songList.forEach {
            sb.append("${it.id}||${it.title}||${it.albumId}||${it.artist}||${it.genre}")
            sb.append("&&")
        }
        Log.d(TAG, "${playListName} : putSongList: ${sb}")
        editor.putString(playListName, sb.toString())
        editor.apply()

    }

    /**
     * param: 원하는 재생목록의 이름
     * return: 해당하는 재생목록의 MutableList<MusicDto>
     */
    fun getSongList(playListName: String): MutableList<MusicDTO> {
        val songList = mutableListOf<MusicDTO>()
        val songStr = preferences.getString(playListName, null)

        songStr?.let {
            val songArr = it.split("&&") // 하나의 MusicDTO를 가져옴
            for (song in songArr) {
                if (song.equals("")) {
                    break
                }
                var element = song.split("||") // 하나의 MusicDTO 안의 요소들을 가져옴
                Log.d(
                    TAG,
                    "getSongList: ${element[0]} / ${element[1]} / ${element[2]} / ${element[3]} / ${element[4]} / "
                )
                songList.add(
                    MusicDTO(
                        element[0].toLong(),
                        element[1], element[2].toLong(), element[3], element[4]
                    )
                )
            }
        }
        return songList
    }

    fun deleteSongFromList(playListName: String, songInfo: MusicDTO) {
        val songListStr = preferences.getString(playListName,null)
        var newSongListStr :String = ""
        val editor = preferences.edit()

        songListStr?.let {
            val songItem = it.split("&&")
            for(song in songItem){
                if (song.equals("")) {
                    break
                }
                Log.d(TAG, "deleteSongFromList: ${song}")
                var element = song.split("||") // 하나의 MusicDTO 안의 요소들을 가져옴

                if(!songInfo.equals(MusicDTO(element[0].toLong(),element[1],element[2].toLong(),element[3],element[4]))){
                    Log.d(TAG, "deleteSongFromList: add : ${song}")
                    newSongListStr+=song+"&&"
                }
            }
        }
        
        editor.putString(playListName,newSongListStr)
        editor.apply()
    }

    /**
     * 현재 재생 목록을 저장하는 함수
     */

    fun putCurSongList(songListName: String) {
        val editor = preferences.edit()

        editor.putString(KEY_CUR_SONG_LIST, songListName)
        editor.apply()
    }


    /**
     * 현재 재생 목록을 반환
     */
    fun getCurSongList(): String {
        val songListName = preferences.getString(KEY_CUR_SONG_LIST, null)

        return songListName ?: ""
    }

    /**
     * 저장된 재생목록들의 String 값들을 ArrayList로 반환하는 함수
     */
    fun getSongListName(): MutableList<String> {

        val songListNameList = mutableListOf<String>()

        val songListNameStr = preferences.getString(KEY_SONG_LIST_NAME, null)
        songListNameStr?.let {
            val names = it.split("&&")
            for (name in names) {
                songListNameList.add(name)
            }
        }

        return songListNameList
    }

    /**
     * 재생목록의 이름을 저장하는 함수
     */
    fun addSongListName(songListName: String) {
        val editor = preferences.edit()
        var songList = preferences.getString(KEY_SONG_LIST_NAME, "")
        songList += "${songListName}&&"
        editor.putString(KEY_SONG_LIST_NAME, songList)
        editor.apply()
    }

    fun deleteSongListName(deletedSongListName: String) {
        val editor = preferences.edit()
        var songListNames = preferences.getString(KEY_SONG_LIST_NAME, "")
        var newSongList = ""

        songListNames?.let {
            val names = it.split("&&")
            for (name in names) {
                Log.d(TAG, "deleteSongListName: name : ${name}")
                if (!name.equals(deletedSongListName) && !name.equals("")) {
                    newSongList += "${name}&&"
                }
            }
        }

        Log.d(TAG, "deleteSongListName: ${newSongList}")
        editor.putString(KEY_SONG_LIST_NAME, newSongList)
        editor.apply()
    }

    /**
     * 재생 중인 곡의 해당 재생목록에서의 위치 저장
     */

    fun putSelectedSongPosition(position:Int){
        val editor = preferences.edit()
        editor.putString(KEY_SELECTED_SONG_POSITION,position.toString())
        editor.apply()
        Log.d(TAG, "putSelectedSongPosition: ${preferences.getString(KEY_SELECTED_SONG_POSITION,null)}")
    }

    fun getSelectedSongPosition() : Int{
        val selectedSongPosition = preferences.getString(KEY_SELECTED_SONG_POSITION,null)
        return selectedSongPosition?.toInt() ?: -1
    }


}
