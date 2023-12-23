package com.mika.mymusicapplication.ui.components

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.ui.theme.Pink80
import com.mika.mymusicapplication.ui.theme.Purple40
import com.mika.mymusicapplication.ui.theme.White
import com.mika.mymusicapplication.viewModel.MyViewModel

val temSongInfo = SongInfo(
    "title - test",
    "album - test",
    "artist - test",
    "uri - test",
    1,
    1
)

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
        mediaPlayer!!.setDataSource(currentPlayList!![currentSongIndex!!].uri)
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
                    if(currentSongIndex!! + 1 < currentPlayList!!.count()) {
                        currentSongIndex = currentSongIndex!! + 1
                        mediaPlayer!!.setDataSource(currentPlayList!![currentSongIndex!!].uri)
                    } else {
                        currentSongIndex = 0
                        mediaPlayer!!.setDataSource(currentPlayList!![currentSongIndex!!].uri)
                    }
                    mediaPlayer!!.prepareAsync()
                }

                // 列表随机
                PlayMode.SHUFFLE -> {
                    mediaPlayer!!.reset()
                    var targetSongIndex: Int
                    do {
                        targetSongIndex = (0 until currentPlayList!!.count()).random()
                    } while (targetSongIndex == currentSongIndex)
                    currentSongIndex = targetSongIndex
                    mediaPlayer!!.setDataSource(currentPlayList!![currentSongIndex!!].uri)
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
        mediaPlayer!!.setDataSource(currentPlayList!![songIndex].uri)
        mediaPlayer!!.prepareAsync()
    }

    /** 根据歌曲信息更改当前播放歌曲 */
    fun changeCurrentSong(songInfo: SongInfo) {
        var targetSongIndex = currentPlayList!!.indexOfFirst { songInfo.uri == it.uri }
        if(targetSongIndex == -1) {
            targetSongIndex = currentPlayList!!.count()
            currentPlayList!!.add(songInfo)
        }
        changeCurrentSong(targetSongIndex)
    }

    /** 播放上一首歌曲 */
    fun changeToPrevious() {
        val targetSongIndex: Int
        if(currentSongIndex!! <= 0) {
            targetSongIndex = currentPlayList!!.count() - 1
        } else {
            targetSongIndex = currentSongIndex!! - 1
        }
        changeCurrentSong(targetSongIndex)
    }

    /** 播放上一首歌曲 */
    fun changeToNext() {
        val targetSongIndex: Int
        if(currentSongIndex!! >= currentPlayList!!.size - 1 ) {
            targetSongIndex = 0
        } else {
            targetSongIndex = currentSongIndex!! + 1
        }
        changeCurrentSong(targetSongIndex)
    }
}

/** 底部播放控制 */
@Composable
fun BottomPlayer(
    currentSongInfo: SongInfo,
    isPlaying: Boolean,
    onPlayButtonClick: () -> Unit,
    onChangeToPrevious: () -> Unit,
    onChangeToNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row (
        modifier = modifier
            .height(65.dp)
            .background(Purple40)
            .height(64.dp)
            .background(Pink80),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            Text(
                text = currentSongInfo.title,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            Text(
                text = currentSongInfo.album,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
        }
        IconButton(
            onClick = onChangeToPrevious,
            modifier = Modifier.weight(0.1f)
        ) {
            Icon(painterResource(R.drawable.player_previous), "previous")
        }
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
fun SongPlayer(songInfo: SongInfo, modifier: Modifier = Modifier) {
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
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painterResource(R.drawable.player_backarrow),
                    "back",
                    tint = Color.White
                )
            }
            Column {
                Text(songInfo.title)
                Text(songInfo.artist)
            }
            IconButton(onClick = { /*TODO*/ }, Modifier.align(Alignment.Bottom)) {
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
        Slider(
            value = 0f,
            onValueChange = { /*TODO*/ },
            modifier = Modifier.padding(horizontal = 32.dp),
            colors = SliderDefaults.colors()
        )

        // 底端操作栏
        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painterResource(R.drawable.player_loop),
                    "loop"
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painterResource(R.drawable.player_previous),
                    "previous"
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painterResource(R.drawable.player_playarrow),
                    "play"
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painterResource(R.drawable.player_next),
                    "next"
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painterResource(R.drawable.player_list),
                    "list"
                )
            }
        }
    }
}

/** 显示当前播放列表 */
@Composable
fun PlayList(modifier: Modifier = Modifier) {}

@Preview(showBackground = true, backgroundColor = 0xFF4B4D5A)
@Composable
fun SongPlayerPreview() {
    SongPlayer(temSongInfo)
}
