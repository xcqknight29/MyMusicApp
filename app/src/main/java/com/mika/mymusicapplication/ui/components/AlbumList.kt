package com.mika.mymusicapplication.ui.components

import android.content.Context
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.alertDialogBuilder
import com.mika.mymusicapplication.model.AlbumInfo
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.songPlayer

/** 专辑列表 */
@Composable
fun AlbumList(
    onItemClick: (List<SongInfo>) -> Unit,
    addToPlaylist: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier
) {

    val albumList by songPlayer!!.viewModel.albumList.collectAsState()

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
                albumList.forEach {
                    item {
                        AlbumColumnItem(it, onItemClick, addToPlaylist, Modifier.fillMaxWidth())
                    }
                }
            }

        } else {

            LazyVerticalGrid(GridCells.Adaptive(minSize = 100.dp)) {
                albumList.forEach {
                    item {
                        AlbumGridItem(it, onItemClick, addToPlaylist)
                    }
                }
            }

        }

        Spacer(Modifier.height(55.dp))

    }

}

@Composable
fun AlbumColumnItem(
    itemData: AlbumInfo,
    onItemClick: (List<SongInfo>) -> Unit,
    selectAlbumToAdd: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clickable { onItemClick(itemData.songList) },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // 显示菜单
        var showDropDownMenu: Boolean by remember { mutableStateOf(false) }

        // 专辑封面
        Image(
            bitmap = itemData.thumbnail!!.asImageBitmap(),
            contentDescription = "album cover",
            modifier = Modifier
                .background(Color.Gray)
                .width(40.dp)
                .aspectRatio(1f),
        )

        Column(Modifier.fillMaxWidth(0.8f)) {
            // 专辑标题
            Text(text = itemData.title, fontSize = 18.sp, maxLines = 1)
            // 专辑作者
            Text(text = itemData.artist, fontSize = 14.sp, maxLines = 1)
        }

        Column {

            // 调出下拉菜单
            IconButton(onClick = { showDropDownMenu = true }) {
                Icon(painterResource(R.drawable.more_buttom), "more detail")
            }

            // 下拉菜单
            AlbumDropdownMenu(showDropDownMenu, itemData.songList, { showDropDownMenu = false }, selectAlbumToAdd)

        }

    }

}

@Composable
fun AlbumGridItem(
    itemData: AlbumInfo,
    onItemClick: (List<SongInfo>) -> Unit,
    selectAlbumToAdd: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .padding(2.dp)
            .fillMaxWidth()
    ) {

        var showMenu: Boolean by remember { mutableStateOf(false) }

        Column(
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { showMenu = !showMenu },
                    onPress = { onItemClick(itemData.songList) }
                )
            }
        ) {

            // 专辑封面
            Image(
                itemData.thumbnail!!.asImageBitmap(),
                "cover",
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxWidth()
                    .aspectRatio(1f) // 固定长宽比
            )

            Column(verticalArrangement = Arrangement.SpaceAround) {
                // 专辑名称
                Text(
                    text = itemData.title,
                    modifier = Modifier.heightIn(min = 34.dp),
                    maxLines = 2
                )
            }

            // 下拉菜单
            AlbumDropdownMenu(showMenu, itemData.songList, { showMenu = false }, selectAlbumToAdd)

        }
    }
}

/** 专辑用下拉菜单 */
@Composable
fun AlbumDropdownMenu(
    expanded: Boolean,
    songsData: List<SongInfo>,
    onDismissRequest: () -> Unit,
    onAddToPlaylistHandler: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier
) {

    // 获取上下文
    val currentContext = LocalContext.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {

        // 详情
        DropdownMenuItem(
            text = { Text(stringResource(R.string.album_detail)) },
            onClick = { showAlbumDetail(currentContext, songsData) }
        )

        // 全部加入当前播放列表
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

/** 显示专辑详情 */
fun showAlbumDetail(context: Context, albumSongList: List<SongInfo>) {
    // 从上下文资源中获取字符串并将其格式化
    val str: String =
        context.resources.getString(R.string.album_info)
            .format(albumSongList[0].album, albumSongList.size, albumSongList[0].artist)
    // 获取对话框对象
    val alertDialog = alertDialogBuilder
    // 设置对话框的文本
    alertDialog.setMessage(str)
    // 显示对话框
    alertDialog.show()
}

@Preview(showBackground = true)
@Composable
fun AlbumListPreview() {

//    val albumList = MutableList(8) { albumIndex ->
//        MutableList(4) { songIndex ->
//            SongInfo(
//                "song - $songIndex",
//                "album - $albumIndex",
//                "artist - $albumIndex",
//                "uri - $songIndex",
//                0,
//                0
//            )
//        }
//    }

//    val album = MutableList(4) { songIndex ->
//        SongInfo(
//            "song - $songIndex",
//            "album - 1 album - 1 album - 1 album - 1 album - 1 album - 1 album - 1 album - 1",
//            "artist - 1",
//            "uri - $songIndex",
//            0,
//            0
//        )
//    }

//    albumList.add(1, album)

//    val albumList = List(4) { songIndex ->
//        SongInfo(
//            "song1",
//            "album1",
//            "artist1",
//            "uri1",
//            null,
//            null,
//            0,
//            0
//        )
//    }
//
//    AlbumColumnItem("song1", albumList, {}, {})

}