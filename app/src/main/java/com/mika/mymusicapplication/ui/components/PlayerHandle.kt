package com.mika.mymusicapplication.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.alertDialogBuilder
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.songPlayer
import com.mika.mymusicapplication.ui.theme.Pink80
import com.mika.mymusicapplication.ui.theme.Purple40
import com.mika.mymusicapplication.ui.theme.White
import kotlinx.coroutines.delay


/** 底部播放控制 */
@Composable
fun BottomPlayer(
    onFooterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row (
        modifier = modifier
            .clickable { onFooterClick() }
            .height(65.dp)
            .background(Purple40)
            .height(64.dp)
            .background(Pink80),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 从ViewModel中获取当前是否播放中
        val isPlaying by songPlayer!!.viewModel.isPlaying.collectAsState()
        // 从ViewModel中获取当前歌曲
        val currentSongList by songPlayer!!.viewModel.currentPlayList.collectAsState()
        val currentSongIndex by songPlayer!!.viewModel.currentPlayIndex.collectAsState()
        val currentSong = currentSongList[currentSongIndex]

        // 歌曲封面
        Icon(
            painterResource(R.drawable.song_cover_test),
            "cover",
            Modifier
                .size(64.dp)
                .background(White)
        )

        Column(
            Modifier
                .padding(horizontal = 4.dp)
                .weight(0.6f)
        ) {
            // 歌曲标题
            Text(
                text = currentSong.title,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            // 歌曲专辑
            Text(
                text = currentSong.album,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
        }

        // 播放上一首
        IconButton(
            onClick = songPlayer!!::changeToPrevious,
            modifier = Modifier.weight(0.1f)
        ) {
            Icon(painterResource(R.drawable.player_previous), "previous")
        }

        // 播放/暂停
        IconButton(
            onClick = songPlayer!!::changePlayState,
            modifier = Modifier.weight(0.1f)
        ) {
            Icon(
                if(isPlaying)
                    painterResource(R.drawable.player_pause)
                else
                    painterResource(R.drawable.player_playarrow),
                "pauseOrPlay"
            )
        }

        // 播放下一首
        IconButton(
            onClick = songPlayer!!::changeToNext,
            modifier = Modifier.weight(0.1f)
        ) {
            Icon(painterResource(R.drawable.player_next), "next")
        }
    }
}

/** 歌曲详细 */
@Composable
fun SongPage(
    onBackButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val isPlaying by songPlayer!!.viewModel.isPlaying.collectAsState()
    // 从ViewModel中获取当前歌曲
    val currentSongList by songPlayer!!.viewModel.currentPlayList.collectAsState()
    val currentSongIndex by songPlayer!!.viewModel.currentPlayIndex.collectAsState()
    val currentSong = currentSongList[currentSongIndex]
    val seekToPosition: (Float) -> Unit = { songPlayer!!.mediaPlayer!!.seekTo((songPlayer!!.mediaPlayer!!.duration.toFloat() * it).toInt()) }

    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 顶栏
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 返回上一页按钮
            IconButton(onClick = { onBackButtonClick() }, modifier = modifier.weight(0.15f)) {
                Icon(
                    painterResource(R.drawable.player_backarrow),
                    "back"
                )
            }

            // 显示当前歌曲标题和作者
            Column(modifier = modifier.weight(0.7f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentSong.title, maxLines = 1)
                Text(currentSong.artist, maxLines = 1)
            }

            // 呼出菜单
            IconButton(onClick = { /*TODO*/ },
                Modifier
                    .weight(0.15f)
                    .align(Alignment.Bottom)) {
                Icon(
                    painterResource(R.drawable.more_buttom),
                    "menu_trigger"
                )
            }

        }

        // 歌曲封面
        Image(
            painterResource(R.drawable.song_cover_test),
            "cover",
            Modifier
                .padding(8.dp, 8.dp)
                .background(Color.Gray)
                .size(350.dp)
        )

        // 进度条
        PlayerSlider(
            onValueChange = { seekToPosition(it) },
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        // 底端操作栏
        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // 播放模式
            val playMode by songPlayer!!.viewModel.playMode.collectAsState()
            // 是否展示菜单
            var showMenu by remember { mutableStateOf(false) }
            val str: String = stringResource(
                R.string.song_alert,
                currentSong.title,
                currentSong.album,
                currentSong.artist,
                currentSong.uri,
                currentSong.duration,
                currentSong.size
            )

            // 切换播放模式按钮
            IconButton(onClick = { songPlayer!!.changePlayMode() }) {
                Icon(
                    painter = painterResource(
                        when (playMode) {
                            PlayMode.SEQUENTIAL -> { R.drawable.songs_repeat }
                            PlayMode.SHUFFLE -> { R.drawable.songs_shuffle }
                            else -> { R.drawable.songs_repeatone }
                        }
                    ),
                    "loop"
                )
            }

            // 播放上一首按钮
            IconButton(onClick = songPlayer!!::changeToPrevious) {
                Icon(
                    painterResource(R.drawable.player_previous),
                    "previous"
                )
            }

            // 播放/暂停按钮
            IconButton(onClick = songPlayer!!::changePlayState) {
                Icon(
                    if(isPlaying)
                        painterResource(R.drawable.player_pause)
                    else
                        painterResource(R.drawable.player_playarrow),
                    "pauseOrPlay"
                )
            }

            // 播放下一首按钮
            IconButton(onClick = songPlayer!!::changeToNext) {
                Icon(
                    painterResource(R.drawable.player_next),
                    "next"
                )
            }

            // 显示播放列表按钮
            IconButton(onClick = {
                val alertDialog = alertDialogBuilder
                alertDialog!!.setMessage(str)
                alertDialog.show()
            }) {
                Icon(
                    painterResource(R.drawable.player_list),
                    "list"
                )
            }

        }
    }
}

/** 进度条 */
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun PlayerSlider(
    onValueChange: (Float) -> Unit, // 当进度条被拖动时触发的函数
    modifier: Modifier = Modifier
) {
    // float形式的进度
    var position by remember { mutableFloatStateOf(songPlayer!!.getPosition()) }
    // 每隔一秒更新一次进度条
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000)
            position = songPlayer!!.getPosition()
        }
    }
    // 进度条组件
    Slider(
        value = position,
        onValueChange =  onValueChange,
        modifier = modifier
    )
}
