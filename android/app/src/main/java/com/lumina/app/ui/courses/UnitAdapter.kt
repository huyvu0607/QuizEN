package com.lumina.app.ui.courses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lumina.app.R
import com.lumina.app.databinding.ItemUnitCardBinding

class UnitAdapter(
    private val items: List<UnitItem>,
    private val onItemClick: (UnitItem) -> Unit
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

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
            tvUnitNumber.text = item.id.toString()
            tvUnitTitle.text = item.title
            tvUnitSubtitle.text = item.subtitle

            root.setOnClickListener { onItemClick(item) }

            when (item.status) {
                UnitStatus.COMPLETED -> {
                    ivStatus.visibility = View.VISIBLE
                    pbUnitStatus.visibility = View.GONE
                    ivLock.visibility = View.GONE
                    cardUnit.strokeWidth = 0
                    tvUnitNumber.setBackgroundResource(R.drawable.bg_icon_circle_primary)
                    tvUnitTitle.setTextColor(ContextCompat.getColor(root.context, R.color.text_primary))
                }
                UnitStatus.IN_PROGRESS -> {
                    ivStatus.visibility = View.GONE
                    pbUnitStatus.visibility = View.VISIBLE
                    ivLock.visibility = View.GONE
                    cardUnit.setStrokeColor(ContextCompat.getColorStateList(root.context, R.color.colorPrimary))
                    cardUnit.strokeWidth = 4
                    tvUnitNumber.setBackgroundResource(R.drawable.bg_icon_circle_primary)
                    tvUnitTitle.setTextColor(ContextCompat.getColor(root.context, R.color.colorPrimary))
                }
                UnitStatus.LOCKED -> {
                    ivStatus.visibility = View.GONE
                    pbUnitStatus.visibility = View.GONE
                    ivLock.visibility = View.VISIBLE
                    cardUnit.strokeWidth = 0
                    tvUnitNumber.setBackgroundResource(R.drawable.bg_icon_circle_gray)
                    tvUnitNumber.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary))
                    tvUnitTitle.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary))
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
