package com.mika.mymusicapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.songPlayer

/** 艺术家列表 */
@Composable
fun ArtistList(
    onItemClick: (List<SongInfo>) -> Unit,
    addToPlaylist: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {

    val artistListMap by songPlayer!!.viewModel.artistMap.collectAsState()

    Column(
        modifier
            .fillMaxHeight()
            .background(Color.White)
    ) {

        // 专辑展示方式: { 0 -> 条状, 1 -> 盒状 }
        var itemDisplay by remember { mutableIntStateOf(0) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (itemDisplay == 0) {
                IconButton(onClick = { itemDisplay = 1 }) {
                    Icon(painterResource(R.drawable.display_square), "square")
                }
            } else {
                IconButton(onClick = { itemDisplay = 0 }) {
                    Icon(painterResource(R.drawable.player_list), "list")
                }
            }
        }

        if (itemDisplay == 0) {
            LazyColumn {
                artistListMap.forEach {
                    item {
                        ArtistColumnItem(it.key, it.value, onItemClick, addToPlaylist, Modifier.fillMaxWidth())
                    }
                }
            }
        } else {
            LazyVerticalGrid(GridCells.Adaptive(minSize = 100.dp)) {
                artistListMap.forEach {
                    item {
                        ArtistGridItem(it.key, it.value, onItemClick, addToPlaylist)
                    }
                }
            }
        }

        Spacer(Modifier.height(55.dp))

    }

}

@Composable
fun ArtistColumnItem(
    title: String,
    dataList: List<SongInfo>,
    onItemClick: (List<SongInfo>) -> Unit,
    selectAlbumToAdd: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clickable { onItemClick(dataList) },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // 显示菜单
        var showDropDownMenu: Boolean by remember { mutableStateOf(false) }

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
            Text(title, maxLines = 2)
        }

        Column {

            // 调出下拉菜单
            IconButton(onClick = { showDropDownMenu = true }) {
                Icon(painterResource(R.drawable.more_buttom), "more detail")
            }

            // 下拉菜单
            ArtistDropdownMenu(showDropDownMenu, dataList, { showDropDownMenu = false }, selectAlbumToAdd)

        }

    }

}

@Composable
fun ArtistGridItem(
    title: String,
    dataList: List<SongInfo>,
    onItemClick: (List<SongInfo>) -> Unit,
    selectAlbumToAdd: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .padding(2.dp)
            .fillMaxWidth()
            .clickable { onItemClick(dataList) }
    ) {

        var showMenu: Boolean by remember { mutableStateOf(false) }

        Column(
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
                    text = title,
                    modifier = Modifier.heightIn(min = 34.dp),
                    maxLines = 2
                )
            }

            // 下拉菜单
            ArtistDropdownMenu(showMenu, dataList, { showMenu = false }, selectAlbumToAdd)

        }
    }
}

/** 专辑用下拉菜单 */
@Composable
fun ArtistDropdownMenu(
    expanded: Boolean,
    songsData: List<SongInfo>,
    onDismissRequest: () -> Unit,
    onAddToPlaylistHandler: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier
    ) {
        // 全部加入当前歌单
        DropdownMenuItem(
            text = { Text(stringResource(R.string.add_to_current_playlist)) },
            onClick = {
                songPlayer!!.addToCurrentPlaylist(songsData)
                onDismissRequest()
            }
        )
        // 全部加入其他歌单
        DropdownMenuItem(
            text = { Text(stringResource(R.string.add_other_playlist)) },
            onClick = {
                onAddToPlaylistHandler(songsData)
                onDismissRequest()
            }
        )
        // 从设备中删除
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete_from_device)) },
            onClick = { /*TODO*/ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ArtistListPreview() {

    val artistList = MutableList(8) {artistIndex ->
        MutableList(4) { songIndex ->
            SongInfo(
                "song - $songIndex",
                "album - $artistIndex",
                "artist - $artistIndex",
                "uri - $songIndex",
                0,
                0
            )
        }
    }
    val artist = MutableList(4) { songIndex ->
        SongInfo(
            "song - $songIndex",
            "album - 1",
            "artist - 1 artist - 1 artist - 1 artist - 1 artist - 1 artist - 1 artist - 1 ",
            "uri - $songIndex",
            0,
            0
        )
    }
    artistList.add(1, artist)

}