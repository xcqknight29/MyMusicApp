package com.mika.mymusicapplication.model

data class SongInfo(
    var title: String,
    var album: String,
    var artist: String,
    var uri: String,
    var duration: Int,
    var size: Int
)