package omar.example.com.wallofimages.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import omar.example.com.wallofimages.R
import omar.example.com.wallofimages.data.local.ImagesDao
import omar.example.com.wallofimages.data.local.ImagesDatabase
import omar.example.com.wallofimages.data.remote.NetworkError
import omar.example.com.wallofimages.data.remote.NetworkSuccess
import omar.example.com.wallofimages.data.remote.RemoteDataSource
import omar.example.com.wallofimages.ui.adapter.ImageAdapter
import omar.example.com.wallofimages.util.BACKGROUND

class MainActivity : AppCompatActivity() {

    private lateinit var imagesDao: ImagesDao
    private val adapter = ImageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup RecyclerView
        setupRecyclerView()
        setupSwipeToRefresh()

        // Get images dao
        val database = ImagesDatabase.getInstance(this)
        imagesDao = database.imagesDao()

        // Make network request
        fetchImages()
    }

    private fun fetchImages() {
        RemoteDataSource.getInstance().fetchImages {
            when (it) {
                is NetworkSuccess -> {
                    Log.d(TAG, "Ui data size = ${it.data.size}")
                    BACKGROUND.execute {
                        imagesDao.insert(it.data)
                        val data = imagesDao.getAll()
                        Log.d(TAG, "data insertion result ${data.size}")
                        Handler(Looper.getMainLooper()).post {
                            adapter.swapData(data)
                            swipeToRefresh.isRefreshing = false
                        }
                    }
                }
                is NetworkError -> {
                    Snackbar.make(mainRv, it.error.message ?: "Network error", Snackbar.LENGTH_LONG).show()
                    swipeToRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun setupRecyclerView() {
        mainRv.layoutManager = GridLayoutManager(this, 2)
        mainRv.adapter = adapter
    }

    private fun setupSwipeToRefresh() {
        swipeToRefresh.setOnRefreshListener { fetchImages() }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
