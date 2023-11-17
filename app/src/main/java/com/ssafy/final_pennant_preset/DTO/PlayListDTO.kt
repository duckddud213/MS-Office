package com.ssafy.final_pennant_preset.DTO

import java.io.Serializable

data class PlayListDTO(val playlistId: Long, val playlistname: String, val songlist: MutableList<MusicDTO>)

//현재 재생 목록에서 재생 중이었던 곡 정보(몇 번째 곡 재생중이었는지 등)