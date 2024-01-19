package com.mika.mymusicapplication.model

data class Playlist(
    var name: String,
    var data: MutableList<SongInfo>
)