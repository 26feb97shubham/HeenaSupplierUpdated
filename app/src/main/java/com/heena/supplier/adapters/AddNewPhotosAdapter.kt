package com.heena.supplier.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.PhotoData
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.photos_post_items.view.*

class AddNewPhotosAdapter(
    private val context: Context, private val photosList: ArrayList<PhotoData>,
    private val service_status: String,
    private val onCLickAction: ClickInterface.OnRecyclerItemClick
) :
    RecyclerView.Adapter<AddNewPhotosAdapter.AddNewPhotosAdapterVH>() {
    inner class AddNewPhotosAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddNewPhotosAdapterVH {
        val view  = LayoutInflater.from(context).inflate(R.layout.photos_post_items, parent, false)
        return AddNewPhotosAdapterVH(view)
    }

    override fun onBindViewHolder(holder: AddNewPhotosAdapterVH, position: Int) {
        val requestOptions = RequestOptions().error(R.drawable.def_henna).centerCrop()

        if (service_status.equals("show")){
            holder.itemView.aciv_delete_photos.visibility = View.GONE
            holder.itemView.aciv_photos.setOnClickListener {
                onCLickAction.OnClickAction(position)
            }
        }else{
            holder.itemView.aciv_delete_photos.visibility = View.VISIBLE
        }


        Glide.with(context).load(photosList[position].path)
            .listener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.itemView.img_loader_photos.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.itemView.img_loader_photos.visibility = View.GONE
                    return false
                }

            }).apply(requestOptions).into(holder.itemView.aciv_photos)
        holder.itemView.aciv_delete_photos.setSafeOnClickListener {
            onCLickAction.OnClickAction(position)
        }
    }

    override fun getItemCount(): Int {
        return photosList.size
    }
}