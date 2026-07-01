package com.lumina.app.ui.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lumina.app.data.model.Vocabulary
import com.lumina.app.databinding.ItemWrongWordBinding

class WrongWordsAdapter(
    private val words: List<Vocabulary>,
    private val onAudioClick: (Vocabulary) -> Unit
) : RecyclerView.Adapter<WrongWordsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemWrongWordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWrongWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vocab = words[position]
        with(holder.binding) {
            tvWord.text = vocab.word
            tvMeaning.text = vocab.meaning
            btnAudio.setOnClickListener { onAudioClick(vocab) }
        }
    }

    override fun getItemCount() = words.size
}
