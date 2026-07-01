package com.lumina.app.ui.flashcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lumina.app.data.model.Vocabulary
import com.lumina.app.databinding.ItemFlashcardBinding

class FlashcardAdapter(
    private val onAudioClick: (Vocabulary) -> Unit,
    private val onFavoriteClick: (Vocabulary) -> Unit,
    private val onCardFlipped: (Boolean) -> Unit
) : ListAdapter<Vocabulary, FlashcardAdapter.FlashcardViewHolder>(VocabularyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val binding = ItemFlashcardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FlashcardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FlashcardViewHolder(private val binding: ItemFlashcardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isFront = true

        fun bind(vocabulary: Vocabulary) {
            binding.apply {
                tvWord.text = vocabulary.word
                tvIpa.text = vocabulary.ipa ?: ""
                tvWordType.text = vocabulary.wordType?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""
                tvMeaning.text = vocabulary.meaning
                tvExample.text = vocabulary.exampleSentence ?: ""

                // Favorite icon color
                btnFavorite.setColorFilter(
                    if (vocabulary.isFavorite) 0xFFF59E0B.toInt() else 0xFFD1D5DB.toInt()
                )

                // Reset card state without animation
                isFront = true
                layoutFront.visibility = View.VISIBLE
                layoutBack.visibility = View.GONE
                
                // Đảm bảo các mặt ở đúng trạng thái góc xoay
                layoutFront.alpha = 1.0f
                layoutFront.rotationY = 0f
                layoutBack.alpha = 1.0f
                layoutBack.rotationY = 180f
                
                cardContainer.rotationY = 0f
                onCardFlipped(false) // Reset nút SRS về ẩn

                cardContainer.setOnClickListener {
                    flipCard()
                }

                btnAudio.setOnClickListener {
                    onAudioClick(vocabulary)
                }

                btnFavorite.setOnClickListener {
                    onFavoriteClick(vocabulary)
                }
            }
        }

        private fun flipCard() {
            val root = binding.cardContainer
            val distance = 16000
            val scale = root.resources.displayMetrics.density * distance
            root.cameraDistance = scale

            val targetRotation = if (isFront) 180f else 0f
            
            root.animate()
                .rotationY(targetRotation)
                .setDuration(500)
                .setUpdateListener { animation ->
                    // Đổi mặt ở góc 90 độ để tránh hiện tượng chữ bị ngược
                    if (animation.animatedFraction >= 0.5f) {
                        if (isFront) {
                            binding.layoutFront.visibility = View.GONE
                            binding.layoutBack.visibility = View.VISIBLE
                        } else {
                            binding.layoutBack.visibility = View.GONE
                            binding.layoutFront.visibility = View.VISIBLE
                        }
                    }
                }
                .withEndAction {
                    isFront = !isFront
                    onCardFlipped(!isFront)
                }
                .start()
        }
    }

    class VocabularyDiffCallback : DiffUtil.ItemCallback<Vocabulary>() {
        override fun areItemsTheSame(oldItem: Vocabulary, newItem: Vocabulary): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Vocabulary, newItem: Vocabulary): Boolean {
            return oldItem == newItem
        }
    }
}
