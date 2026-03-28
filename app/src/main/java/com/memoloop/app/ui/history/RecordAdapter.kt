package com.memoloop.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.memoloop.app.R
import com.memoloop.app.data.model.ReviewSession
import com.memoloop.app.databinding.ItemRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordAdapter : ListAdapter<ReviewSession, RecordAdapter.ViewHolder>(DIFF) {

    private var displayFmt: SimpleDateFormat? = null

    inner class ViewHolder(private val binding: ItemRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: ReviewSession) {
            val ctx = binding.root.context
            if (displayFmt == null) {
                displayFmt = SimpleDateFormat(ctx.getString(R.string.record_date_fmt), Locale.getDefault())
            }
            binding.tvRecordDate.text = displayFmt!!.format(Date(session.dateMillis))
            val min = session.durationSeconds / 60
            val sec = session.durationSeconds % 60
            binding.tvRecordTime.text = ctx.getString(R.string.record_time_fmt, min, sec)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ReviewSession>() {
            override fun areItemsTheSame(a: ReviewSession, b: ReviewSession) = a.id == b.id
            override fun areContentsTheSame(a: ReviewSession, b: ReviewSession) = a == b
        }
    }
}
