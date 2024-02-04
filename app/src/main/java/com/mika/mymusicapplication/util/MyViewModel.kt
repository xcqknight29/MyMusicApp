package com.mika.mymusicapplication.util

import androidx.lifecycle.ViewModel
import com.mika.mymusicapplication.model.AlbumInfo
import com.mika.mymusicapplication.model.ArtistInfo
import com.mika.mymusicapplication.model.Playlist
import com.mika.mymusicapplication.model.SongInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MyViewModel: ViewModel() {

    private var _isPlaying = MutableStateFlow(false) // 当前播放器是否在播放
    val isPlaying: StateFlow<Boolean> get() = _isPlaying


    private var _playMode = MutableStateFlow(PlayMode.SEQUENTIAL) // 当前播放模式
    val playMode: StateFlow<PlayMode> get() = _playMode


    private var _currentPlayIndex = MutableStateFlow(0) // 当前播放的歌曲的索引
    val currentPlayIndex: StateFlow<Int> get() = _currentPlayIndex


    private var _currentPlayList = MutableStateFlow(listOf<SongInfo>()) // 当前播放的播单
    val currentPlayList: StateFlow<List<SongInfo>> get() = _currentPlayList


    private var _albumList = MutableStateFlow(listOf<AlbumInfo>()) // 专辑列表
    val albumList: StateFlow<List<AlbumInfo>> get() = _albumList


    private var _artistList = MutableStateFlow(listOf<ArtistInfo>()) // 艺术家列表
    val artistList: StateFlow<List<ArtistInfo>> get() = _artistList


    private var _playlistList = MutableStateFlow(listOf<Playlist>()) // 播单列表
    val playlistList: StateFlow<List<Playlist>> get() = _playlistList

    fun setIsPlaying(isPlaying: Boolean) {
        _isPlaying.update { isPlaying }
    }

    fun setPlayMode(playMode: PlayMode) {
        _playMode.update { playMode }
    }

    fun setCurrentPlayIndex(currentPlayIndex: Int) {
        _currentPlayIndex.update { currentPlayIndex }
    }

    fun setCurrentPlayList(currentPlayList: List<SongInfo>) {
        _currentPlayList.update { currentPlayList }
    }

    fun setAlbumList(albumList: List<AlbumInfo>) {
        _albumList.update { albumList }
    }

    fun setArtistList(artistList: List<ArtistInfo>) {
        _artistList.update { artistList }
    }

    fun setPlaylistList(playlistList: List<Playlist>) {
        _playlistList.update { playlistList }
    }

}