package cache.image.zimageloaderlib.interfaces

import android.content.Context
import android.widget.ImageView

interface ImageLoader {
//    fun with(context: Context): ImageLoader
    fun clear(context: Context): ImageLoader
    fun load(url: String): ImageLoader
    fun load(resDrawable: Int): ImageLoader
    fun placeholder(resDrawable: Int): ImageLoader
    fun quality(quality: Int): ImageLoader
    fun into(imageView: ImageView)
}