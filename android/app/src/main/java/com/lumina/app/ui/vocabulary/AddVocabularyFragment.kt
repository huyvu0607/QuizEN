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
import com.lumina.app.data.source.remote.DictionaryApiService
import com.lumina.app.databinding.FragmentAddVocabularyBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.course.CourseViewModel
import com.lumina.app.data.source.remote.ai.GeminiService
import com.lumina.app.utils.NetworkUtils
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class AddVocabularyFragment : Fragment() {
    private var _binding: FragmentAddVocabularyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = CourseRepository(
            courseDao = database.courseDao(),
            unitDao = database.unitDao(),
            lessonDao = database.lessonDao(),
            vocabularyDao = database.vocabularyDao(),
            topicGroupDao = database.topicGroupDao(),
            dictionaryApiService = DictionaryApiService.create(),
            geminiService = GeminiService(com.lumina.app.BuildConfig.GEMINI_API_KEY)
        )
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(courseRepository = repository, sessionManager = sessionManager)
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

        binding.tilWord.setEndIconOnClickListener {
            suggestVocabularyData()
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

    private fun suggestVocabularyData() {
        val word = binding.etWord.text.toString().trim()
        if (word.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập từ tiếng Anh trước", Toast.LENGTH_SHORT).show()
            return
        }

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Cần có kết nối mạng để sử dụng tính năng gợi ý AI.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            binding.tilWord.isEndIconVisible = false
            Toast.makeText(requireContext(), "AI đang lấy dữ liệu cho '$word'...", Toast.LENGTH_SHORT).show()
            
            try {
                val jsonResult = viewModel.suggestVocabularyWithAi(word)
                if (!jsonResult.isNullOrEmpty()) {
                    // Robust JSON extraction
                    val cleanJson = extractJsonContent(jsonResult)
                    val json = JSONObject(cleanJson)
                    
                    val meaning = json.optString("meaning")
                    val ipa = json.optString("ipa")
                    val example = json.optString("example")
                    val type = json.optString("type")

                    if (meaning.isNotEmpty()) binding.etMeaning.setText(meaning)
                    if (ipa.isNotEmpty()) binding.etIpa.setText(ipa)
                    if (example.isNotEmpty()) binding.etExample.setText(example)
                    
                    if (type.isNotEmpty()) {
                        val typeFormatted = type.uppercase().let { typeUpper ->
                            try {
                                val enumValue = WordType.valueOf(typeUpper)
                                enumValue.name.lowercase().replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        typeFormatted?.let { binding.actvWordType.setText(it, false) }
                    }

                    Toast.makeText(requireContext(), "Đã tự động điền dữ liệu từ AI!", Toast.LENGTH_SHORT).show()
                } else {
                    fallbackToDictionary(word)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fallbackToDictionary(word)
            } finally {
                binding.tilWord.isEndIconVisible = true
            }
        }
    }

    private fun extractJsonContent(input: String): String {
        val start = input.indexOf('{')
        val end = input.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return input.substring(start, end + 1)
        }
        return input.trim().removePrefix("```json").removeSuffix("```").trim()
    }

    private suspend fun fallbackToDictionary(word: String) {
        try {
            val results = viewModel.fetchDictionaryData(word)
            if (!results.isNullOrEmpty()) {
                val entry = results[0]
                
                // Điền IPA nếu còn trống
                val ipa = entry.phonetic ?: entry.phonetics.firstOrNull { !it.text.isNullOrEmpty() }?.text
                if (binding.etIpa.text.isNullOrEmpty() && !ipa.isNullOrEmpty()) {
                    binding.etIpa.setText(ipa)
                }
                
                // Điền ví dụ nếu còn trống
                if (binding.etExample.text.isNullOrEmpty()) {
                    val firstExample = entry.meanings.flatMap { it.definitions }.firstOrNull { !it.example.isNullOrEmpty() }?.example
                    if (!firstExample.isNullOrEmpty()) binding.etExample.setText(firstExample)
                }
                
                Toast.makeText(requireContext(), "AI không phản hồi, đã lấy từ điển dự phòng (tiếng Anh)", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Không tìm thấy dữ liệu cho từ này", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi kết nối dữ liệu", Toast.LENGTH_SHORT).show()
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
