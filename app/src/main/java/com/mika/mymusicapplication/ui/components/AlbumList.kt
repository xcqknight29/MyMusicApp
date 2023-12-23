package com.mika.mymusicapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as columnItems
import androidx.compose.material.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.model.SongInfo

@Composable
fun AlbumList(albumList: MutableList<MutableList<SongInfo>>, modifier: Modifier = Modifier) {
    Column(modifier) {
        var albumDisplay by remember { mutableIntStateOf(0) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (albumDisplay == 0) {
                IconButton(onClick = { albumDisplay = 1 }) {
                    Icon(painterResource(R.drawable.albums_square), "square")
                }
            } else {
                IconButton(onClick = { albumDisplay = 0 }) {
                    Icon(painterResource(R.drawable.player_list), "list")
                }
            }
        }
        if (albumDisplay == 0) {
            LazyColumn {
                columnItems(albumList) {
                    AlbumColumnItem(it, Modifier.fillMaxWidth())
                }
            }
        } else {
            LazyVerticalGrid(GridCells.Adaptive(minSize = 100.dp)) {
                gridItems(albumList) {
                    AlbumGridItem(it)
                }
            }
        }
    }
}

@Composable
fun AlbumColumnItem(album: MutableList<SongInfo>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(R.drawable.song_cover_test),
            "cover",
            Modifier
                .background(Color.Gray)
                .width(40.dp)
                .aspectRatio(1f),
        )
        Column(Modifier.fillMaxWidth(0.8f)) {
            Text(album[0].album, maxLines = 1)
            Text(album[0].artist, maxLines = 1)
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(painterResource(R.drawable.more_buttom), "")
        }
    }
}

@Composable
fun AlbumGridItem(album: MutableList<SongInfo>, modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(2.dp)
            .fillMaxWidth()) {
        Column {
            Image(
                painterResource(R.drawable.song_cover_test),
                "cover",
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Text(album[0].album, maxLines = 2)
            Text(album[0].artist, maxLines = 1)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlbumListPreview() {
    val albumList = MutableList(8) {albumIndex ->
        MutableList(4) { songIndex ->
            SongInfo(
                "song - $songIndex",
                "album - $albumIndex",
                "artist - $albumIndex",
                "uri - $songIndex",
                0,
                0
            )
        }
    }
    AlbumList(albumList)
}