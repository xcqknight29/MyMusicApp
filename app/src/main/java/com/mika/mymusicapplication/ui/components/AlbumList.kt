package com.mika.mymusicapplication.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as columnItems
import androidx.compose.material.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        // 显示菜单
        var showMenu: Boolean by remember { mutableStateOf(false) }
        // 专辑封面
        Image(
            painterResource(R.drawable.song_cover_test),
            "cover",
            Modifier
                .background(Color.Gray)
                .width(40.dp)
                .aspectRatio(1f),
        )
        Column(Modifier.fillMaxWidth(0.8f)) {
            // 专辑标题
            Text(album[0].album, maxLines = 1)
            // 专辑作者
            Text(album[0].artist, maxLines = 1)
        }
        Column {
            // 调出下拉菜单
            IconButton(onClick = { showMenu = true }) {
                Icon(painterResource(R.drawable.more_buttom), "more detail")
            }
            // 下拉菜单
            AlbumDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false })
        }
    }
}

@Composable
fun AlbumGridItem(album: MutableList<SongInfo>, modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(2.dp)
            .fillMaxWidth()) {
        var showMenu: Boolean by remember { mutableStateOf(false) }
        Column(
//            Modifier.combinedClickable(onClick = {}, onLongClick = {})
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { showMenu = !showMenu },
                    onPress = {  }
                )
            }
        ) {
            // 专辑封面
            Image(
                painterResource(R.drawable.song_cover_test),
                "cover",
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxWidth()
                    .aspectRatio(1f) // 固定长宽比
            )
            Column(verticalArrangement = Arrangement.SpaceAround) {
                // 专辑名称
                Text(
                    text = album[0].album,
                    modifier = Modifier.heightIn(min = 34.dp),
                    maxLines = 2
                )
                // 专辑作者
                Text(
                    text = album[0].artist,
                    maxLines = 1
                )
            }
            // 下拉菜单
            AlbumDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false })
        }
    }
}

/** 专辑用下拉菜单 */
@Composable
fun AlbumDropdownMenu(expanded: Boolean, onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier) {
        // 详情
        DropdownMenuItem(onClick = { /*TODO*/ }) {
            Text(stringResource(R.string.album_detail))
        }
        // 全部加入歌单
        DropdownMenuItem(onClick = { /*TODO*/ }) {
            Text(stringResource(R.string.add_playlist))
        }
        // 从设备中删除
        DropdownMenuItem(onClick = { /*TODO*/ }) {
            Text(stringResource(R.string.delete_album))
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
    val album = MutableList(4) { songIndex ->
        SongInfo(
            "song - $songIndex",
            "album - 1 album - 1 album - 1 album - 1 album - 1 album - 1 album - 1 album - 1",
            "artist - 1",
            "uri - $songIndex",
            0,
            0
        )
    }
    albumList.add(1, album)
    AlbumList(albumList)
}