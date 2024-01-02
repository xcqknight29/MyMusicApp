package com.mika.mymusicapplication

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mika.mymusicapplication.ui.components.BottomPlayer
import com.mika.mymusicapplication.ui.components.PlayMode
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.ui.components.AlbumList
import com.mika.mymusicapplication.ui.components.SongPlayer
import com.mika.mymusicapplication.ui.components.PlayingList
import com.mika.mymusicapplication.ui.components.SongPage
import com.mika.mymusicapplication.ui.theme.MyMusicApplicationTheme
import com.mika.mymusicapplication.ui.theme.Purple40
import com.mika.mymusicapplication.ui.theme.White
import com.mika.mymusicapplication.viewModel.MyViewModel

var songPlayer: SongPlayer? = null // 播放控制
var alertDialogBuilder: AlertDialog.Builder? = null // 对话框构造器

class MainActivity : ComponentActivity() {
    // 生命周期函数: 创建
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(READ_MEDIA_AUDIO),
                1,
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(READ_EXTERNAL_STORAGE),
                1,
            )
        }

        // 创建弹窗构造器
        alertDialogBuilder = AlertDialog.Builder(this)

        // 从MediaStore中获取歌曲列表
        val currentPlayList = getSongsByMediaStore()

        // 从MediaStore中获取专辑列表
        val albumList = getAlbumByMediaStore()

        // 创建视图模型，用于实现UI与数据绑定
        val viewModel: MyViewModel by viewModels()

        // 创建播放器对象
        songPlayer = SongPlayer(0, currentPlayList, albumList, MediaPlayer(), viewModel, PlayMode.SEQUENTIAL)

        // 创建界面
        setContent { MainContent() }
    }

    // 生命周期函数: 销毁
    override fun onDestroy() {
        super.onDestroy()
        songPlayer?.mediaPlayer?.release()
        songPlayer?.mediaPlayer = null
        songPlayer = null
        alertDialogBuilder = null
    }

    /** 从MediaStore中获取专辑 */
    fun getAlbumByMediaStore(): MutableList<MutableList<SongInfo>> {
        val albumList = mutableSetOf<String>()
        val albumSongList = mutableListOf<MutableList<SongInfo>>()
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Media.ALBUM
        )?.use {cursor ->
        if (cursor.moveToFirst()) {
                do {
                    if (albumList.find { cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) == it } == null) {
                        albumList.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)))
                        albumSongList.add(mutableListOf())
                    }
                    albumSongList[albumList.size - 1].add(
                        SongInfo(
                            title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                            album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                            artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                            uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                            duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                            size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        return albumSongList
    }

    /** 从MediaStore中重新获取歌曲信息 */
    fun getSongsByMediaStore(): MutableList<SongInfo> {
        val playList = mutableListOf<SongInfo>()
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Media.TITLE
        )?.use { cursor ->
            if(cursor.moveToFirst()) {
                do {
                    playList.add(
                        SongInfo(
                            title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                            album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                            artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                            uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                            duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                            size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
                        )
                    )
                } while(cursor.moveToNext())
            }
        }
        return playList
    }
}

/** App主界面 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainContent(modifier: Modifier = Modifier) {
    val mediaPlayer: MediaPlayer? = songPlayer!!.mediaPlayer
    val viewModel: MyViewModel = songPlayer!!.viewModel
    val navController: NavHostController = rememberNavController()
    val currentPlayList = viewModel.currentPlayList.observeAsState().value!!
    val currentPlayIndex = viewModel.currentPlayIndex.observeAsState().value!!

    var showSongPlayer: Boolean by remember { mutableStateOf(false) }

    val showSongPLayer: () -> Unit = { showSongPlayer = true }
    val hiddenSongPlayer: () -> Unit = { showSongPlayer = false }
    val getPosition: () -> Float = ::getPosition
    val onSliderChange: (Float) -> Unit = { mediaPlayer!!.seekTo((mediaPlayer.duration.toFloat() * it).toInt()) }

    MyMusicApplicationTheme {
        val isPlaying = viewModel.isPlaying.observeAsState()

        if (showSongPlayer) {
            SongPage(
                currentPlayList[currentPlayIndex],
                isPlaying.value!!,
                hiddenSongPlayer,
                getPosition,
                onSliderChange,
                songPlayer!!::changePlayState,
                songPlayer!!::changeToPrevious,
                songPlayer!!::changeToNext,
            )
        } else {
            Scaffold (
                modifier = modifier,
                topBar = {
                    Column {
                        TopBar()
                        TopTab(navController)
                    }
                },
                bottomBar = {
                    BottomPlayer(
                        songPlayer!!.currentPlayList[currentPlayIndex],
                        isPlaying.value!!,
                        showSongPLayer,
                        songPlayer!!::changePlayState,
                        songPlayer!!::changeToPrevious,
                        songPlayer!!::changeToNext,
                        Modifier.height(65.dp)
                    )
                }
            ) {
                MyNavHost(
                    startDestination = "Songs",
                    onClickPlayMode = songPlayer!!::changePlayMode,
                    onCurrentSongChange = songPlayer!!::changeCurrentSong,
                    modifier = Modifier.padding(8.dp, 0.dp),
                    navController = navController,
                )
            }
        }
    }
}

/** 以Float形式返回当前播放进度 */
fun getPosition(): Float {
//    Log.i("", "test log")
    val position = songPlayer!!.mediaPlayer!!.currentPosition.toFloat() / songPlayer!!.mediaPlayer!!.duration.toFloat()
    return if (!position.isNaN()) position else 0f
}

/** NavHost 用于显示切换正文内容组件? */
@Composable
fun MyNavHost(
    startDestination: String,
    onClickPlayMode: () -> Unit,
    onCurrentSongChange: (SongInfo) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("Songs") {
            PlayingList(
                songPlayer!!.currentPlayList,
                onClickPlayMode,
                onCurrentSongChange
            )
        }
        composable("Albums") {
            val albumList = songPlayer!!.viewModel.albumList.observeAsState()
            AlbumList(albumList.value!!)
        }
        composable("Artists") { Text("test3") }
        composable("Playlists") { Text("test4") }
        composable("Folder") { Text("test5") }
    }
}

/** 顶栏 有1+3个按钮 用于呼出侧边菜单 或者通向不同的界面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = {},
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = { /* Handle menu button click */ }) {
                Icon(painterResource(R.drawable.topbar_menu), "menu")
            }
        },
        actions = {
            IconButton(onClick = { /* Handle menu button click */ }) {
                Icon(painterResource(R.drawable.topbar_sort), "sort")
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { /* Handle menu button click */ }) {
                Icon(painterResource(R.drawable.topbar_timer), "timer")
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { /* Handle menu button click */ }) {
                Icon(painterResource(R.drawable.topbar_search), "timer")
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Purple40,
            navigationIconContentColor = White,
            actionIconContentColor = White,
        )
    )
}

/** 顶端tab横向菜单，有5个菜单项 */
@Composable
fun TopTab(navController: NavHostController, modifier: Modifier = Modifier) {
    var selectedTabIndex: Int by remember { mutableIntStateOf(0) }
    // Tab菜单组件的菜单项
    val titles = listOf<Int>(
        R.string.main_tabitem_songs,
        R.string.main_tabitem_albums,
        R.string.main_tabitem_artists,
        R.string.main_tabitem_playlist,
        R.string.main_tabitem_folder
    )
    TabRow(selectedTabIndex, modifier) {
        titles.forEachIndexed { index, it ->
            val title = stringResource(it)
            Tab(selected = false,
                onClick = {
                    navController.navigate(title)
                    selectedTabIndex = index
                },
                text = { Text(text = stringResource(it), maxLines = 1) })
        }
    }
}

/**
 * 预览
 * */
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MainContent()
}