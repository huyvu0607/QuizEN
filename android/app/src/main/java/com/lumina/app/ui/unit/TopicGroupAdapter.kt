package com.lumina.app.ui.unit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.lumina.app.R
import com.lumina.app.databinding.ItemTopicGroupBinding

class TopicGroupAdapter(
    private var items: List<TopicGroupUiItem>,
    private val onWordClick: (Long) -> Unit,
    private val onWordLongClick: (Long, String) -> Unit,
    private val onGroupMenuClick: (android.view.View, TopicGroupUiItem) -> Unit
) : RecyclerView.Adapter<TopicGroupAdapter.TopicViewHolder>() {

    fun updateData(newItems: List<TopicGroupUiItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TopicViewHolder(val binding: ItemTopicGroupBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemTopicGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvGroupName.text = item.name
            tvGroupDescription.text = item.description ?: "Từ vựng thuộc chủ đề ${item.name}"
            tvWordCount.text = "${item.words.size} từ · ${item.masteryRate}%"

            flexWords.removeAllViews()
            item.words.forEach { vocab ->
                val chip = LayoutInflater.from(root.context).inflate(
                    R.layout.layout_word_chip, flexWords, false
                ) as Chip
                chip.text = vocab.word
                chip.setOnClickListener { onWordClick(vocab.id) }
                chip.setOnLongClickListener {
                    onWordLongClick(vocab.id, vocab.word)
                    true
                }
                flexWords.addView(chip)
            }

            btnGroupMenu.setOnClickListener {
                onGroupMenuClick(it, item)
            }
        }
    }

    override fun getItemCount() = items.size
}
