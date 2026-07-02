package com.lumina.app.ui.unit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lumina.app.R
import com.lumina.app.databinding.ItemUnitCardBinding

class UnitAdapter(
    private var items: List<UnitItem>,
    private val onItemClick: (UnitItem) -> Unit,
    private val onEditClick: ((UnitItem) -> Unit)? = null,
    private val onDeleteClick: ((UnitItem) -> Unit)? = null
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    private var isEditMode: Boolean = false

    fun updateData(newItems: List<UnitItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        notifyDataSetChanged()
    }

    class UnitViewHolder(val binding: ItemUnitCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ItemUnitCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvUnitTitle.text = item.title
            tvUnitSubtitle.text = item.subtitle

            // Set Icon vs Number
            if (item.icon != null) {
                ivUnitIcon.visibility = View.VISIBLE
                tvUnitNumber.visibility = View.GONE
                ivUnitIcon.setImageResource(getIconRes(item.icon))
            } else {
                ivUnitIcon.visibility = View.GONE
                tvUnitNumber.visibility = View.VISIBLE
                tvUnitNumber.text = item.id.toString()
            }

            root.setOnClickListener {
                if (isEditMode) return@setOnClickListener
                if (item.status != UnitStatus.LOCKED) {
                    onItemClick(item)
                }
            }

            ivEditUnit.setOnClickListener { onEditClick?.invoke(item) }
            ivDeleteUnit.setOnClickListener { onDeleteClick?.invoke(item) }

            if (isEditMode) {
                layoutEditOptions.visibility = View.VISIBLE
                ivStatus.visibility = View.GONE
                pbUnitStatus.visibility = View.GONE
                ivLock.visibility = View.GONE
            } else {
                layoutEditOptions.visibility = View.GONE
                // Existing status logic below will handle visibility of status icons
            }

            when (item.status) {
                UnitStatus.COMPLETED -> {
                    if (!isEditMode) ivStatus.visibility = View.VISIBLE
                    pbUnitStatus.visibility = View.GONE
                    ivLock.visibility = View.GONE
                    cardUnit.strokeWidth = 0
                    layoutUnitIcon.setBackgroundResource(R.drawable.bg_icon_circle_primary)
                    tvUnitNumber.setTextColor(ContextCompat.getColor(root.context, R.color.white))
                    tvUnitTitle.setTextColor(ContextCompat.getColor(root.context, R.color.text_primary))
                }
                UnitStatus.IN_PROGRESS -> {
                    ivStatus.visibility = View.GONE
                    pbUnitStatus.visibility = View.GONE
                    ivLock.visibility = View.GONE
                    cardUnit.setStrokeColor(ContextCompat.getColorStateList(root.context, R.color.colorPrimary))
                    cardUnit.strokeWidth = 4
                    layoutUnitIcon.setBackgroundResource(R.drawable.bg_icon_circle_primary)
                    tvUnitNumber.setTextColor(ContextCompat.getColor(root.context, R.color.white))
                    tvUnitTitle.setTextColor(ContextCompat.getColor(root.context, R.color.colorPrimary))
                }
                UnitStatus.LOCKED -> {
                    ivStatus.visibility = View.GONE
                    pbUnitStatus.visibility = View.GONE
                    if (!isEditMode) ivLock.visibility = View.VISIBLE
                    cardUnit.strokeWidth = 0
                    layoutUnitIcon.setBackgroundResource(R.drawable.bg_icon_circle_gray)
                    tvUnitNumber.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary))
                    tvUnitTitle.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary))
                }
            }
        }
    }

    override fun getItemCount() = items.size

    private fun getIconRes(iconKey: String?): Int {
        return when (iconKey) {
            "book" -> R.drawable.ic_book
            "star" -> R.drawable.ic_star
            "trophy" -> R.drawable.ic_trophy
            "brain" -> R.drawable.ic_brain
            "globe" -> R.drawable.ic_globe
            "briefcase" -> R.drawable.ic_briefcase
            "plane" -> R.drawable.ic_plane
            "flame" -> R.drawable.ic_flame
            "sparkle" -> R.drawable.ic_sparkle
            else -> R.drawable.ic_book
        }
    }
}
