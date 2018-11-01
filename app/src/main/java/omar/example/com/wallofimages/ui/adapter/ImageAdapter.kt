package omar.example.com.wallofimages.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_image.view.*
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

    fun swapData(data: List<Image>) {
        this.data = data
        notifyDataSetChanged()
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Image) = with(itemView) {
            Picasso.get().load(item.urls.raw).resize(200, 400).centerCrop().into(imageImg)
        }
    }
}