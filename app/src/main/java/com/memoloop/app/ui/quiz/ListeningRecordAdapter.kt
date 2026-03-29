package com.memoloop.app.ui.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.memoloop.app.R
import com.memoloop.app.data.model.ListeningResult
import com.memoloop.app.databinding.ItemQuizRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListeningRecordAdapter : ListAdapter<ListeningResult, ListeningRecordAdapter.ViewHolder>(DIFF) {

    private var displayFmt: SimpleDateFormat? = null

    inner class ViewHolder(private val binding: ItemQuizRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: ListeningResult) {
            val ctx = binding.root.context
            if (displayFmt == null) {
                displayFmt = SimpleDateFormat(
                    ctx.getString(R.string.record_date_fmt), Locale.getDefault()
                )
            }
            binding.tvQuizDate.text = displayFmt!!.format(Date(result.dateMillis))
            binding.tvQuizDetail.text = ctx.getString(
                R.string.quiz_record_detail_fmt, result.correctCount, result.totalCount
            )
            if (result.passed) {
                binding.tvQuizIcon.text = ctx.getString(R.string.icon_check)
                binding.tvQuizPoints.text = ctx.getString(
                    R.string.quiz_points_earned_fmt, result.pointsEarned
                )
            } else {
                binding.tvQuizIcon.text = ctx.getString(R.string.icon_cross)
                binding.tvQuizPoints.text = ""
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuizRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ListeningResult>() {
            override fun areItemsTheSame(a: ListeningResult, b: ListeningResult) = a.id == b.id
            override fun areContentsTheSame(a: ListeningResult, b: ListeningResult) = a == b
        }
    }
}
