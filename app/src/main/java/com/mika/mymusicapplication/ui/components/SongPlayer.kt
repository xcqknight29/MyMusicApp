package com.mika.mymusicapplication.ui.components

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.util.StorageHandler
import com.mika.mymusicapplication.viewModel.MyViewModel

/** 播放顺序模式 */
enum class PlayMode {
    SEQUENTIAL, // 列表顺序播放
    SHUFFLE,    // 列表随机播放
    LOOP        // 单曲循环
}

/** 播放控制模块 */
class SongPlayer(
    var currentSongIndex: Int,
    var currentPlayList: List<SongInfo>,
    var playMode: PlayMode = PlayMode.SEQUENTIAL,
    var albumMap: Map<String, List<SongInfo>>,
    var artistMap: Map<String, List<SongInfo>>,
    var playlistMap: Map<String, List<SongInfo>>,
    var mediaPlayer: MediaPlayer?,
    var viewModel: MyViewModel,
    val storageHandler: StorageHandler,
) {
    init {
        viewModel.setIsPlaying(mediaPlayer!!.isPlaying)
        viewModel.setPlayMode(playMode)
        viewModel.setCurrentPlayIndex(currentSongIndex)
        viewModel.setCurrentPlayList(currentPlayList)
        viewModel.setAlbumMap(albumMap)
        viewModel.setArtistMap(artistMap)
        viewModel.setPlaylistMap(playlistMap)
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
                        changeCurrentSong(currentSongIndex + 1)
                    } else {
                        changeCurrentSong(0)
                    }
                }
                // 列表随机
                PlayMode.SHUFFLE -> {
                    var targetSongIndex: Int
                    do {
                        targetSongIndex = (0 until currentPlayList.count()).random()
                    } while (targetSongIndex == currentSongIndex)
                    changeCurrentSong(targetSongIndex)
                }
                // 单曲循环
                else -> {
                    mediaPlayer!!.start()
                    viewModel.setIsPlaying(true)
                }
            }
        }
    }

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
        this.playMode = playMode
        viewModel.setPlayMode(playMode)
    }

    /**
     * 切换到下一个播放模式
     * 顺序: 列表循环 -> 列表随机 -> 单曲循环 -> 列表循环
     * */
    fun changePlayMode() {
        when(viewModel.playMode.value) {
            PlayMode.SEQUENTIAL -> {
                changePlayMode(PlayMode.SHUFFLE)
            }
            PlayMode.SHUFFLE -> {
                changePlayMode(PlayMode.LOOP)
            }
            else -> {
                changePlayMode(PlayMode.SEQUENTIAL)
            }
        }
    }

    /** 根据索引更改当前播放歌曲 */
    fun changeCurrentSong(songIndex: Int) {
        currentSongIndex = songIndex
        viewModel.setCurrentPlayIndex(songIndex)
        mediaPlayer!!.reset()
        mediaPlayer!!.setDataSource(currentPlayList[songIndex].uri)
        mediaPlayer!!.prepareAsync()
    }

    /** 根据歌曲信息更改当前播放歌曲 */
    fun changeCurrentSong(songInfo: SongInfo) {
        var targetSongIndex = currentPlayList.indexOfFirst { songInfo.uri == it.uri }
        if(targetSongIndex == -1) {
            targetSongIndex = currentPlayList.count()
            currentPlayList += songInfo
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

    /** 添加歌曲到当前播单 */
    fun addToCurrentPlaylist(songList: List<SongInfo>) {
        currentPlayList += songList
        viewModel.setCurrentPlayList(currentPlayList)
    }

    /** 替换当前歌单 */
    fun replaceCurrentPlaylist(songList: List<SongInfo>) {
        currentPlayList = songList
        viewModel.setCurrentPlayList(currentPlayList)
    }

    /** 将歌曲添加到指定名字的播单 */
    fun addToPlaylist(songList: List<SongInfo>, targetPlaylistName: String) {
        val targetPlaylistMap = playlistMap.toMutableMap()
        val targetPlaylist = playlistMap[targetPlaylistName]!!.toMutableList().plus(songList)
        targetPlaylistMap[targetPlaylistName] = targetPlaylist
        playlistMap = targetPlaylistMap
        viewModel.setPlaylistMap(playlistMap)
        addSongToPlaylist(targetPlaylistName, songList)
    }

    /** 向播单中添加一首歌曲 */
    fun addSongToPlaylist(targetPlaylist: String, songInfo: SongInfo) {
        storageHandler.addSongToPlaylistStorage(targetPlaylist, songInfo, null)
    }

    /** 向播单中添加多首歌曲 */
    fun addSongToPlaylist(targetPlaylist: String, songsInfo: List<SongInfo>) {
        storageHandler.addSongToPlaylistStorage(targetPlaylist, null, songsInfo)
    }

    /** 重新获取播单 */
    fun reloadPlaylist() {
        val playlistMap = storageHandler.getPlaylistFromStorage()
        this.playlistMap = playlistMap
        viewModel.setPlaylistMap(playlistMap)
    }

    /** 添加一个新播单 */
    fun addNewPlaylist(playlistName: String) {
        storageHandler.addPlaylist(playlistName)
        reloadPlaylist()
    }

    /** 根据名称删除播单 */
    fun deletePlaylist(playlistName: String) {
        storageHandler.deletePlaylist(playlistName)
        reloadPlaylist()
    }

    /** 以Float形式返回当前播放进度 */
    fun getPosition(): Float {
        // 计算当前进度
        val position =
            mediaPlayer!!.currentPosition.toFloat() / mediaPlayer!!.duration.toFloat()
        // 当计算值不为NaN时返回计算出的当前位置, 否则返回0
        return if (!position.isNaN()) position else 0f
    }

}

/** 显示当前播放列表 */
//@Composable
//fun CurrentPlayList(playList: MutableList<SongInfo>, modifier: Modifier = Modifier) {
//    LazyColumn(modifier.padding(start = 8.dp)) {
//        items(playList) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                // 是否显示菜单
//                var showMenu by remember { mutableStateOf(false) }
//                // 歌曲信息
//                Text(it.title, Modifier.weight(0.6f))
//                Text(it.artist, Modifier.weight(0.3f))
//                // 按钮及其呼出的下拉菜单
//                Column(Modifier.weight(0.1f)) {
//                    IconButton(onClick = { showMenu = !showMenu }) {
//                        Icon(painter = painterResource(id = R.drawable.more_buttom), contentDescription = "more action")
//                    }
//                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
//                        DropdownMenuItem(text = { Text(stringResource(R.string.song_details)) }, onClick = { /*TODO*/ })
//                        DropdownMenuItem(text = { Text(stringResource(R.string.remove)) }, onClick = { /*TODO*/ })
//                    }
//                }
//            }
//        }
//    }
//}

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
//    val currentPlayList = MutableList(20) { songInfo ->
//        SongInfo(
//            "song - $songInfo",
//            "album - 1",
//            "artist - 1",
//            "uri - 1",
//            0,
//            0
//            )
//    }
//    CurrentPlayList(currentPlayList)
}
