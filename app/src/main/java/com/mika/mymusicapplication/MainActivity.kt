package com.mika.mymusicapplication

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mika.mymusicapplication.ui.components.BottomPlayer
import com.mika.mymusicapplication.util.PlayMode
import com.mika.mymusicapplication.model.SongInfo
import com.mika.mymusicapplication.ui.components.AlbumList
import com.mika.mymusicapplication.ui.components.ArtistList
import com.mika.mymusicapplication.ui.components.Folders
import com.mika.mymusicapplication.util.SongPlayer
import com.mika.mymusicapplication.ui.components.CurrentPlayingList
import com.mika.mymusicapplication.ui.components.PlaylistSelections
import com.mika.mymusicapplication.ui.components.Playlists
import com.mika.mymusicapplication.ui.components.SongList
import com.mika.mymusicapplication.ui.components.SongPage
import com.mika.mymusicapplication.ui.components.SongsFromPlaylist
import com.mika.mymusicapplication.ui.components.TopBar
import com.mika.mymusicapplication.ui.components.TopTab
import com.mika.mymusicapplication.ui.theme.MyMusicApplicationTheme
import com.mika.mymusicapplication.util.StorageHandler
import com.mika.mymusicapplication.util.MyViewModel

var songPlayer: SongPlayer? = null // 播放控制
lateinit var alertDialogBuilder: AlertDialog.Builder // 对话框构造器

class MainActivity : ComponentActivity() {

    // 生命周期函数: 创建
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_MEDIA_AUDIO),
                1,
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_EXTERNAL_STORAGE),
                1,
            )
        }

        alertDialogBuilder = AlertDialog.Builder(this) // 创建弹窗构造器

        val storageHandler = StorageHandler(this, contentResolver) // 创建存储操作对象

        val currentPlayList = storageHandler.loadCurrentPlaylist() // 从MediaStore中获取歌曲列表

        val currentSongIndex = storageHandler.loadCurrentIndex() // 读取当前播放歌曲的索引

        val playMode: PlayMode = storageHandler.loadPlayMode() // 设置播放模式为顺序播放

        val albumList = storageHandler.getAlbumFromMediaStore()// 从MediaStore中获取专辑列表

        val artistList = storageHandler.getArtistFromMediaStore()// 从MediaStore中获取艺术家列表

        val playlists = storageHandler.getPlaylistFromStorage() // 获取歌单

        val mediaPlayer = MediaPlayer() // 创建媒体播放器

        val viewModel: MyViewModel by viewModels() // 创建视图模型，用于实现UI与数据绑定

        songPlayer = SongPlayer( // 创建播放器对象
            currentSongIndex,
            currentPlayList,
            playMode,
            albumList,
            artistList,
            playlists,
            mediaPlayer,
            viewModel,
            storageHandler,
        )

        setContent { MainContent() } // 创建界面

    }

    // 生命周期函数: 销毁
    override fun onDestroy() {

        super.onDestroy()

        songPlayer?.mediaPlayer?.release()
        songPlayer?.mediaPlayer = null
        songPlayer = null

    }

}

/** App主界面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(modifier: Modifier = Modifier) {

    val songPlayerNavController: NavHostController = rememberNavController()// 用于切换是否显示歌曲播放界面的navController
    val mainNavController: NavHostController = rememberNavController()// 用于切换主界面显示的navController

    MyMusicApplicationTheme {

        NavHost(// NavHost用于接受导航消息并切换显示正文内容
            navController = songPlayerNavController,
            startDestination = "MainSpace",
            modifier = modifier
        ) {

            composable("MainSpace") {// 主界面
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
                    MyNavHost(mainNavController, Modifier.padding(it))
                }
            }

            composable("SongPlayer") {// 播放页面
                SongPage(
                    onBackButtonClick = { songPlayerNavController.popBackStack() },
                )
            }

        }

    }

}

/** 一个NavHost，用于显示可切换主界面 */
@Composable
fun MyNavHost(navController: NavHostController, modifier: Modifier = Modifier) {

    var songsToShow by remember { mutableStateOf(listOf<SongInfo>()) }// 显示在界面上的歌曲
    var songsToAdd by remember { mutableStateOf(listOf<SongInfo>()) }// 将要添加的歌曲
    var targetPlaylist by remember { mutableStateOf("") }// 添加目标播单

    val showSongList: (List<SongInfo>) -> Unit = {// 显示专辑中的歌曲
        songsToShow = it
        navController.navigate("OtherSongs")
    }

    val selectSongListToAdd: (List<SongInfo>) -> Unit = {// 将选中歌曲信息赋值到songList中并导航到播单选择界面
        songsToAdd = it
        navController.navigate("PlaylistSelections")
    }

    val selectPlaylist: (String) -> Unit = {// 选中播单作为添加目标
        targetPlaylist = it
        songPlayer!!.addToPlaylist(songsToAdd, targetPlaylist)
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

/** 预览 */
@Preview(showBackground = true)
@Composable
fun MainPreview() {

}