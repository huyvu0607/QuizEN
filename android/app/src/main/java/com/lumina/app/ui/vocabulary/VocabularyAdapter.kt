package com.lumina.app.ui.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lumina.app.R
import com.lumina.app.databinding.ItemVocabularyCardBinding

class VocabularyAdapter(
    private var items: List<VocabularyUiItem>,
    private val onAudioClick: (VocabularyUiItem) -> Unit,
    private val onFavoriteClick: (VocabularyUiItem) -> Unit,
    private val onItemClick: (VocabularyUiItem) -> Unit
) : RecyclerView.Adapter<VocabularyAdapter.VocabViewHolder>() {

    fun updateData(newItems: List<VocabularyUiItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VocabViewHolder(val binding: ItemVocabularyCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabViewHolder {
        val binding = ItemVocabularyCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VocabViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VocabViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvWord.text = item.word
            tvIpa.text = item.ipa
            tvMeaning.text = item.meaning
            tvWordType.text = item.wordType ?: "Word"
            tvExample.text = item.exampleSentence

            // Status Dot Color
            val statusColor = when (item.status) {
                VocabularyStatus.LEARNED -> "#22C55E"      // Green
                VocabularyStatus.NEEDS_REVIEW -> "#EF4444" // Red
                VocabularyStatus.NOT_STARTED -> "#D1D5DB"  // Gray
            }
            viewStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(statusColor)
            )

            // Favorite Icon
            ivFavorite.setImageResource(
                if (item.isFavorite) R.drawable.ic_star else R.drawable.ic_star // Should use filled vs outline if available
            )
            ivFavorite.setColorFilter(
                ContextCompat.getColor(root.context, if (item.isFavorite) R.color.streak_orange else R.color.text_secondary)
            )

            ivAudio.setOnClickListener { onAudioClick(item) }
            ivFavorite.setOnClickListener { onFavoriteClick(item) }
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size
}
