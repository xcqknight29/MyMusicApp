package com.mika.mymusicapplication.util

import android.media.MediaPlayer
import com.mika.mymusicapplication.alertDialogBuilder
import com.mika.mymusicapplication.model.AlbumInfo
import com.mika.mymusicapplication.model.ArtistInfo
import com.mika.mymusicapplication.model.Playlist
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.songPlayer

/** 播放顺序模式 */
enum class PlayMode {
    SEQUENTIAL, // 列表顺序播放
    SHUFFLE,    // 列表随机播放
    LOOP        // 单曲循环
}

/** 播放控制模块 */
class SongPlayer(
    private var currentSongIndex: Int,
    private var currentPlayList: List<SongInfo>,
    private var playMode: PlayMode = PlayMode.SEQUENTIAL,
    private var albumList: List<AlbumInfo>,
    private var artistList: List<ArtistInfo>,
    private var playlistList: List<Playlist>,
    var mediaPlayer: MediaPlayer?,
    val viewModel: MyViewModel,
    private val storageHandler: StorageHandler,
) {
    init {

        viewModel.setIsPlaying(mediaPlayer!!.isPlaying)
        viewModel.setPlayMode(playMode)

        when {
            currentPlayList.isEmpty() -> { /* do nothing */ }
            currentSongIndex == -1 -> {
                currentSongIndex = 0
            }
            currentSongIndex >= currentPlayList.size -> {
                currentSongIndex = currentPlayList.size - 1
            }
        }

        viewModel.setCurrentPlayIndex(currentSongIndex)
        viewModel.setCurrentPlayList(currentPlayList)
        viewModel.setAlbumList(albumList)
        viewModel.setArtistList(artistList)
        viewModel.setPlaylistList(playlistList)

        if (currentPlayList.isNotEmpty()) {
            mediaPlayer!!.setDataSource(currentPlayList[currentSongIndex].uri)
            mediaPlayer!!.prepare()
        }

        mediaPlayer!!.setOnPreparedListener {
            viewModel.setIsPlaying(false)
            changeCurrentIndex(currentSongIndex)
        }

        // 设置完播侦听器
        mediaPlayer!!.setOnCompletionListener {
            when (playMode) {
                // 列表播放
                PlayMode.SEQUENTIAL -> {
                    mediaPlayer!!.reset()
                    changeToNext()
                }
                // 列表随机
                PlayMode.SHUFFLE -> {
                    var targetSongIndex: Int
                    do {
                        targetSongIndex = (currentPlayList.indices).random()
                    } while (targetSongIndex == currentSongIndex)
                    changeCurrentSong(targetSongIndex)
                }
                // 单曲循环
                else -> {
                    mediaPlayer!!.start()
                }
            }
        }

    }

    /** 跳转到目标位置 */
    fun seekToTargetPosition(targetPosition: Float) {
        mediaPlayer!!.seekTo((mediaPlayer!!.duration.toFloat() * targetPosition).toInt())
    }

    /** 切换播放状态 */
    fun changePlayState() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            viewModel.setIsPlaying(false)
            mediaPlayer!!.setOnPreparedListener {
                viewModel.setIsPlaying(false)
                changeCurrentIndex(currentSongIndex)
            }
        } else {
            mediaPlayer!!.start()
            viewModel.setIsPlaying(true)
            mediaPlayer!!.setOnPreparedListener {
                it.start()
                viewModel.setIsPlaying(true)
                changeCurrentIndex(currentSongIndex)
            }
        }
    }

    /** 切换到指定播放模式 */
    fun changePlayMode(playMode: PlayMode) {
        this.playMode = playMode
        viewModel.setPlayMode(playMode)
        storageHandler.savePlayMode(playMode)
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

    /** 更改当前播放索引 */
    private fun changeCurrentIndex(targetIndex: Int) {
        this.currentSongIndex = targetIndex
        viewModel.setCurrentPlayIndex(targetIndex)
        storageHandler.saveCurrentIndex(targetIndex)
    }

    /** 根据目标索引更改当前播放歌曲, 当目标索引为-1时不改变mediaPlayer的资源 */
    private fun changeCurrentSong(songIndex: Int) {
        changeCurrentIndex(songIndex)
        if (songIndex <= -1) {
            return
        }
        mediaPlayer!!.reset()
        mediaPlayer!!.setDataSource(currentPlayList[songIndex].uri)
        mediaPlayer!!.prepareAsync()
    }

    /** 根据歌曲信息处理当前播放列表, 当歌曲存在时切换到该歌曲, 不存在时则添加歌曲进入列表 */
    fun changeCurrentSong(songInfo: SongInfo) {
        var targetSongIndex = currentPlayList.indexOfFirst { songInfo.uri == it.uri }
        if(targetSongIndex == -1) {
            targetSongIndex = currentPlayList.size
            addToCurrentPlaylist(songInfo)
        }
        changeCurrentSong(targetSongIndex)
    }

    /** 播放上一首歌曲 */
    fun changeToPrevious() {
        changeCurrentSong(getPreviousIndex())
    }

    /** 获取上一首的索引 */
    private fun getPreviousIndex() : Int {
        return getPreviousIndex(currentSongIndex, currentPlayList)
    }

    /** 获取上一首的索引 */
    private fun getPreviousIndex(currentIndex: Int, list: List<SongInfo>) : Int {
        return if(currentIndex <= 0) {
            list.size - 1
        } else {
            currentIndex - 1
        }
    }

    /** 播放下一首歌曲 */
    fun changeToNext() {
        changeCurrentSong(getNextIndex())
    }

    /** 获取下一首的索引 */
    private fun getNextIndex() : Int {
        return when {
            currentPlayList.isEmpty() -> -1
            currentSongIndex >= currentPlayList.size - 1 -> 0
            else -> currentSongIndex + 1
        }
    }

    /** 读取当前播放歌曲索引 */
    fun loadCurrentIndex() {
        currentSongIndex = storageHandler.loadCurrentIndex() // 修改SongPlayer中的currentSongIndex
        viewModel.setCurrentPlayIndex(currentSongIndex) // 修改ViewModel中的currentSongIndex
    }

    /** 读取当前播单 */
    fun loadCurrentPlaylist() {
        currentPlayList = storageHandler.loadCurrentPlaylist()// 修改SongPlayer中的currentPlaylist
        viewModel.setCurrentPlayList(currentPlayList)// 修改ViewModel中的currentPlayList
    }

    /** 添加歌曲到当前播单 */
    fun addToCurrentPlaylist(song: SongInfo) {
        replaceCurrentPlaylist(currentPlayList.plus(song))
        if (currentSongIndex == -1) {
            changeCurrentSong(0)
        }
    }

    /** 添加歌曲到当前播单 */
    fun addToCurrentPlaylist(songList: List<SongInfo>) {
        replaceCurrentPlaylist(currentPlayList + songList)
        if (currentSongIndex == -1) {
            changeCurrentSong(0)
        }
    }

    /** 从当前歌单中移除歌曲 */
    fun deleteSongFromCurrent(song: SongInfo) {
        val targetPlaylist = currentPlayList.toMutableList()
        val targetSongIndex: Int = targetPlaylist.indexOf(song)
        targetPlaylist.remove(song)
        if (targetSongIndex <= currentSongIndex) {
            changeCurrentIndex(getPreviousIndex(currentSongIndex, targetPlaylist))
            replaceCurrentPlaylist(targetPlaylist)
            changeToNext()
        } else {
            replaceCurrentPlaylist(targetPlaylist)
        }
    }

    /** 清空当前播放列表 */
    fun deleteAllFromCurrent() {
        replaceCurrentPlaylist(listOf())
    }

    /** 替换当前歌单 */
    fun replaceCurrentPlaylist(songList: List<SongInfo>) {
        this.currentPlayList = songList
        viewModel.setCurrentPlayList(currentPlayList)
        storageHandler.saveCurrentPlaylist(currentPlayList)
    }

    /** 将歌曲添加到指定名字的播单 */
    fun addToPlaylist(songList: List<SongInfo>, targetPlaylistName: String) {

        val targetPlaylistList = playlistList.toMutableList()
        val targetPlaylist = targetPlaylistList.find { it.name == targetPlaylistName }

        if (targetPlaylist == null) {
            playlistNotExist(targetPlaylistName)
            return
        }

        targetPlaylist.data = targetPlaylist.data!!.plus(songList)

        this.playlistList = targetPlaylistList
        viewModel.setPlaylistList(this.playlistList)
        replacePlaylist(targetPlaylist) // 写入文件中
        reloadPlaylist()

    }

    /** 弹出对话框提示歌单不存在 */
    fun playlistNotExist(targetPlaylistName: String) {
        val dialog = alertDialogBuilder
        dialog.setMessage("$targetPlaylistName is not exist!")
        dialog.show()
    }

    /** 向播单中添加一首歌曲 */
    fun addSongToPlaylist(targetPlaylist: String, songInfo: SongInfo) {
        storageHandler.addSongToPlaylist(targetPlaylist, songInfo, null)
    }

    /** 向播单中添加多首歌曲 */
    fun addSongToPlaylist(targetPlaylist: String, songsInfo: List<SongInfo>) {
        storageHandler.addSongToPlaylist(targetPlaylist, null, songsInfo)
    }

    /** 用一个新的播单代替旧播单 */
    fun replacePlaylist(playlist: Playlist) {
        storageHandler.replacePlaylist(playlist)
    }

    /** 重新获取专辑 */
    fun reloadAlbum() {
        val albumList = storageHandler.getAlbumFromMediaStore()
        this.albumList = albumList
        viewModel.setAlbumList(albumList)
    }

    /** 重新获取艺术家 */
    fun reloadArtist() {
        val artistList = storageHandler.getArtistFromMediaStore()
        this.artistList = artistList
        viewModel.setArtistList(artistList)
    }

    /** 重新获取播单 */
    fun reloadPlaylist() {
        val playlistList = storageHandler.getPlaylistFromStorage()
        this.playlistList = playlistList
        viewModel.setPlaylistList(playlistList)
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

