package cache.image.zimageloader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/*
*
* TODO (1) -> We can change our quality as we want (Builder Pattern).
* TODO (2) -> We can use of Drawable images and not caching it but display it ASAP.
* TODO (3) -> We should improve our thread working theory and using Flow and power of coroutine.
*
* @Created by Zeyad Alsayed
* */
class ImageLoader constructor(
    private val fileCache: FileCache
) {
    //Create Map (collection) to store image and image url in key value pair
    private val imageViews = Collections.synchronizedMap(WeakHashMap<ImageView, String>())
    private var executorService: ExecutorService? = null

    //handler to display images in UI thread
    var handler = Handler(Looper.myLooper()!!)

    init {
        /*
        * Should be replace with new Flow and coroutine
        * @Created by Zeyad Alsayed
        * */
        executorService = Executors.newCachedThreadPool()
    }

    // default image show in list (Before online image download)
    private val imagePlaceHolder: Int? = null//R.drawable.app_icon

    fun displayImage(url: String, imageView: ImageView?) {
        //Store image and url in Map
        imageViews[imageView] = url

        //Check image is stored in MemoryCache Map or not (see MemoryCache.java)
        val bitmap: Bitmap? = MemoryCache[url]
        if (bitmap != null) {
            // if image is stored in MemoryCache Map then
            // Show image in listview row
            imageView?.setImageBitmap(bitmap)
        } else {
            //queue Photo to download from url
            imageView?.let { queuePhoto(url, it) }

            //Before downloading image show default image
            imageView?.setImageResource(imagePlaceHolder ?: 0)
        }
    }

    private fun queuePhoto(url: String, imageView: ImageView) {
        // Store image and url in PhotoToLoad object
        val p = PhotoToLoad(url, imageView)

        // pass PhotoToLoad object to PhotosLoader runnable class
        // and submit PhotosLoader runnable to executers to run runnable
        // Submits a PhotosLoader runnable task for execution
        executorService!!.submit(PhotosLoader(p))
    }

    //Task for the queue
    class PhotoToLoad(var url: String, var imageView: ImageView)

    inner class PhotosLoader(private var photoToLoad: PhotoToLoad) : Runnable {
        override fun run() {
            try {
                //Check if image already downloaded
                if (imageViewReused(photoToLoad)) return
                // download image from web url
                val bmp: Bitmap? = getBitmap(photoToLoad.url)

                // set image data in Memory Cache
                bmp?.let { MemoryCache.put(photoToLoad.url, bmp) }
                if (imageViewReused(photoToLoad)) return

                // Get bitmap to display
                val bd = BitmapDisplay(bmp, photoToLoad)

                // Causes the Runnable bd (BitmapDisplay) to be added to the message queue.
                // The runnable will be run on the thread to which this handler is attached.
                // BitmapDisplay run method will call
                handler.post(bd)
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }
    }

    // @params @Created by Zeyad Alsayed
    private fun getBitmap(url: String): Bitmap? {
        val f = fileCache.getFile(url)

        //from SD cache
        //CHECK : if trying to decode file which not exist in cache return null
        val b = decodeFile(f)
        return b
            ?: try {
                var bitmap: Bitmap? = null
                val imageUrl = URL(url)
                val conn = imageUrl.openConnection() as HttpURLConnection
                conn.connectTimeout = 30000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = true
                val `is` = conn.inputStream

                // Constructs a new FileOutputStream that writes to file
                // if file not exist then it will create file
                val os: OutputStream = FileOutputStream(f)

                // See Utils class CopyStream method
                // It will each pixel from input stream and
                // write pixels to output stream (file)
                FileStreamableHelper.copyStream(`is`, os)
                os.close()
                conn.disconnect()

                //Now file created and going to resize file with defined height
                // Decodes image and scales it to reduce memory consumption
                bitmap = decodeFile(f)
                bitmap
            } catch (ex: Throwable) {
                ex.printStackTrace()
                if (ex is OutOfMemoryError) MemoryCache.clear()
                null
            }
    }

    //Decodes image and scales it to reduce memory consumption
    private fun decodeFile(f: File?): Bitmap? {
        try {
            //Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            val stream1 = FileInputStream(f)
            BitmapFactory.decodeStream(stream1, null, o)
            stream1.close()

            //Find the correct scale value. It should be the power of 2.
            val mRequiredSize = 85
            var tmpWidth = o.outWidth
            var tmpHeight = o.outHeight
            var scale = 1
            while (true) {
                if (tmpWidth / 2 < mRequiredSize || tmpHeight / 2 < mRequiredSize) break
                tmpWidth /= 2
                tmpHeight /= 2
                scale *= 2
            }

            //decode with current scale values
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            val stream2 = FileInputStream(f)
            val bitmap = BitmapFactory.decodeStream(stream2, null, o2)
            stream2.close()
            return bitmap
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun imageViewReused(photoToLoad: PhotoToLoad): Boolean {
        val tag = imageViews[photoToLoad.imageView]
        //Check url is already exist in imageViews MAP
        return tag == null || tag != photoToLoad.url
    }

    //Used to display bitmap in the UI thread
    inner class BitmapDisplay(private var bitmap: Bitmap?, private var photoToLoad: PhotoToLoad) :
        Runnable {
        override fun run() {
            if (imageViewReused(photoToLoad)) return

            // Show bitmap on UI
            if (bitmap != null) photoToLoad.imageView.setImageBitmap(bitmap)
            else photoToLoad.imageView.setImageResource(imagePlaceHolder ?: 0)
        }
    }

    // Clearing all cache that related to our app
    // we can use of it by space free some of user device
    fun clearCache() {
        //Clear cache directory downloaded images and stored data in maps
        MemoryCache.clear()
        fileCache.clear()
    }

    fun onDestroy() {
        executorService?.shutdownNow()
    }
}