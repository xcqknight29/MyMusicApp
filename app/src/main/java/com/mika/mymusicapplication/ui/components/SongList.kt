package com.mika.mymusicapplication.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mika.mymusicapplication.model.SongInfo

/** 歌曲列表 */
@Composable
fun SongList(
    songsData: List<SongInfo>?,
    modifier: Modifier = Modifier,
    onItemClick: (() -> Unit)? = null,
    dropdownMenu: @Composable (((Boolean, List<SongInfo>) -> Unit))? = null
) {

    Column(modifier) {

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            if (songsData != null) {
                items(songsData) {
                    SongItem(it)
                }
            }
        }

        Spacer(Modifier.height(55.dp))

    }

}

@Preview(showBackground = true)
@Composable
fun SongListPreview() {
    val songsData: List<SongInfo> = List(10) {
        SongInfo(
            "title-1",
            "album-1",
            "artist-1",
            "uri-1",
            0,
            0
        )
    }
    SongList(songsData)
}