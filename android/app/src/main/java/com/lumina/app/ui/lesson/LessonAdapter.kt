package com.lumina.app.ui.lesson

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lumina.app.R
import com.lumina.app.databinding.ItemLessonCardBinding

class LessonAdapter(
    private var items: List<LessonUiItem>,
    private val onItemClick: (LessonUiItem) -> Unit,
    private val onMoreClick: (View, LessonUiItem) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    fun updateData(newItems: List<LessonUiItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class LessonViewHolder(val binding: ItemLessonCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvLessonTitle.text = item.title
            tvLessonSubtitle.text = item.subtitle
            
            // Icon
            item.iconRes?.let { ivLessonIcon.setImageResource(it) }

            // Status Badge
            when (item.status) {
                LessonStatus.COMPLETED -> {
                    tvStatusBadge.text = "Đã thuộc"
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_gray)
                    tvStatusBadge.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary))
                    pbLessonProgress.visibility = View.GONE
                    cardLesson.strokeWidth = 0
                }
                LessonStatus.LEARNING -> {
                    tvStatusBadge.text = "Đang học"
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green)
                    tvStatusBadge.setTextColor(ContextCompat.getColor(root.context, R.color.text_primary))
                    pbLessonProgress.visibility = View.VISIBLE
                    pbLessonProgress.progress = item.progress
                    cardLesson.setStrokeColor(ContextCompat.getColorStateList(root.context, R.color.colorPrimary))
                    cardLesson.strokeWidth = 4 // Viền xanh khi đang học
                }
                LessonStatus.NOT_STARTED -> {
                    tvStatusBadge.text = "Chưa học"
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_gray)
                    tvStatusBadge.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary))
                    pbLessonProgress.visibility = View.GONE
                    cardLesson.strokeWidth = 0
                }
            }

            // Buttons
            btnFlashcard.setOnClickListener { onItemClick(item) }
            btnQuiz.setOnClickListener { onItemClick(item) }
            
            root.setOnClickListener { onItemClick(item) }
            ivMore.setOnClickListener { onMoreClick(it, item) }
        }
    }

    override fun getItemCount() = items.size
}
