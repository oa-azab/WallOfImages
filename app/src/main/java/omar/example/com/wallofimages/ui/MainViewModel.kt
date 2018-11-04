package omar.example.com.wallofimages.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import omar.example.com.wallofimages.data.Image
import omar.example.com.wallofimages.data.local.ImagesDao
import omar.example.com.wallofimages.data.remote.ImagesBoundaryCallback
import omar.example.com.wallofimages.data.remote.NetworkError
import omar.example.com.wallofimages.data.remote.NetworkSuccess
import omar.example.com.wallofimages.data.remote.RemoteDataSource
import omar.example.com.wallofimages.util.BACKGROUND
import omar.example.com.wallofimages.util.Const.PAGE_SIZE

class MainViewModel(private val imagesDao: ImagesDao) : ViewModel() {

    val images: LiveData<PagedList<Image>>
    val refreshStateLoading = MutableLiveData<Boolean>()
    val snackbarMessage = MutableLiveData<String>()

    init {
        val factory: DataSource.Factory<Int, Image> = imagesDao.getAll()
        val config = PagedList.Config.Builder().apply {
            setEnablePlaceholders(false)
            setInitialLoadSizeHint(20)
            setPageSize(PAGE_SIZE)
        }.build()
        images = LivePagedListBuilder(factory, config)
            .setBoundaryCallback(ImagesBoundaryCallback(imagesDao))
            .build()
    }

    fun fetchImages() {
        RemoteDataSource.getInstance().fetchImages {
            when (it) {
                is NetworkSuccess -> {
                    Log.d(TAG, "Ui data size = ${it.data.size}")
                    BACKGROUND.execute { imagesDao.insert(it.data) }
                    refreshStateLoading.value = false
                }
                is NetworkError -> {
                    Log.d(TAG, "Network error ${it.error.message}")
                    snackbarMessage.value = "Network error"
                    refreshStateLoading.value = false
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}