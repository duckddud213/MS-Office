package com.ssafy.final_pennant_preset.dto

data class MusicDTO(val id:Long, val title:String,val albumId:Long, val artist:String, val genre:String) {
    override fun equals(other: Any?): Boolean {
        var oDto = other as MusicDTO
        return (oDto.title == title && oDto.artist == artist)
    }
    override fun hashCode(): Int {
        return title.hashCode().xor(artist.hashCode())
    }
}

//앱 내 필요 음악 정보 DTO

//id
//이름
//앨범 id
//아티스트
//장르
