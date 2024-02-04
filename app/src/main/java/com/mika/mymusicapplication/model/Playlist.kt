package com.mika.mymusicapplication.model

data class Playlist(
    var name: String,
    var data: List<SongInfo> = listOf(),
    var note: String? = null,
)