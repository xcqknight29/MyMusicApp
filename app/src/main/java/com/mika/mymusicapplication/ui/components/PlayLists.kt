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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.dp
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.model.Playlist
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.songPlayer

/** 播单展示 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Playlists(
    onItemClick: (List<SongInfo>) -> Unit,
    addToOtherHandler: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier
) {

    // 是否显示对话框
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(floatingActionButton = {
        SmallFloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }) {

        Column(
            modifier
                .padding(it)
                .fillMaxHeight()
                .background(Color.White)
        ) {

            // 播单展示方式: { 0 -> 条状, 1 -> 盒状 }
            var itemDisplay by remember { mutableIntStateOf(0) }
            // 从ViewModel中获取播单Map
            val playlists by songPlayer!!.viewModel.playlistList.collectAsState()

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
                    playlists.forEach {
                        item {
                            PlaylistColumnItem(it, onItemClick, Modifier.fillMaxWidth(), selectPlaylistToAdd = addToOtherHandler)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(GridCells.Adaptive(minSize = 100.dp)) {
                    playlists.forEach {
                        item {
                            PlaylistGridItem(it, onItemClick, selectPlaylistToAdd =  addToOtherHandler)
                        }
                    }
                }
            }

            Spacer(Modifier.height(55.dp))

        }

        if (showDialog) {
            AddPlaylistDialog({ showDialog = false })
        }

    }

}

@Composable
fun PlaylistColumnItem(
    itemData: Playlist,
    onItemClick: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier,
    selectPlaylistToAdd: ((List<SongInfo>) -> Unit)? = null,
) {

    Row(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clickable { onItemClick(itemData.data!!) },
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
            Text(itemData.name, maxLines = 2)
        }

        Column {

            // 调出下拉菜单
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(painterResource(R.drawable.more_buttom), "more detail")
            }

            // 下拉菜单
            PlaylistDropdownMenu(
                expanded = showMenu,
                playlistName = itemData.name,
                songsData = itemData.data!!,
                onDismissRequest = { showMenu = false },
                addToOtherHandler = selectPlaylistToAdd
            )

        }

    }

}

@Composable
fun PlaylistGridItem(
    itemData: Playlist,
    onItemClick: (List<SongInfo>) -> Unit,
    modifier: Modifier = Modifier,
    selectPlaylistToAdd: ((List<SongInfo>) -> Unit)? = null,
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
                    onPress = { onItemClick(itemData.data!!) }
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

            }

            // 专辑名称
            Text(
                text = itemData.name,
                modifier = Modifier.heightIn(min = 34.dp),
                maxLines = 2
            )

            // 下拉菜单
            PlaylistDropdownMenu(
                expanded = showMenu,
                playlistName = itemData.name,
                songsData = itemData.data!!,
                onDismissRequest = { showMenu = false },
                addToOtherHandler = selectPlaylistToAdd
            )

        }
    }
}

/** 播单用下拉菜单 */
@Composable
fun PlaylistDropdownMenu(
    expanded: Boolean,
    playlistName: String,
    songsData: List<SongInfo>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    addToOtherHandler: ((List<SongInfo>) -> Unit)? = null,
) {

    var showDialog by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {

        // 全部加入其他歌单
        DropdownMenuItem(
            text = { Text(stringResource(R.string.add_other_playlist)) },
            onClick = { addToOtherHandler?.let { it(songsData) } }
        )

        // 全部加入当前歌单
        DropdownMenuItem(
            text = { Text(stringResource(R.string.add_to_current_playlist)) },
            onClick = { songPlayer!!.addToCurrentPlaylist(songsData) }
        )

        // 替换当前歌单
        DropdownMenuItem(
            text = { Text(stringResource(R.string.replace_current_playlist)) },
            onClick = { songPlayer!!.replaceCurrentPlaylist(songsData) }
        )

        // 删除播单
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete_playlist)) },
            onClick = { showDialog = !showDialog }
        )

    }

    if (showDialog) {
        DeletePlaylistDialog({ showDialog = false }, playlistName)
    }

}

/** 弹出此对话框创建新播单 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistDialog(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {

    var input by remember { mutableStateOf("new playlist") }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.add_new_playlist))
        },
        text = {
            // 新播单名输入框
            TextField(
                value = input,
                onValueChange = { input = it },
            )
        },
        dismissButton = {
            // 取消按钮
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            // 确认添加按钮
            TextButton(onClick = {
                songPlayer!!.addNewPlaylist(input)
                onDismissRequest()
            }) {
                Text(stringResource(R.string.add))
            }
        },
    )

}

/** 弹出此对话框确认删除播单 */
@Composable
fun DeletePlaylistDialog(
    onDismissRequest: () -> Unit,
    playlistName: String,
    modifier: Modifier = Modifier
) {

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(stringResource(R.string.delete_playlist_title, playlistName)) },
        text = { Text(stringResource(R.string.delete_playlist_text)) },
        dismissButton = {
            // 取消按钮
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            // 确认删除按钮
            TextButton(onClick = {
                songPlayer!!.deletePlaylist(playlistName)
                onDismissRequest()
            }) {
                Text(stringResource(R.string.delete))
            }
        }
    )

}

/** 选择playlist作为添加的目标 */
@Composable
fun PlaylistSelections(
    dataCount: Int,
    confirmPlaylistToAdd: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    // 显示对话框
    var showDialog by remember { mutableStateOf(false) }
    // 被选中作为添加目标的播单
    var targetPlaylistName by remember { mutableStateOf("") }

    // 选中一个播单作为添加目标
    val selectAddToPlaylist: (String) -> Unit = {
        targetPlaylistName = it
        showDialog = true
    }

    // 播单列表
    PlaylistList(onItemClick = selectAddToPlaylist, modifier = modifier)

    // 弹出对话框确认添加入播单
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            dismissButton = {
                IconButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.widthIn(min = 120.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                IconButton(
                    onClick = { confirmPlaylistToAdd(targetPlaylistName) },
                    modifier = Modifier.widthIn(min = 120.dp)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            text = { Text(stringResource(
                R.string.add_song_confirm,
                dataCount,
                targetPlaylistName
            )) },
        )
    }

}

/** item列表，可以为Album、Artist和Playlist */
@Composable
fun PlaylistList(
    modifier: Modifier = Modifier,
    onItemClick: ((String) -> Unit)? = null,
    dropdownMenu: @Composable ((Boolean, List<SongInfo>, () -> Unit) -> Unit)? = null
) {

    Column(
        modifier
            .fillMaxHeight()
            .background(Color.White)
    ) {

        // 专辑展示方式: { 0 -> 条状, 1 -> 盒状 }
        var itemDisplay by remember { mutableIntStateOf(0) }

        val playlistList by songPlayer!!.viewModel.playlistList.collectAsState()

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
                playlistList.forEach {
                    item {
                        PlaylistSelectionColumnItem(it, onItemClick, Modifier.fillMaxWidth(), dropdownMenu)
                    }
                }
            }
        } else {
            LazyVerticalGrid(GridCells.Adaptive(minSize = 100.dp)) {
                playlistList.forEach {
                    item {
                        PlaylistSelectionGridItem(it, onItemClick, Modifier, dropdownMenu)
                    }
                }
            }
        }

        Spacer(Modifier.height(55.dp))

    }

}

@Composable
fun PlaylistSelectionColumnItem(
    itemData: Playlist,
    onItemClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    dropdownMenu: (@Composable (Boolean, List<SongInfo>, () -> Unit) -> Unit)? = null,
) {

    Row(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clickable { onItemClick?.let { it(itemData.name) } },
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
            Text(itemData.name, maxLines = 2)
        }

        if (dropdownMenu != null) {
            Column {
                // 调出下拉菜单
                IconButton(onClick = { showMenu = true }) {
                    Icon(painterResource(R.drawable.more_buttom), "more detail")
                }
                // 下拉菜单
                dropdownMenu(showMenu, itemData.data!!) { showMenu = false }
            }
        }

    }

}

@Composable
fun PlaylistSelectionGridItem(
    itemData: Playlist,
    onItemClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    dropdownMenu: (@Composable (Boolean, List<SongInfo>, () -> Unit) -> Unit)? = null,
) {
    Box(
        modifier
            .padding(2.dp)
            .fillMaxWidth()
            .clickable { onItemClick?.let { it(itemData.name) } }
    ) {
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
                    text = itemData.name,
                    modifier = Modifier.heightIn(min = 34.dp),
                    maxLines = 2
                )
            }
            if (dropdownMenu != null) {
                // 下拉菜单
                dropdownMenu(showMenu, itemData.data!!) { showMenu = false }
            }
        }
    }
}