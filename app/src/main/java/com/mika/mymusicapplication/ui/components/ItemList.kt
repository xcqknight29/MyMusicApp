package com.mika.mymusicapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.model.SongInfo
import java.util.Objects

/** item列表，可以为Album、Artist和Playlist */
@Composable
fun ItemList(
    listData: Map<String, List<SongInfo>>,
    modifier: Modifier = Modifier,
    onItemClick: ((String) -> Unit)? = null,
    dropdownMenu: @Composable ((Boolean, List<SongInfo>, () -> Unit) -> Unit)? = null
) {

    Column(
        modifier
            .fillMaxHeight()
            .background(Color.White)
    ) {

        // 专辑展示方式: { 0 -> 条状, 1 -> 盒状 }
        var itemDisplay by remember { mutableIntStateOf(0) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (itemDisplay == 0) {
                IconButton(onClick = { itemDisplay = 1 }) {
                    Icon(painterResource(R.drawable.display_square), "square")
                }
            } else {
                IconButton(onClick = { itemDisplay = 0 }) {
                    Icon(painterResource(R.drawable.player_list), "list")
                }
            }
        }

        if (itemDisplay == 0) {
            LazyColumn {
                listData.forEach {
                    item {
                        ListColumnItem(it.key, it.value, onItemClick, Modifier.fillMaxWidth(), dropdownMenu)
                    }
                }
            }
        } else {
            LazyVerticalGrid(GridCells.Adaptive(minSize = 100.dp)) {
                listData.forEach {
                    item {
                        ListGridItem(it.key, it.value, onItemClick, Modifier, dropdownMenu)
                    }
                }
            }
        }

        Spacer(Modifier.height(55.dp))

    }

}

@Composable
fun ListColumnItem(
    title: String,
    dataList: List<SongInfo>,
    onItemClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    dropdownMenu: (@Composable (Boolean, List<SongInfo>, () -> Unit) -> Unit)? = null,
) {

    Row(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clickable { onItemClick?.let { it(title) } },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // 显示菜单
        var showMenu: Boolean by remember { mutableStateOf(false) }

        // 专辑封面
        Image(
            painterResource(R.drawable.song_cover_test),
            "cover",
            Modifier
                .background(Color.Gray)
                .width(40.dp)
                .aspectRatio(1f),
        )

        Column(Modifier.fillMaxWidth(0.8f)) {
            // 专辑标题
            Text(title, maxLines = 2)
        }

        if (dropdownMenu != null) {
            Column {
                // 调出下拉菜单
                IconButton(onClick = { showMenu = true }) {
                    Icon(painterResource(R.drawable.more_buttom), "more detail")
                }
                // 下拉菜单
                dropdownMenu(showMenu, dataList) { showMenu = false }
            }
        }

    }

}

@Composable
fun ListGridItem(
    title: String,
    dataList: List<SongInfo>,
    onItemClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    dropdownMenu: (@Composable (Boolean, List<SongInfo>, () -> Unit) -> Unit)? = null,
) {
    Box(
        modifier
            .padding(2.dp)
            .fillMaxWidth()
            .clickable { onItemClick?.let { it(title) } }
    ) {
        var showMenu: Boolean by remember { mutableStateOf(false) }
        Column(
//            Modifier.combinedClickable(onClick = {}, onLongClick = {})
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { showMenu = !showMenu },
                    onPress = {  }
                )
            }
        ) {
            // 专辑封面
            Image(
                painterResource(R.drawable.song_cover_test),
                "cover",
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxWidth()
                    .aspectRatio(1f) // 固定长宽比
            )
            Column(verticalArrangement = Arrangement.SpaceAround) {
                // 专辑名称
                Text(
                    text = title,
                    modifier = Modifier.heightIn(min = 34.dp),
                    maxLines = 2
                )
            }
            if (dropdownMenu != null) {
                // 下拉菜单
                dropdownMenu(showMenu, dataList) { showMenu = false }
            }
        }
    }
}