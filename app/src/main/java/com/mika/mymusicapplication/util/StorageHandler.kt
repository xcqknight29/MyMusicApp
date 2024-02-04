package com.mika.mymusicapplication.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.alertDialogBuilder
import com.mika.mymusicapplication.model.AlbumInfo
import com.mika.mymusicapplication.model.ArtistInfo
import com.mika.mymusicapplication.model.Playlist
import com.mika.mymusicapplication.model.SongInfo
import java.io.File

/** 存储操作类 */
class StorageHandler(
    private val context: Context,
    private val contentResolver: ContentResolver,
) {

    /** 从MediaStore中获取something，返回MutableMap */
    /*fun getByMediaStore(sortBasis: String = MediaStore.Audio.Media.ALBUM): MutableMap<String, MutableList<SongInfo>> {

        val dataMap = mutableMapOf<String, MutableList<SongInfo>>() // 创建Map存储数据

        val cursor = contentResolver.query( // 用contentResolver从MediaStore获取媒体文件并以sortBasis(album, artist)为排序依据
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, // uri
            null,
            null,
            null,
            sortBasis // sort order
        ) ?: return dataMap

        if (!cursor.moveToFirst()) return dataMap // 当cursor内容为空时返回

        do {

            val basisTitle = cursor.getString(cursor.getColumnIndexOrThrow(sortBasis)) // 获取指针指向的歌曲的sortBasis的标题
            var thumbnail: Bitmap? = null // 缩略图: SDK29以上使用
            var albumArt: String? = null // SDK29以下使用

            if (dataMap[basisTitle] == null) { // 当该分类依据对应的MutableList尚不存在于Map中时
                dataMap[basisTitle] = mutableListOf() // 创建一个空的MutableList以存储该分类依据对应的歌曲
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 当版本高于SDK29
                val thumpId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)) // 获取Album的ID
                val imageUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thumpId)
                thumbnail = contentResolver.loadThumbnail(imageUri, Size(100, 100), null)
            } else {
                albumArt = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
            }

            val songInfo = SongInfo( // 创建歌曲信息
                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                thumbnail = thumbnail,
                albumArt = albumArt,
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
            )

            dataMap[basisTitle]?.add(songInfo) // 向Map中的存在的MutableList添加一首歌曲

        } while (cursor.moveToNext()) // 当cursor中还存在下一个元素

        cursor.close() // 关闭cursor

        return dataMap // 返回Map

    }*/

    /** 从MediaStore中获取专辑信息并获取其中的歌曲 */
    fun getAlbumFromMediaStore(): List<AlbumInfo> {

        val albumList = mutableListOf<AlbumInfo>()

        val cursor = contentResolver.query( // 用contentResolver从MediaStore获取媒体文件并以sortBasis(album, artist)为排序依据
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, // uri
            null,
            null,
            null,
            MediaStore.Audio.Albums.ALBUM // sort order
        ) ?: return albumList

        if (!cursor.moveToFirst()) return albumList // 当cursor内容为空时返回

        do {

            val albumInfo = AlbumInfo( // 创建专辑信息
                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)),
                artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)),
                songCount = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)),
            )

            getAlbumCover(albumInfo, cursor) // 获取专辑封面

            albumInfo.songList = getSongsByAlbum( // 获取专辑歌曲信息
                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID))
            )

            albumList.add(albumInfo) // 将专辑添加入List中

        } while (cursor.moveToNext()) // 当cursor中还存在下一个元素继续循环

        cursor.close() // 关闭cursor
        return albumList // 返回List

    }

    /** 获取专辑封面 */
    fun getAlbumCover(albumInfo: AlbumInfo, cursor: Cursor) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // SDK版本 >= 29 ?
            albumInfo.thumbnail = getAlbumCoverAsThumbnail(cursor)
        } else {
            albumInfo.albumArt = getAlbumCoverAsAlbumArt(cursor)
        }

    }

    /** 获取专辑封面, 返回缩略图形式 */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAlbumCoverAsThumbnail(cursor: Cursor): Bitmap {
        val contentUri: Uri = ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, // contentUri
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)), // id
        )
        return contentResolver.loadThumbnail(// 缩略图
            contentUri, // uri
            Size(100, 100), // size
            null, // signal
        )
    }

    /** 获取专辑封面, 返回String形式 */
    fun getAlbumCoverAsAlbumArt(cursor: Cursor) : String {
        return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
    }

    /** 通过专辑获取歌曲信息 */
    fun getSongsByAlbum(albumId: Long): List<SongInfo> {

        val songList = mutableListOf<SongInfo>() // 创建List存储数据

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, // uri
            null, // projection
            "${MediaStore.Audio.Media.ALBUM_ID} = ?", // selection
            arrayOf("$albumId"), // selectionArgs
            MediaStore.Audio.Media.TITLE, // sort order
        ) ?: return songList

        if (!cursor.moveToFirst()) return songList

        do {
            val songInfo = SongInfo(
                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                thumbnail = null,
                albumArt = null,
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
            )
            songList.add(songInfo)
        } while (cursor.moveToNext())

        cursor.close()
        return songList

    }

    /** 从MediaStore中获取艺术家 */
    fun getArtistFromMediaStore(): List<ArtistInfo> {

        val artistList = mutableListOf<ArtistInfo>()
        // 用contentResolver从MediaStore获取媒体文件并以sortBasis(album, artist)为排序依据
        val cursor = contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, // uri
            null, // projection
            null, // selection
            null, // selectionArgs
            MediaStore.Audio.Artists.ARTIST // sort order
        ) ?: return artistList

        if (!cursor.moveToFirst()) return artistList // 当cursor内容为空时返回

        do {
            val artistInfo = ArtistInfo( // 创建艺术家信息
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
            )
            getArtistCover(artistInfo) // 获取艺术家封面
            artistInfo.songList = getSongByArtist( // 获取艺术家歌曲信息
                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
            )
            artistList.add(artistInfo) // 将艺术家添加入List中
        } while (cursor.moveToNext()) // 当cursor中还存在下一个元素继续循环

        cursor.close() // 关闭cursor
        return artistList // 返回List

    }

    /** 获取艺术家前4个专辑的封面 */
    fun getArtistCover(artistInfo: ArtistInfo) {

        val cursor = contentResolver.query( // 根据艺术家获取专辑
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, // uri
            null, // projection
            "${MediaStore.Audio.Albums.ARTIST} = ?", // selection
            arrayOf(artistInfo.name), // selectionArgs
            MediaStore.Audio.Albums.ALBUM // sort order
        ) ?: return

        if (!cursor.moveToFirst()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // SDK版本 >= 29 ?

            val coverList = mutableListOf<Bitmap>()

            do {
                coverList.add(getAlbumCoverAsThumbnail(cursor))
            } while (cursor.moveToNext() && coverList.size < 4)

            artistInfo.thumbnail = coverList

        } else {

            val coverList = mutableListOf<String>()

            do {
                coverList.add(getAlbumCoverAsAlbumArt(cursor))
            } while (cursor.moveToNext() && coverList.size < 4)

            artistInfo.albumArt = coverList

        }

        cursor.close()

    }

    /** 根据艺术家获取歌曲信息 */
    fun getSongByArtist(artistId: Long) : List<SongInfo> {

        val songList = mutableListOf<SongInfo>() // 创建List存储数据

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, // uri
            null, // projection
            "${MediaStore.Audio.Media.ARTIST_ID} = ?", // selection
            arrayOf("$artistId"), // selectionArgs
            MediaStore.Audio.Media.TITLE, // sort order
        ) ?: return songList

        if (!cursor.moveToFirst()) return songList

        do {
            val songInfo = SongInfo(
                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                thumbnail = null,
                albumArt = null,
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
            )
            songList.add(songInfo)
        } while (cursor.moveToNext())

        cursor.close()
        return songList

    }

    /** 从MediaStore中重新获取歌曲信息 */
    /*fun getSongsByMediaStore(): List<SongInfo> {

        val playList = mutableListOf<SongInfo>()

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, // uri
            null,
            null,
            null,
            MediaStore.Audio.Media.TITLE // sort order
        ) ?: return playList

        if (!cursor.moveToFirst()) return playList// cursor判空

        do {// 循环读取audio
            var thumbnail: Bitmap? = null// 缩略图: SDK29以上使用
            var albumArt: String? = null// 以下

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 判断SDK版本
                // 当版本高于SDK29
                val thumpId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID))
                val imageUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thumpId)
                thumbnail = contentResolver.loadThumbnail(imageUri, Size(100, 100), null)
            } else {
                albumArt = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
            }

            val songInfo = SongInfo(
                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                thumbnail = thumbnail,
                albumArt = albumArt,
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
            )

            playList.add(songInfo) // 添加到List中

        } while (cursor.moveToNext())

        cursor.close() // 关闭cursor
        return playList

    }*/

    /** 从MediaStore中获取专辑 */
    /*fun getAlbumByMediaStore(): MutableMap<String, MutableList<SongInfo>> {
        return getByMediaStore(MediaStore.Audio.Media.ALBUM)
    }

    /** 从MediaStore中获取艺术家列表 */
    fun getArtistByMediaStore(): MutableMap<String, MutableList<SongInfo>> {
        return getByMediaStore(MediaStore.Audio.Media.ARTIST)
    }*/

    /** 从存储中读取歌单信息 */
    /*fun getPlaylistFromStorage(): MutableMap<String, MutableList<SongInfo>> {
        val mkdir = File(context.filesDir, "playlist")// 找到目标文件夹
        val fileArray: Array<out File>? = mkdir.listFiles()// 读取文件列表
        val playlistMap: MutableMap<String, MutableList<SongInfo>> = mutableMapOf()// 创建Map用于储存播单数据
        val gson = Gson()// Gson对象用于解析json文件
        fileArray?.forEach {// 对每个文件处理
            val nameList = it.name.split('.')// 将文件名字符串以"."为界拆分
            if (nameList[nameList.size - 1] != "json") {// 排除后缀不为.json的文件
                Log.e("MyMusicApplication", "文件格式不符：${it.name}")
            } else {
                Log.i("MyMusicApplication", "正在解析：${it.path}")
                val playlistName = it.name.take(it.name.length - 5)// 截去后5个字符".json"
                val jsonData: String = it.readText()// 读取数
                val targetType = object : TypeToken<MutableList<SongInfo>>() {}.type// 声明转换的目标类型MutableList<SongInfo>
                playlistMap[playlistName] = gson.fromJson(jsonData, targetType)// 将数据用Gson转换成MutableList<SongInfo>并存入Map中
            }
        }
        return playlistMap// 返回Map
    }*/

    /** 从存储中获取播单 */
    fun getPlaylistFromStorage(): List<Playlist> {
        val dir = File(context.filesDir, "playlist")// 找到目标文件夹
        val fileArray: Array<out File>? = dir.listFiles()// 读取文件列表
        val playlistList = mutableListOf<Playlist>() // 创建List用于储存播单数据
        val gson = Gson()// Gson对象用于解析json文件
        fileArray?.forEach {// 对每个文件处理
            val nameList = it.name.split('.')// 将文件名字符串以"."为界拆分
            if (nameList[nameList.size - 1] != "json") {// 排除后缀不为.json的文件
                Log.e("MyMusicApplication", "文件格式不符: ${it.name}")
            } else {
                Log.i("MyMusicApplication", "正在解析: ${it.path}")
                try {
                    val jsonData: String = it.readText()// 读取数据
                    val playlist: Playlist = gson.fromJson(jsonData, Playlist::class.java)
                    playlistList.add(playlist)// 将数据用Gson转换成Playlist数据对象并存入List中
                } catch (ex: Exception) {
                    Log.e("MyMusicApplication", ex.message.toString())
                }
            }
        }
        return playlistList// 返回List
    }

    /** 读取当前播放歌曲索引 */
    fun loadCurrentIndex(): Int {
        // 获取根据字符串setting获取SharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("playerHandle", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("playIndex", 0)// 获取playIndex值
    }

    /** 存储当前播放歌曲索引 */
    fun saveCurrentIndex(currentPlayIndex: Int) {// 将索引存入sharedPreferences中
        // 获取根据字符串setting获取SharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("playerHandle", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()// 获取editor
        editor.putInt("playIndex", currentPlayIndex)
        editor.apply()
    }

    /** 读取当前播放歌曲索引 */
    fun loadPlayMode(): PlayMode {

        // 获取根据字符串setting获取SharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("playerHandle", Context.MODE_PRIVATE)

        return when (sharedPreferences.getInt("playMode", 0)) {// 获取playIndex值
            0 -> { PlayMode.SEQUENTIAL }

            1 -> { PlayMode.SHUFFLE }

            else -> { PlayMode.LOOP }
        }

    }

    /** 存储当前播放歌曲索引 */
    fun savePlayMode(playMode: PlayMode) {
        // 获取根据字符串setting获取SharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("playerHandle", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()// 获取editor
        when(playMode) {
            PlayMode.SEQUENTIAL -> { editor.putInt("playMode", 0) }
            PlayMode.SHUFFLE -> { editor.putInt("playMode", 1) }
            else -> { editor.putInt("playMode", 2) }
        }
        editor.apply()
    }

    /** 读取当前播放列表 */
    fun loadCurrentPlaylist(): List<SongInfo> {
        val file = File(context.filesDir, "current/currentPlaylist.json")
        if (!file.exists()) {
            return listOf()
        }
        val jsonData: String = file.readText()
        // 声明想要转换成的目标类型
        val targetType = object : TypeToken<List<SongInfo>>() {}.type
        return Gson().fromJson(jsonData, targetType)
    }

    /** 存储当前播放列表 */
    fun saveCurrentPlaylist(currentPlaylist: List<SongInfo>) {
        val jsonData: String = Gson().toJson(currentPlaylist)
        File(context.filesDir, "current").mkdirs()
        val file = File(context.filesDir, "current/currentPlaylist.json")
        file.writeText(jsonData)
    }

    /** 向播单中添加歌曲 */
    fun addSongToPlaylist(
        targetPlaylist: String,
        songInfo: SongInfo?,
        songsInfo: List<SongInfo>?
    ) {
        val file = File(context.filesDir, "playlist/$targetPlaylist.json") // 获取文件
        val gson = Gson() // 创建Gson对象用于转换json格式数据
        var jsonData: String = file.readText() // 导出文件数据
        // 声明想要转换成的目标类型
        val targetType = object : TypeToken<MutableList<SongInfo>>() {}.type
        // 将json转换成MutableList格式
        val listData: MutableList<SongInfo> = gson.fromJson(jsonData, targetType)
        if (songInfo != null) {
            addOrReplaceSongByTitle(songInfo, listData)
        } else songsInfo!!.forEach {
            addOrReplaceSongByTitle(it, listData)
        }
        jsonData = gson.toJson(listData) // 将MutableList转回json格式
        file.writeText(jsonData) // 将数据写回文件中
    }

    /** 添加新播单 */
    fun addPlaylist(input: String) {
        val gson = Gson()
        File(context.filesDir, "playlist").mkdirs() // 创建目录
        val file = File(context.filesDir, "playlist/$input.json") // 获取文件
        if (file.exists()) { // 检查文件是否存在
            // 弹窗提示playlist已存在
            alertDialogBuilder.setMessage(R.string.playlist_exists)
            alertDialogBuilder.show()
            return
        } else {
//            file.createNewFile() // 创建文件
            file.writeText(gson.toJson(Playlist(input)))
        }
    }

    /** 将一个新的播单代替旧播单 */
    fun replacePlaylist(playlist: Playlist) {
        val file = File(context.filesDir, "playlist/${playlist.name}.json") // 获取文件
        val gson = Gson() // 创建Gson对象用于转换json格式数据
        val jsonData = gson.toJson(playlist) // 将MutableList转回json格式
        file.writeText(jsonData) // 将数据写回文件中
    }

    /** 删除播单 */
    fun deletePlaylist(playlistName: String) {
        val file = File(context.filesDir, "playlist/$playlistName.json") // 获取文件
        if (file.exists()) {
            file.delete()
        }
    }

    /** 添加或替换相同名字的歌曲 */
    private fun addOrReplaceSongByTitle(newSong: SongInfo, targetList: MutableList<SongInfo>) {
        val oldSong = targetList.find { it.title == newSong.title }
        if (oldSong == null) {
            targetList.add(newSong)
        } else {
            targetList.remove(oldSong)
            targetList.add(newSong)
        }
    }

}
