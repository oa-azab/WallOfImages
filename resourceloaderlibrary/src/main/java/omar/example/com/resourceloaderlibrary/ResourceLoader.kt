package omar.example.com.resourceloaderlibrary

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.LruCache
import omar.example.com.resourceloaderlibrary.model.DownloadRequest
import omar.example.com.resourceloaderlibrary.model.FetchRequest
import omar.example.com.resourceloaderlibrary.util.BACKGROUND
import omar.example.com.resourceloaderlibrary.util.Const.WEB_SERVICE_BASE_URL
import omar.example.com.resourceloaderlibrary.util.UI
import retrofit2.Retrofit

class ResourceLoader private constructor(private val cacheMaxCapacity: Int) {

    private lateinit var cache: LruCache<String, ByteArray>
    private lateinit var service: DownloadService

    // This handlerThread is responsible for queuing resource fetch requests
    private val handlerThread = HandlerThread("RequestsThread")
    private lateinit var requestsHandler: Handler

    private val currentlyDownloading: MutableMap<String, DownloadRequest> = mutableMapOf()
    private val fetchRequests: MutableMap<String, FetchRequest> = mutableMapOf()

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

    /**
     * Setup the local cache with value entered by user or default value
     */
    private fun setupCache() {
        val cacheSize = if (cacheMaxCapacity > defaultCacheSize()) defaultCacheSize() else cacheMaxCapacity
        cache = object : LruCache<String, ByteArray>(cacheSize) {
            override fun sizeOf(key: String, value: ByteArray): Int {
                return value.size / 1024
            }
        }
        Log.d(TAG, "[Init] cache size = $cacheSize")
    }

    private fun setupService() {
        val retrofit = Retrofit.Builder()
            .baseUrl(WEB_SERVICE_BASE_URL)
            .build()
        service = retrofit.create(DownloadService::class.java)
    }

    /**
     * Load resource from cache if exist or from url if not exist
     *
     * return fetchRequestId if you want to cancel loading is resource
     */
    private fun load(url: String, callback: (ResourceLoadResult<ByteArray>) -> Unit): String {
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
            val call = downloadRequest.call
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val input = response.body()?.byteStream()
                    if (input != null) {
                        // transform data to byteArray
                        val resource = input.readBytes()

                        // cache resource
                        cache.put(downloadRequest.url, resource)

                        // deliver result
                        downloadResult(downloadRequest.url, ResourceLoadSuccess(resource))
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
     * Passes download resource result
     */
    private fun downloadResult(
        url: String,
        resourceLoadResult: ResourceLoadResult<ByteArray>
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
    private fun cancelLoad(fetchRequestId: String) {
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

    companion object {
        private const val TAG = "ResourceLoader"
        private var INSTANCE: ResourceLoader? = null

        /**
         * Initialize the library with cache size if input cache size is too high
         * or not entered, a default cache size is provided
         *
         * cacheMaxCapacity: cache size in kilobytes
         */
        fun init(cacheMaxCapacity: Int = defaultCacheSize()) {
            if (INSTANCE == null) {
                INSTANCE = ResourceLoader(cacheMaxCapacity)
            }
        }

        private fun defaultCacheSize(): Int {
            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
            return maxMemory / 4
        }

        fun load(url: String, callback: (ResourceLoadResult<ByteArray>) -> Unit): String {
            return INSTANCE?.load(url, callback) ?: throw Exception("Must call ResourceLoader.init()")
        }

        fun cancelLoad(fetchRequestId: String) {
            INSTANCE?.cancelLoad(fetchRequestId) ?: throw Exception("Must call ResourceLoader.init()")
        }
    }
}

