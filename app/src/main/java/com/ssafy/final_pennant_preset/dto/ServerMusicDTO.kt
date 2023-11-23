package com.ssafy.final_pennant_preset.dto

import com.google.gson.annotations.SerializedName

// 서버에서 리스트를 가져올 때 사용하는 dto

data class ServerMusicDTO (
    @SerializedName("musicId") val musicId: String
    , @SerializedName("uploadUser") val uploadUser: String
    , @SerializedName("musicName") val musicName: String
    , @SerializedName("musicS3Key") val musicS3Key: String
    , @SerializedName("musicGenre") val musicGenre: String
    , @SerializedName("musicUrl") val musicUrl: String
    ) {
    override fun toString(): String {
        return musicName.replace(".mp3", "")
    }
}