package com.mika.mymusicapplication

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.mika.mymusicapplication.ui.components.ArtistList
import com.mika.mymusicapplication.ui.components.Folders
import com.mika.mymusicapplication.ui.components.SongPlayer
import com.mika.mymusicapplication.ui.components.CurrentPlayingList
import com.mika.mymusicapplication.ui.components.PlaylistSelections
import com.mika.mymusicapplication.ui.components.Playlists
import com.mika.mymusicapplication.ui.components.SongList
import com.mika.mymusicapplication.ui.components.SongPage
import com.mika.mymusicapplication.ui.components.SongsFromPlaylist
import com.mika.mymusicapplication.ui.theme.MyMusicApplicationTheme
import com.mika.mymusicapplication.ui.theme.Purple40
import com.mika.mymusicapplication.ui.theme.White
import com.mika.mymusicapplication.util.StorageHandler
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

        // 创建存储操作对象
        val storageHandler = StorageHandler(this)

        // 从MediaStore中获取歌曲列表
        val currentPlayList = storageHandler.getSongsByMediaStore(contentResolver)

        // 从MediaStore中获取专辑列表
        val albumList = storageHandler.getAlbumByMediaStore(contentResolver)

        // 从MediaStore中获取艺术家列表
        val artistList = storageHandler.getArtistByMediaStore(contentResolver)

        // 获取歌单
        val playlists = storageHandler.getPlaylistFromStorage()

        // 创建媒体播放器
        val mediaPlayer = MediaPlayer()

        // 创建视图模型，用于实现UI与数据绑定
        val viewModel: MyViewModel by viewModels()

        // 设置播放模式为顺序播放
        val playMode: PlayMode = PlayMode.SEQUENTIAL

        // 创建播放器对象
        songPlayer = SongPlayer(
            0,
            currentPlayList,
            playMode,
            albumList,
            artistList,
            playlists,
            mediaPlayer,
            viewModel,
            storageHandler,
        )

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

}

/** App主界面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(modifier: Modifier = Modifier) {

    // 用于切换是否显示歌曲播放界面的navController
    val songPlayerNavController: NavHostController = rememberNavController()
    // 用于切换主界面显示的navController
    val mainNavController: NavHostController = rememberNavController()

    MyMusicApplicationTheme {

        // NavHost用于接受导航消息并切换显示正文内容
        NavHost(
            navController = songPlayerNavController,
            startDestination = "MainSpace",
            modifier = modifier
        ) {

            // 主界面
            composable("MainSpace") {
                Scaffold (
                    topBar = { Column {
                        TopBar()
                        TopTab(mainNavController)
                    } },
                    bottomBar = { BottomPlayer(
                        onFooterClick = { songPlayerNavController.navigate("SongPlayer") },
                        modifier = Modifier.height(65.dp)
                    ) }
                ) {
                    SongsManage(mainNavController, Modifier.padding(it))
                }
            }

            // 播放页面
            composable("SongPlayer") {
                SongPage(
                    onBackButtonClick = { songPlayerNavController.popBackStack() },
                )
            }

        }

    }

}

/** 一个NavHost，用于显示可切换主界面 */
@Composable
fun SongsManage(navController: NavHostController, modifier: Modifier = Modifier) {

    // 显示在界面上的歌曲
    var songsToShow by remember { mutableStateOf(listOf<SongInfo>()) }
    // 将要添加的歌曲
    var songsToAdd by remember { mutableStateOf(listOf<SongInfo>()) }
    // 添加目标播单
    var targetPlaylist by remember { mutableStateOf("") }

    // 显示专辑中的歌曲
    val showSongList: (List<SongInfo>) -> Unit = {
        songsToShow = it
        navController.navigate("OtherSongs")
    }

    // 将选中歌曲信息赋值到songList中并导航到播单选择界面
    val selectSongListToAdd: (List<SongInfo>) -> Unit = {
        songsToAdd = it
        navController.navigate("PlaylistSelections")
    }

    // 选中播单作为添加目标
    val selectPlaylist: (String) -> Unit = {
        targetPlaylist = it
        songPlayer!!.addSongToPlaylist(targetPlaylist, songsToAdd)
        navController.popBackStack()
        songPlayer!!.reloadPlaylist()
    }

    NavHost(
        navController = navController,
        startDestination = "CurrentPlaylist",
        modifier = modifier
    ) {
        composable("CurrentPlaylist") {
            CurrentPlayingList()
        }
        composable("Albums") {
            AlbumList(onItemClick = showSongList, addToPlaylist = selectSongListToAdd)
        }
        composable("Artists") {
            ArtistList(onItemClick = showSongList, addToPlaylist = selectSongListToAdd)
        }
        composable("Playlists") {
            Playlists(onItemClick = showSongList, addToOtherHandler = selectSongListToAdd)
        }
        composable("Folders") {
            Folders()
        }
        composable("OtherSongs") {
            SongList(songsData = songsToShow)
        }
        composable("PlaylistSongs") {
            SongsFromPlaylist(songsData = songsToShow)
        }
        composable("PlaylistSelections") {
            PlaylistSelections(dataCount = songsToAdd.size, confirmPlaylistToAdd = selectPlaylist)
        }
    }

}

/** 顶栏 有1+3个按钮，用于呼出侧边菜单，或者通向不同的界面？ */
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

    var index = 0
    var selectedTabIndex: Int by remember { mutableIntStateOf(0) }
    // Tab菜单组件的菜单项数据
    val titles = mapOf<Int, String>(
        R.string.main_tabitem_songs to "CurrentPlaylist",
        R.string.main_tabitem_albums to "Albums",
        R.string.main_tabitem_artists to "Artists",
        R.string.main_tabitem_playlist to "Playlists",
        R.string.main_tabitem_folder to "Folders"
    )

    TabRow(selectedTabIndex, modifier) {
        titles.forEach { (str, route) ->
            val itemIndex = index
            Tab(
                selected = false,
                onClick = {
                    navController.navigate(route)
                    selectedTabIndex = itemIndex
                },
                text = { Text(stringResource(str), maxLines = 1) }
            )
            index += 1
        }
    }

}

/** 预览 */
@Preview(showBackground = true)
@Composable
fun MainPreview() {

}