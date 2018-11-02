package omar.example.com.resourceloaderlibrary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.LruCache
import okhttp3.ResponseBody
import omar.example.com.resourceloaderlibrary.model.DownloadRequest
import omar.example.com.resourceloaderlibrary.model.FetchRequest
import omar.example.com.resourceloaderlibrary.util.BACKGROUND
import omar.example.com.resourceloaderlibrary.util.UI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.InputStream

object ResourceLoader {

    private const val TAG = "ResourceLoader"

    private lateinit var cache: LruCache<String, Bitmap>
    private lateinit var service: DownloadService
    private val handlerThread = HandlerThread("RequestsThread")
    private lateinit var requestsHandler: Handler

    init {
        // setup cache
        setupCache()

        // Setup download service
        setupService()

        // setup handler
        setupHandler()
    }

    private fun setupHandler() {
        handlerThread.start()
        requestsHandler = Handler(handlerThread.looper)
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

    fun loadBitmapResource(url: String, callback: (Bitmap) -> Unit) {
        val cachedResource = cache.get(url)
        if (cachedResource != null) {
            Log.d(TAG, "[loadBitmapResource] key:$url found in cache")
            callback.invoke(cachedResource)
            return
        }

        Log.d(TAG, "[loadBitmapResource} url= $url")
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

    ////////////////////////////////////////

    private val currentlyDownloading: MutableMap<String, DownloadRequest> = mutableMapOf()
    private val fetchRequests: MutableMap<String, FetchRequest> = mutableMapOf()

    fun load(url: String, callback: (ResourceLoadResult<Bitmap>) -> Unit): String {
        val fetchRequest = FetchRequest(url, callback)

        requestsHandler.post {

            // Check if resource is saved in cache
            val cachedResource = cache.get(url)
            if (cachedResource != null) {
                Log.d(TAG, "[loadBitmapResource] key:$url found in cache")
                UI.post { callback.invoke(ResourceLoadSuccess(cachedResource)) }
                return@post
            }

            val downloadRequest = currentlyDownloading[url]

            // if the resource is downloading now just add the fetchRequest
            if (downloadRequest != null) {
                fetchRequests[fetchRequest.id] = fetchRequest
                downloadRequest.fetchRequests.add(fetchRequest)
            }
            // Create the downloadRequest and start download
            else {
                val downloadCall = service.downloadResource(url)
                val newDownloadRequest = DownloadRequest(url, downloadCall, mutableListOf(fetchRequest))

                fetchRequests[fetchRequest.id] = fetchRequest
                currentlyDownloading[url] = newDownloadRequest

                download(newDownloadRequest)
            }

        }

        return fetchRequest.id
    }

    /**
     * Downloads url and cache it in memory
     */
    private fun download(downloadRequest: DownloadRequest) {
        BACKGROUND.execute {
            Log.d(TAG, "[download] sleep for 5 seconds")
            Thread.sleep(5000)
            Log.d(TAG, "[download] wakeup for 5 seconds")
            val call = downloadRequest.call
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val input = response.body()?.byteStream()
                    if (input != null) {
                        // transform data to bitmap
                        val bitmap = inputStreamToBitmap(input)

                        // cache bitmap
                        cache.put(downloadRequest.url, bitmap)

                        // deliver result
                        downloadResult(downloadRequest.url, ResourceLoadSuccess(bitmap))
                    } else {
                        downloadResult(downloadRequest.url, ResourceLoadError(Exception("Input stream NULL")))
                    }
                } else {
                    downloadResult(downloadRequest.url, ResourceLoadError(Exception(response.message())))
                }
            } catch (e: Exception) {
                downloadResult(downloadRequest.url, ResourceLoadError(e))
            }
        }
    }

    /**
     * Decode inputstream to bitmap
     */
    private fun inputStreamToBitmap(input: InputStream): Bitmap {
        val bitmap = BitmapFactory.decodeStream(input)
        Log.d(TAG, "[inputStreamToBitmap] bitmap size = ${bitmap.byteCount / 1024}")
        return bitmap
    }

    /**
     * Passes download resource result
     */
    private fun downloadResult(
        url: String,
        resourceLoadResult: ResourceLoadResult<Bitmap>
    ) {
        requestsHandler.post {
            val downloadRequest = currentlyDownloading[url]
            currentlyDownloading.remove(url)

            downloadRequest?.fetchRequests?.forEach {
                UI.post { it.callback.invoke(resourceLoadResult) }
                fetchRequests.remove(it.id)
            }
        }
    }

    /**
     * Cancels fetching resource by its request id
     * and cancel the downloads if no other sources requesting this resource
     */
    fun cancelLoad(fetchRequestId: String) {
        requestsHandler.post {
            val fetchRequest = fetchRequests[fetchRequestId]
            if (fetchRequest != null) {
                val downloadRequest = currentlyDownloading[fetchRequest.url]
                if (downloadRequest != null) {
                    Log.d(TAG, "[cancelLoad] calling cancel on downloadRequest")
                    downloadRequest.cancel(fetchRequest)
                } else {
                    Log.d(TAG, "[cancelLoad] downloadRequest is null")
                }
            } else {
                Log.d(TAG, "[cancelLoad] fetchRequest is null")
            }
        }
    }
}

