package com.mika.mymusicapplication.viewModel

import androidx.lifecycle.ViewModel
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.ui.components.PlayMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MyViewModel: ViewModel() {
    // 当前播放器是否在播放
    private var _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying
    // 当前播放模式
    private var _playMode = MutableStateFlow(PlayMode.SEQUENTIAL)
    val playMode: StateFlow<PlayMode> get() = _playMode
    // 当前播放的歌曲的索引
    private var _currentPlayIndex = MutableStateFlow(0)
    val currentPlayIndex: StateFlow<Int> get() = _currentPlayIndex
    // 当前播放的播单
    private var _currentPlayList = MutableStateFlow(listOf<SongInfo>())
    val currentPlayList: StateFlow<List<SongInfo>> get() = _currentPlayList
    // 专辑列表
    private var _albumMap = MutableStateFlow(mapOf<String, List<SongInfo>>())
    val albumMap: StateFlow<Map<String, List<SongInfo>>> get() = _albumMap
    // 艺术家列表
    private var _artistMap = MutableStateFlow(mapOf<String, List<SongInfo>>())
    val artistMap: StateFlow<Map<String, List<SongInfo>>> get() = _artistMap
    // 播单列表
    private var _playlistMap = MutableStateFlow(mapOf<String, List<SongInfo>>())
    val playlistMap: StateFlow<Map<String, List<SongInfo>>> get() = _playlistMap

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
    fun setAlbumMap(albumList: Map<String, List<SongInfo>>) {
        _albumMap.update { albumList }
    }
    fun setArtistMap(artistList: Map<String, List<SongInfo>>) {
        _artistMap.update { artistList }
    }
    fun setPlaylistMap(playlists: Map<String, List<SongInfo>>) {
        _playlistMap.update { playlists }
    }
}