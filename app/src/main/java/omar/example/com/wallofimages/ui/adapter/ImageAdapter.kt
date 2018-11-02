package omar.example.com.wallofimages.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_image.view.*
import omar.example.com.resourceloaderlibrary.ResourceLoadError
import omar.example.com.resourceloaderlibrary.ResourceLoadSuccess
import omar.example.com.resourceloaderlibrary.ResourceLoader
import omar.example.com.wallofimages.R
import omar.example.com.wallofimages.data.Image
import java.util.*

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private var data: List<Image> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image, parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) = holder.bind(data[position])

    override fun onViewRecycled(holder: ImageViewHolder) {
        super.onViewRecycled(holder)
        holder.itemView.imageImg.setImageBitmap(null)
    }

    fun swapData(data: List<Image>) {
        this.data = data
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.cancelLoadBtn.setOnClickListener(this)
        }

        fun bind(item: Image) = with(itemView) {
            imageImg.setBackgroundColor(Color.parseColor(item.color))
            val id = ResourceLoader.load(item.urls.regular) {
                when (it) {
                    is ResourceLoadSuccess -> {
                        if (imageImg != null) {
                            loadingView.visibility = View.GONE
                            imageImg.setImageBitmap(it.data)
                        }
                    }
                    is ResourceLoadError -> {
                        loadingView.visibility = View.GONE
                        Log.d("ImageAdapter", "UI failed fetching resource ${it.error.message}")
                    }
                }
            }
            item.fetchId = id
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val image = data[position]
                Log.d("ImageAdapter", "[cancelLoadBtn] image of id ${image.id}")
                val fetchId = image.fetchId
                if (fetchId != null) {
                    ResourceLoader.cancelLoad(fetchId)
                } else {
                    Log.d("ImageAdapter", "[cancelLoadBtn] fetchId is Null")
                }
            }
        }
    }
}