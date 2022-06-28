package cache.image.zimageloaderlib

import android.content.Context
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import cache.image.zimageloaderlib.singleton.SingletonHolder
import java.io.File

/*
* TODO -> Need to find some way to improve caching performance
* Here we decide if we gonna caching from 30 and Higher
* or we will cache from 29 and down, so we got an Instance from CacheDirectory File
* And we use this file to store and cache our image bitmap
*
* @Created by Zeyad Alsayed
* */
class FileCacheSingleton constructor(
    private val context: Context
) {
    /*
    * Check if there an SD Card inserted then we gonna store in it
    * otherwise we gonna use cache directory in application data
    * with name ziadesm_image_cache_directory
    * */

    companion object : SingletonHolder<FileCacheSingleton, Context>(::FileCacheSingleton)


    private val cacheDir: File by lazy {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !isExternalStorageRemovable())
            context.cacheDir ?: File(Environment.getExternalStorageDirectory(), "ziadesm_image_cache_directory")
        else context.cacheDir
    }

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    fun getFile(url: String): File? {
        val filename = url.hashCode().toString()
        return File(cacheDir, filename)
    }

    fun clear() {
        val files = cacheDir.listFiles() ?: return
        for (f in files) f.delete()
    }
}