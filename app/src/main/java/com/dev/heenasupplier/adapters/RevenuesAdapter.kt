package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.R
import com.dev.heenasupplier.models.TransactionItem
import kotlinx.android.synthetic.main.revenues_listing.view.*

class RevenuesAdapter(private val context: Context,
                      private val transactionsList: ArrayList<TransactionItem>) : RecyclerView.Adapter<RevenuesAdapter.RevenuesAdapterVH>() {
    inner class RevenuesAdapterVH(val revenuesItemView : View) : RecyclerView.ViewHolder(revenuesItemView){
        fun bind(transactionItem: TransactionItem) {
            val bookingId = "BOOK#" + transactionItem.booking_id
            val servicePrice = "AED " + transactionItem.amount
            revenuesItemView.tv_booking_id.text = bookingId
            revenuesItemView.tv_booking_date_time.text = transactionItem.created_at
            revenuesItemView.tv_service_name.text = transactionItem.service!!.name
            revenuesItemView.tv_service_price.text = servicePrice
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevenuesAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.revenues_listing, parent, false)
        return RevenuesAdapterVH(view)
    }

    override fun onBindViewHolder(holder: RevenuesAdapterVH, position: Int) {
        val transactionItem = transactionsList[position]
        holder.bind(transactionItem)
    }

    override fun getItemCount(): Int {
       return transactionsList.size
    }
}