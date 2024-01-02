package com.mika.mymusicapplication.ui.components

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.MediaPlayer
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.alertDialogBuilder
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.songPlayer
import com.mika.mymusicapplication.ui.theme.Pink80
import com.mika.mymusicapplication.ui.theme.Purple40
import com.mika.mymusicapplication.ui.theme.White
import com.mika.mymusicapplication.viewModel.MyViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay

/** 播放顺序模式 */
enum class PlayMode {
    SEQUENTIAL, // 列表顺序播放
    SHUFFLE,    // 列表随机播放
    LOOP        // 单曲循环
}

/** 播放控制模块 */
class SongPlayer(
    var currentSongIndex: Int,
    var currentPlayList: MutableList<SongInfo>,
    var albumList: MutableList<MutableList<SongInfo>>,
    var mediaPlayer: MediaPlayer?,
    var viewModel: MyViewModel,
    var playMode: PlayMode = PlayMode.SEQUENTIAL,
) {
    init {
        viewModel.setIsPlaying(mediaPlayer!!.isPlaying)
        viewModel.setPlayMode(playMode)
        viewModel.setCurrentPlayIndex(currentSongIndex)
        viewModel.setCurrentPlayList(currentPlayList)
        viewModel.setAlbumList(albumList)
        mediaPlayer!!.setDataSource(currentPlayList[currentSongIndex].uri)
        mediaPlayer!!.setOnPreparedListener {
            it.start()
            viewModel.setIsPlaying(true)
            viewModel.setCurrentPlayIndex(currentSongIndex)
        }
        mediaPlayer!!.prepareAsync()
        // 设置完播侦听器
        mediaPlayer!!.setOnCompletionListener {
            when (playMode) {
                // 列表播放
                PlayMode.SEQUENTIAL -> {
                    mediaPlayer!!.reset()
                    if(currentSongIndex + 1 < currentPlayList.count()) {
                        currentSongIndex += 1
                        mediaPlayer!!.setDataSource(currentPlayList[currentSongIndex].uri)
                    } else {
                        currentSongIndex = 0
                        mediaPlayer!!.setDataSource(currentPlayList[currentSongIndex].uri)
                    }
                    mediaPlayer!!.prepareAsync()
                }

                // 列表随机
                PlayMode.SHUFFLE -> {
                    mediaPlayer!!.reset()
                    var targetSongIndex: Int
                    do {
                        targetSongIndex = (0 until currentPlayList.count()).random()
                    } while (targetSongIndex == currentSongIndex)
                    currentSongIndex = targetSongIndex
                    mediaPlayer!!.setDataSource(currentPlayList[currentSongIndex].uri)
                    mediaPlayer!!.prepareAsync()
                }

                // 单曲循环
                else -> {
                    mediaPlayer!!.start()
                    viewModel.setIsPlaying(true)
                }
            }
        }
    }

/*    /** 设置当前播放歌曲在列表中的索引 */
    fun setCurrentSongIndex(targetSongIndex: Int) {
        this.currentSongIndex = targetSongIndex
    }

    /** 设置当前播放列表 */
    fun setCurrentPlayList(targetPlayList: MutableList<SongInfo>) {
        this.currentPlayList = targetPlayList
    }

    /** 设置播放模式（列表顺序播放、列表随机播放、单曲循环） */
    fun changePlayMode(playMode: PlayMode) {
        this.playMode = playMode
    }*/

    /** 切换播放状态 */
    fun changePlayState() {
        if(mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            viewModel.setIsPlaying(false)
        } else {
            mediaPlayer!!.start()
            viewModel.setIsPlaying(true)
        }
    }

    /** 切换到指定播放模式 */
    fun changePlayMode(playMode: PlayMode) {
        viewModel.setPlayMode(playMode)
    }

    /**
     * 切换到下一个播放模式
     * 顺序: 列表循环 -> 列表随机 -> 单曲循环 -> 列表循环
     * */
    fun changePlayMode() {
        when(viewModel.playMode.value!!) {
            PlayMode.SEQUENTIAL -> {
                this.playMode = PlayMode.SHUFFLE
                changePlayMode(PlayMode.SHUFFLE)
            }
            PlayMode.SHUFFLE -> {
                this.playMode = PlayMode.LOOP
                changePlayMode(PlayMode.LOOP)
            }
            else -> {
                this.playMode = PlayMode.SEQUENTIAL
                changePlayMode(PlayMode.SEQUENTIAL)
            }
        }
    }

    /** 根据索引更改当前播放歌曲 */
    fun changeCurrentSong(songIndex: Int) {
        currentSongIndex = songIndex
        mediaPlayer!!.reset()
        mediaPlayer!!.setDataSource(currentPlayList[songIndex].uri)
        mediaPlayer!!.prepareAsync()
    }

    /** 根据歌曲信息更改当前播放歌曲 */
    fun changeCurrentSong(songInfo: SongInfo) {
        var targetSongIndex = currentPlayList.indexOfFirst { songInfo.uri == it.uri }
        if(targetSongIndex == -1) {
            targetSongIndex = currentPlayList.count()
            currentPlayList.add(songInfo)
        }
        changeCurrentSong(targetSongIndex)
    }

    /** 播放上一首歌曲 */
    fun changeToPrevious() {
        val targetSongIndex: Int
        if(currentSongIndex <= 0) {
            targetSongIndex = currentPlayList.count() - 1
        } else {
            targetSongIndex = currentSongIndex - 1
        }
        changeCurrentSong(targetSongIndex)
    }

    /** 播放下一首歌曲 */
    fun changeToNext() {
        val targetSongIndex: Int
        if(currentSongIndex >= currentPlayList.size - 1 ) {
            targetSongIndex = 0
        } else {
            targetSongIndex = currentSongIndex + 1
        }
        changeCurrentSong(targetSongIndex)
    }
}

/** 底部播放控制 */
@Composable
fun BottomPlayer(
    currentSongInfo: SongInfo,
    isPlaying: Boolean,
    onFooterClick: () -> Unit,
    onPlayButtonClick: () -> Unit,
    onChangeToPrevious: () -> Unit,
    onChangeToNext: () -> Unit,
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
                text = currentSongInfo.title,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            // 歌曲专辑
            Text(
                text = currentSongInfo.album,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
        }

        // 播放上一首
        IconButton(
            onClick = onChangeToPrevious,
            modifier = Modifier.weight(0.1f)
        ) {
            Icon(painterResource(R.drawable.player_previous), "previous")
        }

        // 播放/暂停
        IconButton(
            onClick = onPlayButtonClick,
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
            onClick = onChangeToNext,
            modifier = Modifier.weight(0.1f)
        ) {
            Icon(painterResource(R.drawable.player_next), "next")
        }
    }
}

/** 歌曲详细 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SongPage(
    songInfo: SongInfo,
    isPlaying: Boolean,
    onBackButtonClick: () -> Unit,
    getPosition: () -> Float,
    onSliderChange: (Float) -> Unit,
    onPlayButtonClick: () -> Unit,
    onChangeToPrevious: () -> Unit,
    onChangeToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                Text(songInfo.title, maxLines = 1)
                Text(songInfo.artist, maxLines = 1)
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
            getPosition = getPosition,
            onValueChange = { onSliderChange(it) },
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
            val playMode = songPlayer!!.viewModel.playMode.observeAsState().value
            // 是否展示菜单
            var showMenu by remember { mutableStateOf(false) }
            val str: String = stringResource(
                R.string.song_alert,
                songInfo.title,
                songInfo.album,
                songInfo.artist,
                songInfo.uri,
                songInfo.duration,
                songInfo.size
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
            IconButton(onClick = { onChangeToPrevious() }) {
                Icon(
                    painterResource(R.drawable.player_previous),
                    "previous"
                )
            }
            // 播放/暂停按钮
            IconButton(onClick = { onPlayButtonClick() }) {
                Icon(
                    if(isPlaying)
                        painterResource(R.drawable.player_pause)
                    else
                        painterResource(R.drawable.player_playarrow),
                    "pauseOrPlay"
                )
            }
            // 播放下一首按钮
            IconButton(onClick = { onChangeToNext() }) {
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
    getPosition: () -> Float, // 获取当前位置的函数
    onValueChange: (Float) -> Unit, // 当进度条被拖动时触发的函数
    modifier: Modifier = Modifier
) {
    // float形式的进度
    var position by remember { mutableFloatStateOf(getPosition()) }

    // 每隔一秒更新一次进度条
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000)
            position = getPosition()
        }
    }

    // 进度条组件
    Slider(
        value = position,
        onValueChange =  onValueChange,
        modifier = modifier,
        colors = SliderDefaults.colors()
    )
}

/** 显示当前播放列表 */
@Composable
fun PlayList(playList: MutableList<SongInfo>, modifier: Modifier = Modifier) {
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
}

@Preview(showBackground = true)
@Composable
fun SongPlayerPreview() {
//    val temSongInfo = SongInfo(
//        "title - test",
//        "album - test",
//        "artist - test",
//        "uri - test",
//        1,
//        1
//    )
//
//    SongPage(temSongInfo, true,  {}, { 0f }, {}, {}, {}, {})

    val playList = MutableList(20) { songInfo ->
        SongInfo(
            "song - $songInfo",
            "album - 1",
            "artist - 1",
            "uri - 1",
            0,
            0
            )
    }
    PlayList(playList)
}
