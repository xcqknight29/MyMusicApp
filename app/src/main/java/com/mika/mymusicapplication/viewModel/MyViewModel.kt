package com.mika.mymusicapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.ui.components.PlayMode

class MyViewModel: ViewModel() {
    private var _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private var _playMode = MutableLiveData(PlayMode.SEQUENTIAL)
    val playMode: LiveData<PlayMode> get() = _playMode

    private var _currentPlayIndex = MutableLiveData(0)
    val currentPlayIndex: LiveData<Int> get() = _currentPlayIndex

    private var _currentPlayList = MutableLiveData(mutableListOf<SongInfo>())
    val currentPlayList: LiveData<MutableList<SongInfo>> get() = _currentPlayList

    private var _albumList = MutableLiveData(mutableListOf<MutableList<SongInfo>>())
    val albumList: LiveData<MutableList<MutableList<SongInfo>>> get() = _albumList

    fun setIsPlaying(isPlaying: Boolean) {
        _isPlaying.postValue(isPlaying)
    }
    fun setPlayMode(playMode: PlayMode) {
        _playMode.postValue(playMode)
    }
    fun setCurrentPlayIndex(currentPlayIndex: Int) {
        _currentPlayIndex.postValue(currentPlayIndex)
    }
    fun setCurrentPlayList(currentPlayList: MutableList<SongInfo>) {
        _currentPlayList.postValue(currentPlayList)
    }
    fun setAlbumList(albumList: MutableList<MutableList<SongInfo>>) {
        _albumList.postValue(albumList)
    }
}