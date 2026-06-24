package com.lumina.app.ui.courses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lumina.app.R
import com.lumina.app.databinding.ItemCourseCardBinding

data class CourseUiItem(
    val id: Long,
    val title: String,
    val level: String,
    val progress: Int,
    val words: String,
    val iconRes: Int,
    val isCompleted: Boolean = false
)

class CourseAdapter(
    private val items: List<CourseUiItem>,
    private val onItemClick: (CourseUiItem) -> Unit,
    private val onMoreClick: (View, CourseUiItem) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(val binding: ItemCourseCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvCourseName.text = item.title
            tvLevelBadge.text = item.level
            tvCourseWords.text = item.words
            ivCourseIcon.setImageResource(item.iconRes)
            
            pbCourseProgress.progress = item.progress
            
            if (item.isCompleted) {
                pbCourseProgress.progressDrawable = ContextCompat.getDrawable(root.context, R.drawable.progress_drawable_green)
                tvLevelBadge.backgroundTintList = ContextCompat.getColorStateList(root.context, R.color.success_green_bg)
                tvLevelBadge.setTextColor(ContextCompat.getColor(root.context, R.color.success_green))
            } else {
                pbCourseProgress.progressDrawable = ContextCompat.getDrawable(root.context, R.drawable.progress_drawable_horizontal)
                tvLevelBadge.backgroundTintList = ContextCompat.getColorStateList(root.context, R.color.reminder_bg)
                tvLevelBadge.setTextColor(ContextCompat.getColor(root.context, R.color.colorPrimary))
            }

            ivMore.setOnClickListener { onMoreClick(it, item) }
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size
}
