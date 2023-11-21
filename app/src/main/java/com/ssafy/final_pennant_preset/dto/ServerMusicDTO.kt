package com.ssafy.final_pennant_preset.dto

// 서버에서 리스트를 가져올 때 사용하는 dto

data class ServerMusicDTO (
    val id: String
    , val user: String
    , val name: String
    , val s3Name: String
    , val genre: String
    , val site: String
    ) {
    override fun toString(): String {
        return name
    }
}