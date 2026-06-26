package com.lumina.app.ui.vocabulary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lumina.app.R
import com.lumina.app.data.model.WordType
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentAddVocabularyBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.course.CourseViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class AddVocabularyFragment : Fragment() {
    private var _binding: FragmentAddVocabularyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val courseRepository = CourseRepository(
            database.courseDao(),
            database.unitDao(),
            database.lessonDao(),
            database.vocabularyDao()
        )
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(courseRepository = courseRepository, sessionManager = sessionManager)
    }

    private var lessonId: Long = -1
    private var vocabId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddVocabularyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        lessonId = arguments?.getLong("lesson_id") ?: -1
        vocabId = arguments?.getLong("vocabulary_id") ?: -1

        setupWordTypeDropdown()
        setupListeners()

        if (vocabId != -1L) {
            loadVocabData()
            binding.tvTitle.text = "Chỉnh sửa từ vựng"
            binding.btnSaveAndNext.visibility = View.GONE
        }
    }

    private fun setupWordTypeDropdown() {
        val types = WordType.values().map { type ->
            type.name.lowercase().replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.actvWordType.setAdapter(adapter)
    }

    private fun loadVocabData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val vocab = viewModel.getVocabularyById(vocabId)
            vocab?.let {
                binding.etWord.setText(it.word)
                binding.etMeaning.setText(it.meaning)
                binding.etExample.setText(it.exampleSentence)
                binding.etIpa.setText(it.ipa)
                val typeFormatted = it.wordType?.name?.lowercase()?.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
                binding.actvWordType.setText(typeFormatted, false)
            }
        }
    }

    private fun setupListeners() {
        binding.ivClose.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            saveVocab(true)
        }

        binding.btnSaveAndNext.setOnClickListener {
            saveVocab(false)
        }

        binding.layoutAddImage.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng thêm ảnh sẽ sớm ra mắt!", Toast.LENGTH_SHORT).show()
        }

        binding.btnImportCsv.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("lesson_id", lessonId)
            }
            findNavController().navigate(R.id.importVocabularyFragment, bundle)
        }
    }

    private fun saveVocab(finish: Boolean) {
        val word = binding.etWord.text.toString().trim()
        val meaning = binding.etMeaning.text.toString().trim()
        val example = binding.etExample.text.toString().trim()
        val ipa = binding.etIpa.text.toString().trim()
        val typeStr = binding.actvWordType.text.toString().uppercase()
        val type = try { WordType.valueOf(typeStr) } catch (e: Exception) { null }

        if (word.isEmpty() || meaning.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập từ và nghĩa!", Toast.LENGTH_SHORT).show()
            return
        }

        // According to section 2.4, Example sentence is mandatory
        if (example.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập câu ví dụ để hỗ trợ Quiz!", Toast.LENGTH_SHORT).show()
            return
        }

        if (vocabId != -1L) {
            viewModel.updateVocabulary(vocabId, lessonId, word, meaning, example, ipa, type)
            Toast.makeText(requireContext(), "Đã cập nhật từ vựng!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addVocabulary(lessonId, word, meaning, example, ipa, type)
            Toast.makeText(requireContext(), "Đã thêm từ vựng mới!", Toast.LENGTH_SHORT).show()
        }

        if (finish) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        } else {
            clearFields()
        }
    }

    private fun clearFields() {
        binding.etWord.text?.clear()
        binding.etMeaning.text?.clear()
        binding.etExample.text?.clear()
        binding.etIpa.text?.clear()
        binding.actvWordType.text?.clear()
        binding.etWord.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
