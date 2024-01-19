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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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

/** 当前播放歌曲列表 */
@Composable
fun CurrentPlayingList(modifier: Modifier = Modifier) {
    val currentPlayList = songPlayer!!.viewModel.currentPlayList.collectAsState().value

    LazyColumn (modifier = modifier) {
        item {
            var showMenu by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .clickable { showMenu = !showMenu }
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val songsIconStyle = Modifier.size(30.dp)
                val playTextStyle = Modifier.padding(horizontal = 4.dp)
                val playMode by songPlayer!!.viewModel.playMode.collectAsState()

                when (playMode) {
                    PlayMode.SEQUENTIAL -> {
                        Icon(painterResource(R.drawable.songs_repeat), "list_repeat", songsIconStyle)
                        Text(stringResource(R.string.songs_loopplay), playTextStyle)
                    }
                    PlayMode.SHUFFLE -> {
                        Icon(painterResource(R.drawable.songs_shuffle), "random", songsIconStyle)
                        Text(stringResource(R.string.songs_shuffleplayback), playTextStyle)
                    }
                    else -> {
                        Icon(painterResource(R.drawable.songs_repeatone), "repeat_one", songsIconStyle)
                        Text(stringResource(R.string.songs_repeatone), playTextStyle)
                    }
                }
            }
            // 下拉菜单
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
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
        items(currentPlayList) { item ->
            SongItem(
                item,
                Modifier
                    .height(48.dp)
                    .fillMaxWidth()
            ) { expanded, onDismissRequest ->
                DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
                    DropdownMenuItem(text = { /*TODO*/ }, onClick = { /*TODO*/ })
                }
            }
        }
        item { Spacer(Modifier.height(55.dp)) }
    }
}



/** 单首歌曲项 */
@Composable
fun SongItem(
    item: SongInfo,
    modifier: Modifier = Modifier,
    dropdownMenu: @Composable ((expanded: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null
) {

    Row(
        modifier = modifier
            .heightIn(min = 32.dp)
            .clickable { songPlayer!!.changeCurrentSong(item) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 是否显示菜单
        var showMenu by remember { mutableStateOf(false) }

        Column {
            // 歌曲标题
            Text(
                item.title,
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(0.9f),
                fontSize = 18.sp,
                maxLines = 1
            )
            // 作者
            Text(
                item.artist,
                Modifier.padding(horizontal = 16.dp),
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        if (dropdownMenu != null) {
            Column {
                // 按下呼出下拉菜单
                Icon(
                    painterResource(R.drawable.more_buttom),
                    "more",
                    Modifier
                        .requiredSize(32.dp)
                        .clickable { showMenu = true }
                )
                dropdownMenu(showMenu, { showMenu = false })
            }
        }

    }

}

@Preview(showBackground = true)
@Composable
fun SongsPreview() {
    val songInfo = SongInfo("song1song1song1", "", "", "", 0, 0)
    SongItem(songInfo)
}