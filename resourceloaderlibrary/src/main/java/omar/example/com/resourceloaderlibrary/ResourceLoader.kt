package omar.example.com.resourceloaderlibrary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.InputStream

object ResourceLoader {

    private const val TAG = "ResourceLoader"

    private lateinit var cache: LruCache<String, Bitmap>
    private lateinit var service: DownloadService

    init {
        // setup cache
        setupCache()

        // Setup download service
        setupService()
    }

    private fun setupCache() {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        cache = object : LruCache<String, Bitmap>(cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
        Log.d(TAG, "[Init] cache size = $cacheSize")
    }

    private fun setupService() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://pastebin.com")
            .build()
        service = retrofit.create(DownloadService::class.java)
    }

    fun loadResource(url: String, callback: (Bitmap) -> Unit) {
        val cachedResource = cache.get(url)
        if (cachedResource != null) {
            Log.d(TAG, "[loadResource] key:$url found in cache")
            callback.invoke(cachedResource)
            return
        }

        Log.d(TAG, "[loadResource} url= $url")
        service.downloadResource(url).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "[onFailure]")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(TAG, "[onResponse]")
                if (response.isSuccessful) {
                    Log.d(TAG, "[onResponse] isSuccessful")
                    val input = response.body()?.byteStream()
                    if (input != null) saveBitmap(url, input, callback)
                    else Log.d(TAG, "[onResponse] inputstream is null")
                } else {
                    Log.d(TAG, "[onResponse] Not Successful")
                }
            }

        })
    }

    fun saveBitmap(key: String, input: InputStream, callback: (Bitmap) -> Unit) {
        val bitmap = BitmapFactory.decodeStream(input)
        cache.put(key, bitmap)
        callback.invoke(bitmap)
        Log.d(TAG, "[saveBitmap] bitmap size = ${bitmap.byteCount / 1024}")
    }

}