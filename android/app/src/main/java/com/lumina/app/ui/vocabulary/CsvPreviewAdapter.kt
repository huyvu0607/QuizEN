package com.lumina.app.ui.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lumina.app.data.model.Vocabulary
import com.lumina.app.databinding.ItemCsvRowBinding

class CsvPreviewAdapter(
    private var items: List<Vocabulary>
) : RecyclerView.Adapter<CsvPreviewAdapter.CsvViewHolder>() {

    fun updateData(newItems: List<Vocabulary>) {
        items = newItems
        notifyDataSetChanged()
    }

    class CsvViewHolder(val binding: ItemCsvRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CsvViewHolder {
        val binding = ItemCsvRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CsvViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CsvViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvIndex.text = (position + 1).toString()
            tvWord.text = item.word
            tvMeaning.text = item.meaning
            tvPos.text = item.wordType?.name?.lowercase() ?: "word"
        }
    }

    override fun getItemCount() = items.size
}
