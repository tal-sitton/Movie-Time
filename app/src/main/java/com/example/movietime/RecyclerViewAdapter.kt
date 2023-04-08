package com.example.movietime

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RecyclerViewAdapter internal constructor(context: Context, data: List<Recyclable>) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    private val mData: List<Recyclable>
    private val mContext: Context
    private val mInflater: LayoutInflater

    // data is passed into the constructor
    init {
        mInflater = LayoutInflater.from(context)
        mData = data
        mContext = context
    }

    // inflates the cell layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == TITLE_TYPE) {
            val view: View = mInflater.inflate(R.layout.date_title, parent, false)
            return ViewHolder(view)
        }

        if (viewType == SPACE_TYPE) {
            val view: View = mInflater.inflate(R.layout.date_title, parent, false)
            return ViewHolder(view)
        }

        val view: View = mInflater.inflate(R.layout.all_screenings, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        if (mData[position] is Title)
            return TITLE_TYPE
        if (mData[position] is RecyclableSpace)
            return SPACE_TYPE
        return SCREENING_TYPE
    }

    // binds the data to the TextView in each cell
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.myTextView.text = mData[position].createText()
        if (getItemViewType(position) != SCREENING_TYPE) {
            holder.myTextView.isClickable = false
            holder.myTextView.minHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            holder.myTextView.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mData[position].url))
                mContext.startActivity(browserIntent)
            }
        }
    }

    // total number of cells
    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var myTextView: TextView

        init {
            myTextView = itemView.findViewById(R.id.recyclable)
            myTextView.minHeight =
                mContext.resources.getDimensionPixelSize(R.dimen.screening_height)
            myTextView.width =
                mContext.resources.getDimensionPixelSize(R.dimen.screening_width)
        }

    }

    companion object {
        const val SCREENING_TYPE = 0
        const val TITLE_TYPE = 1
        const val SPACE_TYPE = 2
    }
}
