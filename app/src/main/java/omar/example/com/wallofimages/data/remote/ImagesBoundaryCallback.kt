package omar.example.com.wallofimages.data.remote

import android.util.Log
import androidx.paging.PagedList
import omar.example.com.wallofimages.data.Image
import omar.example.com.wallofimages.data.local.ImagesDao
import omar.example.com.wallofimages.util.BACKGROUND

/**
 * This class is responsible for fetching new pages
 * when user reaches the end of current local images
 */
class ImagesBoundaryCallback(private val imagesDao: ImagesDao) : PagedList.BoundaryCallback<Image>() {

    private var page = 2
    private var isLoading = false

    override fun onItemAtEndLoaded(itemAtEnd: Image) {

        if(isLoading) return
        isLoading = true

        Log.d(TAG, "[onItemAtEndLoaded] nextPage=$page")

        RemoteDataSource.getInstance().fetchImages(page) {
            when (it) {
                is NetworkSuccess -> {
                    Log.d(TAG, "Ui data size = ${it.data.size}")
                    BACKGROUND.execute { imagesDao.insert(it.data) }
                    page++
                }
                is NetworkError -> {
                    Log.d(TAG, "Network error ${it.error.message}")
                }
            }
            isLoading = false
        }
    }

    companion object {
        private const val TAG = "ImagesBoundaryCallback"
    }

}
