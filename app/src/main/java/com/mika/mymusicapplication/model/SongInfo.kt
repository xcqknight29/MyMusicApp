package com.mika.mymusicapplication.model

import android.graphics.Bitmap

data class SongInfo(
    var title: String,
    var album: String,
    var artist: String,
    var uri: String,
    var duration: Int,
    var size: Int,
    var thumbnail: Bitmap? = null,
    var albumArt: String? = null,
)