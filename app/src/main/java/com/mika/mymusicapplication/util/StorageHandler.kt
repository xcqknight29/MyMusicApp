package com.mika.mymusicapplication.util

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mika.mymusicapplication.R
import com.mika.mymusicapplication.alertDialogBuilder
import com.mika.mymusicapplication.model.SongInfo
import java.io.File

/** 存储操作类 */
class StorageHandler(private val context: Context) {

    /** 从MediaStore中获取something，返回MutableMap */
    fun getByMediaStore(contentResolver: ContentResolver, sortBasis: String = MediaStore.Audio.Media.ALBUM): MutableMap<String, MutableList<SongInfo>> {
        // 创建Map存储数据
        val dataMap = mutableMapOf<String, MutableList<SongInfo>>()
        // 用contentResolver从MediaStore获取媒体文件并以sortBasis(album, artist)为排序依据
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            sortBasis
        )?.use { cursor ->
            if (cursor.moveToFirst()) {// 当cursor不为空
                do {
                    // 获取指针指向的歌曲的sortBasis的标题
                    val basisTitle = cursor.getString(cursor.getColumnIndexOrThrow(sortBasis))
                    // 当该分类依据对应的MutableList尚不存在于Map中时
                    if (dataMap[basisTitle] == null) {
                        // 创建一个空的MutableList以存储该分类依据对应的歌曲
                        dataMap[basisTitle] = mutableListOf()
                    }
                    // 向Map中的存在的MutableList添加一首歌曲
                    dataMap[basisTitle]?.add(
                        SongInfo(
                            title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                            album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                            artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                            uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                            duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                            size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
                        )
                    )
                } while (cursor.moveToNext()) // 当cursor中还存在下一个元素
            }
        }
        // 返回Map
        return dataMap
    }

    /** 从MediaStore中获取专辑 */
    fun getAlbumByMediaStore(contentResolver: ContentResolver): MutableMap<String, MutableList<SongInfo>> {
        return getByMediaStore(contentResolver, MediaStore.Audio.Media.ALBUM)
    }

    /** 从MediaStore中获取艺术家列表 */
    fun getArtistByMediaStore(contentResolver: ContentResolver): MutableMap<String, MutableList<SongInfo>> {
        return getByMediaStore(contentResolver, MediaStore.Audio.Media.ARTIST)
    }

    /** 获取歌单信息 */
    fun getPlaylistFromStorage(): MutableMap<String, MutableList<SongInfo>> {
        // 找到目标文件夹
        val mkdir = File(context.filesDir, "playlist")
        // 读取文件列表
        val fileArray: Array<out File>? = mkdir.listFiles()
        // 创建Map用于储存播单数据
        val playlistMap: MutableMap<String, MutableList<SongInfo>> = mutableMapOf()
        // Gson对象用于解析json文件
        val gson = Gson()
        // 对每个文件处理
        fileArray?.forEach {
            // 将文件名字符串以"."为界拆分
            val nameList = it.name.split('.')
            // 排除后缀不为.json的文件
            if (nameList[nameList.size - 1] != "json") {
                Log.e("MyMusicApplication", "文件格式不符：${it.name}")
            } else {
                Log.i("MyMusicApplication", "正在解析：${it.path}")
                // 截去后5个字符".json"
                val playlistName = it.name.take(it.name.length - 5)
                // 读取数据
                val jsonData: String = it.readText()
                // 声明转换的目标类型MutableList<SongInfo>
                val targetType = object : TypeToken<MutableList<SongInfo>>() {}.type
                // 将数据用Gson转换成MutableList<SongInfo>并存入Map中
                playlistMap[playlistName] = gson.fromJson(jsonData, targetType)
            }
        }
        // 返回Map
        return playlistMap
    }

    /** 从MediaStore中重新获取歌曲信息 */
    fun getSongsByMediaStore(contentResolver: ContentResolver): List<SongInfo> {
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

    /** 向播单中添加歌曲 */
    fun addSongToPlaylistStorage(
        targetPlaylist: String,
        songInfo: SongInfo?,
        songsInfo: List<SongInfo>?
    ) {
        // 获取文件
        val file = File(context.filesDir, "playlist/$targetPlaylist.json")
        // 创建Gson对象用于转换json格式数据
        val gson = Gson()
        // 导出文件数据
        var jsonData: String = file.readText()
        // 声明想要转换成的目标类型
        val targetType = object : TypeToken<MutableList<SongInfo>>() {}.type
        // 将json转换成MutableList格式
        val listData: MutableList<SongInfo> = gson.fromJson(jsonData, targetType)
        if (songInfo != null) {
            addOrReplaceSongByTitle(songInfo, listData)
        } else songsInfo!!.forEach {
            addOrReplaceSongByTitle(it, listData)
        }
        // 将MutableList转回json格式
        jsonData = gson.toJson(listData)
        // 将数据写回文件中
        file.writeText(jsonData)
    }

    /** 添加新播单 */
    fun addPlaylist(input: String) {
        val gson = Gson()
        // 创建目录
        File(context.filesDir, "playlist").mkdirs()
        // 获取文件
        val file = File(context.filesDir, "playlist/$input.json")
        // 检查文件是否存在
        if (file.exists()) {
            // 弹窗提示playlist已存在
            alertDialogBuilder!!.setMessage(R.string.playlist_exists)
            alertDialogBuilder!!.show()
            return
        } else {
            // 创建文件
            file.createNewFile()
            file.writeText(gson.toJson(listOf<SongInfo>()))
        }
    }

    /** 删除播单 */
    fun deletePlaylist(playlistName: String) {
        // 获取文件
        val file = File(context.filesDir, "playlist/$playlistName.json")
        if (file.exists()) {
            file.delete()
        }
    }

    /** 添加或替换相同名字的歌曲 */
    private fun addOrReplaceSongByTitle(newSong: SongInfo, targetList: MutableList<SongInfo>) {
        val oldSong = targetList.find { it.title == newSong.title }
        if (oldSong == null) {
            // 新添加一首歌的数据
            targetList.add(newSong)
        } else {
            // 覆盖旧的歌曲数据
            targetList.remove(oldSong)
            targetList.add(newSong)
        }
    }

}
