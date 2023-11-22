package com.ssafy.final_pennant_preset.service

interface IMusicDownloader {
    fun downloadFile(url: String, fileName: String): Long
}