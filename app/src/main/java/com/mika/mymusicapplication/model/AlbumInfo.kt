package com.mika.mymusicapplication.model

import android.graphics.Bitmap

data class AlbumInfo(
    var title: String,
    var artist: String,
    var songCount: Int,
    var songList: List<SongInfo> = listOf(),
    var thumbnail: Bitmap? = null,
    var albumArt: String? = null,
)