package omar.example.com.wallofimages.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import omar.example.com.wallofimages.R
import omar.example.com.wallofimages.data.Image
import omar.example.com.wallofimages.data.local.ImagesDao
import omar.example.com.wallofimages.data.local.ImagesDatabase
import omar.example.com.wallofimages.data.remote.NetworkError
import omar.example.com.wallofimages.data.remote.NetworkSuccess
import omar.example.com.wallofimages.data.remote.RemoteDataSource
import omar.example.com.wallofimages.ui.adapter.ImageAdapter
import omar.example.com.wallofimages.util.BACKGROUND

class MainActivity : AppCompatActivity() {

    private lateinit var model: MainViewModel

    private val adapter = ImageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get ViewModel
        getViewModel()

        // Setup RecyclerView
        setupRecyclerView()
        setupSwipeToRefresh()
        setupSnackbar()

        // Make network request
        model.fetchImages()
    }

    private fun getViewModel() {
        val imagesDao = ImagesDatabase.getInstance(this).imagesDao()

        model = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return MainViewModel(imagesDao) as T
            }
        }).get(MainViewModel::class.java)
    }

    private fun setupRecyclerView() {
        mainRv.layoutManager = GridLayoutManager(this, 2)
        mainRv.adapter = adapter

        // set observable on images list
        model.images.observe(this, Observer<PagedList<Image>> {
            adapter.submitList(it)
        })
    }

    private fun setupSwipeToRefresh() {
        swipeToRefresh.setOnRefreshListener { model.fetchImages() }

        model.refreshStateLoading.observe(this, Observer<Boolean> {
            swipeToRefresh.isRefreshing = it
        })
    }

    private fun setupSnackbar() {
        model.snackbarMessage.observe(this, Observer<String> {
            Snackbar.make(mainRv, it, Snackbar.LENGTH_LONG).show()
        })
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
