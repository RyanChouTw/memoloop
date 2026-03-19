package com.memoloop.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.memoloop.app.data.model.ReviewSession
import com.memoloop.app.databinding.ItemRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordAdapter : ListAdapter<ReviewSession, RecordAdapter.ViewHolder>(DIFF) {

    private val displayFmt = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: ReviewSession) {
            binding.tvRecordDate.text = displayFmt.format(Date(session.dateMillis))
            val min = session.durationSeconds / 60
            val sec = session.durationSeconds % 60
            binding.tvRecordTime.text = String.format("花費時間：%02d:%02d", min, sec)
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
