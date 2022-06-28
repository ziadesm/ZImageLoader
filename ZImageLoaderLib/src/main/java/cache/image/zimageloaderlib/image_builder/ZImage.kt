package cache.image.zimageloaderlib.image_builder
import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import cache.image.zimageloaderlib.interfaces.ImageLoader
import cache.image.zimageloaderlib.model.AllImagePropertiesModel
import cache.image.zimageloaderlib.FileCacheSingleton
import cache.image.zimageloaderlib.ImageLoaderSingleton

class ZImage private constructor(
    private val context: Context
): ImageLoader {
    @Volatile
    private lateinit var mFileCache: FileCacheSingleton
    private lateinit var mImageLoader: ImageLoaderSingleton

    private lateinit var mProperties: AllImagePropertiesModel

    //companion object : SingletonHolder<ZImage, Context>(::ZImage)

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ZImage? = null

        fun with(context: Context): ZImage =
            instance ?: synchronized(this) {
                instance ?: ZImage(context.applicationContext).also { instance = it }
            }
    }

    /*override fun with(context: Context): ImageLoader {
        mFileCache = FileCacheSingleton.getInstance(context)
        mProperties = AllImagePropertiesModel()
        return this
    }*/

    override fun clear(context: Context): ImageLoader {
        mFileCache = FileCacheSingleton.getInstance(context)
        mImageLoader = ImageLoaderSingleton(mFileCache)
        mImageLoader.clearCache()
        return this
    }

    override fun load(url: String): ImageLoader {
        mProperties = AllImagePropertiesModel()
        mFileCache = FileCacheSingleton.getInstance(context)
        mImageLoader = ImageLoaderSingleton(mFileCache)
        mProperties.url = url
        return this
    }

    override fun load(resDrawable: Int): ImageLoader {
        mProperties = AllImagePropertiesModel()
        mProperties.resDrawable = resDrawable
        mFileCache = FileCacheSingleton.getInstance(context)
        mImageLoader = ImageLoaderSingleton(mFileCache)
        return this
    }

    override fun placeholder(resDrawable: Int): ImageLoader {
        mProperties.placeholder = resDrawable
        return this
    }

    override fun quality(quality: Int): ImageLoader {
        mProperties.quality = quality
        return this
    }

    override fun into(imageView: ImageView) {
        mImageLoader.displayImage(mProperties.url, mProperties.placeholder, mProperties.quality, imageView)
    }
}