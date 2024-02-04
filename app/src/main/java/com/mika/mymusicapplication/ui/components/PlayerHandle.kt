package com.mika.mymusicapplication.ui.components

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.mika.mymusicapplication.util.PlayMode
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
        // 从ViewModel中获取当前播放列表
        val currentSongList by songPlayer!!.viewModel.currentPlayList.collectAsState()
        // 从ViewModel中获取当前歌曲索引
        val currentSongIndex by songPlayer!!.viewModel.currentPlayIndex.collectAsState()

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
                text =
                    if (currentSongList.isNotEmpty() && currentSongIndex >= 0)
                        currentSongList[currentSongIndex].title
                    else
                        "",
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            // 歌曲专辑
            Text(
                text =
                    if (currentSongList.isNotEmpty() && currentSongIndex >= 0)
                        currentSongList[currentSongIndex].album
                    else
                        "",
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
        }

        // 播放上一首
        IconButton(
            enabled = currentSongList.isNotEmpty() && currentSongIndex >= 0,
            onClick = songPlayer!!::changeToPrevious,
            modifier = Modifier.weight(0.1f)
        ) {
            Icon(painterResource(R.drawable.player_previous), "previous")
        }

        // 播放/暂停
        IconButton(
            enabled = currentSongList.isNotEmpty() && currentSongIndex >= 0,
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
            enabled = currentSongList.isNotEmpty() && currentSongIndex >= 0,
            onClick = songPlayer!!::changeToNext,
            modifier = Modifier.weight(0.1f)
        ) {
            Icon(painterResource(R.drawable.player_next), "next")
        }

    }
}

/** 歌曲播放控制界面 */
@Composable
fun SongPage(
    onBackButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 从ViewModel中获取当前歌曲
        val currentSongList by songPlayer!!.viewModel.currentPlayList.collectAsState()
        val currentSongIndex by songPlayer!!.viewModel.currentPlayIndex.collectAsState()
        val currentSong: SongInfo? =
            if (currentSongList.isNotEmpty() && currentSongIndex >= 0)
                currentSongList[currentSongIndex]
            else
                null

        // 顶栏
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {


            IconButton(// 返回上一页按钮
                onClick = { onBackButtonClick() },
                modifier = modifier.weight(0.15f)
            ) {
                Icon(
                    painterResource(R.drawable.player_backarrow),
                    "back"
                )
            }

            Column(// 显示当前歌曲标题和作者
                modifier = modifier.weight(0.7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentSong != null) {
                    Text(currentSong.title, maxLines = 1)
                    Text(currentSong.artist, maxLines = 1)
                } else {
                    Text("", maxLines = 1)
                    Text("", maxLines = 1)
                }
            }

            IconButton(// 呼出菜单
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .weight(0.15f)
                    .align(Alignment.Bottom)
            ) {
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
            onValueChange = songPlayer!!::seekToTargetPosition,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Row(// 底端操作栏
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            val playMode by songPlayer!!.viewModel.playMode.collectAsState()// 播放模式
            val context = LocalContext.current// 获取当前上下文

            IconButton(onClick = { songPlayer!!.changePlayMode() }) {// 切换播放模式按钮
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

            IconButton(// 播放上一首按钮
                enabled = currentSongList.isNotEmpty() && currentSongIndex >= 0,
                onClick = songPlayer!!::changeToPrevious
            ) {
                Icon(
                    painterResource(R.drawable.player_previous),
                    "previous"
                )
            }

            IconButton(// 播放/暂停按钮
                enabled = currentSongList.isNotEmpty() && currentSongIndex >= 0,
                onClick = songPlayer!!::changePlayState
            ) {
                val isPlaying by songPlayer!!.viewModel.isPlaying.collectAsState()
                Icon(
                    if(isPlaying)
                        painterResource(R.drawable.player_pause)
                    else
                        painterResource(R.drawable.player_playarrow),
                    "pauseOrPlay"
                )
            }

            IconButton(onClick = songPlayer!!::changeToNext) {// 播放下一首按钮
                Icon(
                    painterResource(R.drawable.player_next),
                    "next"
                )
            }

            IconButton(// 显示播放列表按钮
                enabled = currentSong != null,
                onClick = { showSongInfo(context, currentSong) }
            ) {
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
    modifier: Modifier = Modifier,
) {

    var position by remember { mutableFloatStateOf(songPlayer!!.getPosition()) }// float形式的进度

    LaunchedEffect(key1 = true) {// 每隔一秒更新一次进度条
        while (true) {
            delay(1000)
            position = songPlayer!!.getPosition()
        }
    }

    Slider(// 进度条组件
        value = position,
        onValueChange =  onValueChange,
        modifier = modifier
    )

}

/** 弹出对话框显示歌曲信息 */
fun showSongInfo(context: Context, currentSong: SongInfo?) {

    if (currentSong == null) {// 当歌曲为空时
        return
    }

    val str: String = context.getString(// 获取字符串资源
        R.string.song_alert,
        currentSong.title,
        currentSong.album,
        currentSong.artist,
        currentSong.uri,
        currentSong.duration,
        currentSong.size,
    )

    val dialog = alertDialogBuilder// 获取对话框对象
    dialog.setMessage(str)// 设置信息内容
    dialog.show()// 弹出对话框

}

/** 显示当前播放列表 */
/*@Composable
fun CurrentPlayList(playList: MutableList<SongInfo>, modifier: Modifier = Modifier) {
    LazyColumn(modifier.padding(start = 8.dp)) {
        items(playList) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 是否显示菜单
                var showMenu by remember { mutableStateOf(false) }
                // 歌曲信息
                Text(it.title, Modifier.weight(0.6f))
                Text(it.artist, Modifier.weight(0.3f))
                // 按钮及其呼出的下拉菜单
                Column(Modifier.weight(0.1f)) {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(painter = painterResource(id = R.drawable.more_buttom), contentDescription = "more action")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.song_details)) }, onClick = { /*TODO*/ })
                        DropdownMenuItem(text = { Text(stringResource(R.string.remove)) }, onClick = { /*TODO*/ })
                    }
                }
            }
        }
    }
}*/

