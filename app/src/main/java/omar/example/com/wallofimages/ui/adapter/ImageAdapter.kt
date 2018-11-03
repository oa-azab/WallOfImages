package omar.example.com.wallofimages.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_image.view.*
import omar.example.com.resourceloaderlibrary.ResourceLoadError
import omar.example.com.resourceloaderlibrary.ResourceLoadSuccess
import omar.example.com.resourceloaderlibrary.ResourceLoader
import omar.example.com.resourceloaderlibrary.util.toBitmap
import omar.example.com.wallofimages.R
import omar.example.com.wallofimages.data.Image

class ImageAdapter : PagedListAdapter<Image, ImageAdapter.ImageViewHolder>(IMAGE_COMPARATOR) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) = holder.bind(getItem(position)!!)

    override fun onViewRecycled(holder: ImageViewHolder) {
        super.onViewRecycled(holder)
        holder.itemView.imageImg.setImageBitmap(null)
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.cancelLoadBtn.setOnClickListener(this)
            itemView.retryBtn.setOnClickListener(this)
        }

        fun bind(item: Image) = with(itemView) {
            imageImg.setBackgroundColor(Color.parseColor(item.color))
            loadingView.visibility = View.VISIBLE
            val id = ResourceLoader.load(item.urls.regular) {
                when (it) {
                    is ResourceLoadSuccess -> {
                        if (imageImg != null) {
                            loadingView.visibility = View.GONE
                            imageImg.setImageBitmap(it.data.toBitmap())
                        }
                    }
                    is ResourceLoadError -> {
                        loadingView.visibility = View.GONE
                        retryBtn.visibility = View.VISIBLE
                        Log.d("ImageAdapter", "UI failed fetching resource ${it.error.message}")
                    }
                }
            }
            item.fetchId = id
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val image = getItem(position)!!
                when (view?.id) {
                    R.id.cancelLoadBtn -> cancel(image)
                    R.id.retryBtn -> {
                        itemView.retryBtn.visibility = View.GONE
                        bind(image)
                    }
                }
            }
        }

        private fun cancel(image: Image) {
            Log.d("ImageAdapter", "[cancel] image of id ${image.id}")
            val fetchId = image.fetchId
            if (fetchId != null) {
                ResourceLoader.cancelLoad(fetchId)
            } else {
                Log.d("ImageAdapter", "[cancel] fetchId is Null")
            }
        }
    }

    companion object {
        val IMAGE_COMPARATOR = object : DiffUtil.ItemCallback<Image>() {
            override fun areItemsTheSame(p0: Image, p1: Image): Boolean {
                return p0.id == p1.id
            }

            override fun areContentsTheSame(p0: Image, p1: Image): Boolean {
                return p0 == p1
            }
        }
    }
}