package com.mika.mymusicapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.songPlayer

/** 当前播放歌曲列表 */
@Composable
fun PlayingList(
    currentPlayerList: MutableList<SongInfo>,
    onClickPlayMode: () -> Unit,
    onCurrentSongChange: (SongInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn (modifier = modifier) {
        item {
            Row(
                modifier = Modifier
                    .clickable { onClickPlayMode() }
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val songsIconStyle = Modifier.size(30.dp)
                val playTextStyle = Modifier.padding(horizontal = 4.dp)
                val playMode = songPlayer!!.viewModel.playMode.observeAsState()
                when (playMode.value) {
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
        }
        items(currentPlayerList) { item -> SongItem(item, onCurrentSongChange,
            Modifier
                .height(48.dp)
                .fillMaxWidth()) }
        item { Spacer(Modifier.height(55.dp)) }
    }
}

@Composable
fun SongItem(item: SongInfo, onCurrentSongChange: (SongInfo) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clickable {
            onCurrentSongChange(item)
        },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(R.drawable.song_cover_test),
            "cover",
            Modifier
                .fillMaxHeight()
                .size(48.dp)
        )
        Column(
            Modifier
                .padding(horizontal = 4.dp)
        ) {
            Text(item.title, Modifier.fillMaxWidth(0.9f), maxLines = 1)
            Text(item.album, Modifier.fillMaxWidth(0.9f), maxLines = 1)
        }
        var showMenu by remember { mutableStateOf(false) }
        Column() {
            Icon(
                painterResource(R.drawable.more_buttom),
                "more",
                Modifier
                    .requiredSize(32.dp)
                    .clickable { showMenu = true }
            )
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.position)) }, onClick = { /*TODO*/ })
                DropdownMenuItem(text = { Text(stringResource(R.string.song_details)) }, onClick = { /*TODO*/ })
                DropdownMenuItem(text = { Text(stringResource(R.string.remove)) }, onClick = { /*TODO*/ })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SongsPreview() {
    val temSongsInfo = MutableList<SongInfo>(30) { index -> SongInfo(
        "Song - $index",
        "album - $index",
        "artist - $index",
        "test uri - $index",
        1,
        1
    )}
    PlayingList(temSongsInfo, {}, {}, Modifier.padding(8.dp, 0.dp))
}