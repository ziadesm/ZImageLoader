package cache.image.zimageloader

import android.graphics.Bitmap
import android.util.Log
import java.util.*

/*
* TODO -> Need to find some way to improve caching performance
* Here we decide if we gonna caching from 30 and Higher
* or we will cache from 29 and down, so we got an Instance from CacheDirectory File
* And we use this file to store and cache our image bitmap
*
* @Created by Zeyad Alsayed
* */
object MemoryCache {
    private const val TAG = "MemoryCache"

    //Last argument true for LRU ordering
    private val cache = Collections.synchronizedMap(
        LinkedHashMap<String, Bitmap>(10, 1.5f, true)
    )

    //current allocated size
    private var size: Long = 0

    //max memory cache folder used to download images in bytes
    private var limit: Long = 1000000

    init {
        setLimit(Runtime.getRuntime().maxMemory() / 4)
    }

    private fun setLimit(new_limit: Long) {
        limit = new_limit
        Log.i(TAG, "MemoryCache will use up to " + limit / 1024.0 / 1024.0 + "MB")
    }

    // @Created by Zeyad Alsayed
    operator fun get(id: String): Bitmap? {
        return try {
            if (!cache.containsKey(id)) null else cache[id]
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
            null
        }
    }

    fun put(id: String, bitmap: Bitmap) {
        try {
            if (cache.containsKey(id)) size -= getSizeInBytes(cache[id])
            cache[id] = bitmap
            size += getSizeInBytes(bitmap)
            checkSize()
        } catch (th: Throwable) {
            th.printStackTrace()
        }
    }

    private fun checkSize() {
        Log.i(TAG, "cache size=" + size + " length=" + cache.size)
        if (size > limit) {
            val iter: MutableIterator<Map.Entry<String, Bitmap>> =
                cache.entries.iterator() //least recently accessed item will be the first one iterated
            while (iter.hasNext()) {
                val (_, value) = iter.next()
                size -= getSizeInBytes(value)
                iter.remove()
                if (size <= limit) break
            }
            Log.i(TAG, "Clean cache. New size " + cache.size)
        }
    }

    // @Created by Zeyad Alsayed
    fun clear() {
        try {
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78
            cache.clear()
            size = 0
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }
    }

    // @Created by Zeyad Alsayed
    private fun getSizeInBytes(bitmap: Bitmap?): Long {
        return if (bitmap == null) 0 else (bitmap.rowBytes * bitmap.height).toLong()
    }
}