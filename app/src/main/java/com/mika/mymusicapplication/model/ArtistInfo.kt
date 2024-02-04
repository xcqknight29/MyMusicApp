package com.mika.mymusicapplication.model

import android.graphics.Bitmap

data class ArtistInfo(
    var name: String,
    var songList: List<SongInfo> = listOf(),
    var thumbnail: List<Bitmap>? = null,
    var albumArt: List<String>? = null,
)