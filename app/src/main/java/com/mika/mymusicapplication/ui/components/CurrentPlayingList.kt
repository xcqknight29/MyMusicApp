package com.mika.mymusicapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.songPlayer
import com.mika.mymusicapplication.util.PlayMode

/** 当前播放歌曲列表 */
@Composable
fun CurrentPlayingList(modifier: Modifier = Modifier) {

    val currentPlayList by songPlayer!!.viewModel.currentPlayList.collectAsState()

    LazyColumn (modifier = modifier) {

        item {
            PlayModeDropdownMenu()
        }

        items(currentPlayList) { item ->
            Row {

                SongItem(
                    item,
                    Modifier
                        .height(48.dp)
                )

                Column(
                    modifier = Modifier.height(48.dp),
                    verticalArrangement = Arrangement.Center,
                ) {

                    var showMenu by remember { mutableStateOf(false) }

                    // 按下呼出下拉菜单
                    Icon(
                        painterResource(R.drawable.more_buttom),
                        "more",
                        Modifier
                            .requiredSize(32.dp)
                            .clickable { showMenu = true }
                    )

                    CurrentListDropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        itemData = item,
                    )

                }

            }

        }

        item { Spacer(Modifier.height(55.dp)) }

    }

}

/** 单首歌曲项 */
@Composable
fun SongItem(
    itemData: SongInfo,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier
            .heightIn(min = 32.dp)
            .clickable { songPlayer!!.changeCurrentSong(itemData) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            // 歌曲标题
            Text(
                itemData.title,
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(0.9f),
                fontSize = 18.sp,
                maxLines = 1
            )
            // 作者
            Text(
                itemData.artist,
                Modifier.padding(horizontal = 16.dp),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }

}

/** PlayMode menu  */
@Composable
fun PlayModeDropdownMenu(modifier: Modifier = Modifier) {

    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable { showMenu = !showMenu }
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val songsIconStyle = Modifier.size(30.dp)
        val playTextStyle = Modifier.padding(horizontal = 4.dp)
        val playMode by songPlayer!!.viewModel.playMode.collectAsState()

        Icon(painterResource(
            when (playMode) {
                PlayMode.SEQUENTIAL -> {R.drawable.songs_repeat}
                PlayMode.SHUFFLE -> {R.drawable.songs_shuffle}
                else -> {R.drawable.songs_repeatone}
            }
        ), "icon", songsIconStyle)
        Text(stringResource(
            when (playMode) {
                PlayMode.SEQUENTIAL -> {R.string.songs_loopplay}
                PlayMode.SHUFFLE -> {R.string.songs_shuffleplayback}
                else -> {R.string.songs_repeatone}
            }
        ), playTextStyle)

        // 下拉菜单
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {

            DropdownMenuItem(
                text = { Text(stringResource(R.string.songs_loopplay)) },
                onClick = {
                    songPlayer!!.changePlayMode(PlayMode.SEQUENTIAL)
                    showMenu = false
                },
                leadingIcon = { Icon(painterResource(R.drawable.songs_repeat), "list_repeat") }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.songs_shuffleplayback)) },
                onClick = {
                    songPlayer!!.changePlayMode(PlayMode.SHUFFLE)
                    showMenu = false
                },
                leadingIcon = { Icon(painterResource(R.drawable.songs_shuffle), "random") }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.songs_repeatone)) },
                onClick = {
                    songPlayer!!.changePlayMode(PlayMode.LOOP)
                    showMenu = false
                },
                leadingIcon = { Icon(painterResource(R.drawable.songs_repeatone), "repeat_one") }
            )

        }

    }

}

/** 下降菜单 */
@Composable
fun CurrentListDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    itemData: SongInfo,
    modifier: Modifier = Modifier,
) {

    var showDialog by remember { mutableStateOf(false) }

    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {

        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete)) },
            onClick = {
                songPlayer!!.deleteSongFromCurrent(itemData)// 删除当前歌曲
                onDismissRequest()
            },
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete_all)) },
            onClick = {
                showDialog = true
                onDismissRequest()
            },
        )

    }

    DeleteConfirmDialog(
        expended = showDialog,
        onDismissRequest = { showDialog = false },
    )

}

/** 确认删除全部歌曲对话框 */
@Composable
fun DeleteConfirmDialog(
    expended: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {

    if (expended) {

        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            text = { Text(stringResource(R.string.delete_all_text)) },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    songPlayer!!.deleteAllFromCurrent()
                    onDismissRequest()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
        )

    }

}

@Preview(showBackground = true)
@Composable
fun SongsPreview() {

}