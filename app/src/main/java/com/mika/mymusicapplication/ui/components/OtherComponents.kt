package com.mika.mymusicapplication.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.ui.theme.Purple40
import com.mika.mymusicapplication.ui.theme.White

/* 这个文件用来放一些不好分类的小组件 */

/** 顶栏 有1+3个按钮，用于呼出侧边菜单，或者通向不同的界面？ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier) {

    TopAppBar(
        title = {},
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(painterResource(R.drawable.topbar_menu), "menu")
            }
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(painterResource(R.drawable.topbar_sort), "sort")
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { /* TODO */ }) {
                Icon(painterResource(R.drawable.topbar_timer), "timer")
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { /* TODO */ }) {
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