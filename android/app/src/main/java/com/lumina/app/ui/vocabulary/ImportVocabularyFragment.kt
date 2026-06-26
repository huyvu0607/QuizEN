package com.lumina.app.ui.vocabulary

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lumina.app.data.model.Vocabulary
import com.lumina.app.data.model.WordType
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentImportVocabularyBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.course.CourseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportVocabularyFragment : Fragment() {
    private var _binding: FragmentImportVocabularyBinding? = null
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
    private var parsedVocabs = mutableListOf<Vocabulary>()

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { handleFilePicked(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportVocabularyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lessonId = arguments?.getLong("lesson_id") ?: -1

        setupRecyclerView()
        setupListeners()
        
        // Initial UI state
        binding.cardPreview.visibility = View.GONE
        binding.cardConfirmImport.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        binding.rvCsvPreview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CsvPreviewAdapter(emptyList())
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnChangeFile.setOnClickListener {
            pickFileLauncher.launch(arrayOf("text/*"))
        }

        binding.btnConfirmImport.setOnClickListener {
            importToDatabase()
        }
        
        // Pick file automatically on start if not already picked
        if (parsedVocabs.isEmpty()) {
            pickFileLauncher.launch(arrayOf("text/*"))
        }
    }

    private fun handleFilePicked(uri: Uri) {
        val fileName = getFileName(uri) ?: "unknown_file.csv"
        binding.tvFileName.text = fileName
        
        lifecycleScope.launch {
            try {
                val vocabs = parseCsv(uri)
                if (vocabs.isNotEmpty()) {
                    parsedVocabs.clear()
                    parsedVocabs.addAll(vocabs)
                    
                    binding.cardPreview.visibility = View.VISIBLE
                    binding.cardConfirmImport.visibility = View.VISIBLE
                    
                    (binding.rvCsvPreview.adapter as? CsvPreviewAdapter)?.updateData(parsedVocabs.take(10))
                    
                    binding.tvRowCountBadge.text = "${vocabs.size} Rows Detected"
                    binding.tvShowingStats.text = "Showing ${minOf(10, vocabs.size)} of ${vocabs.size} rows"
                    binding.tvImportSummary.text = "You are about to add ${vocabs.size} new words to your vocabulary collection."
                } else {
                    Toast.makeText(requireContext(), "File CSV trống hoặc sai định dạng", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi khi đọc file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun parseCsv(uri: Uri): List<Vocabulary> = withContext(Dispatchers.IO) {
        val vocabs = mutableListOf<Vocabulary>()
        requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine() // Skip header
                while (reader.readLine().also { line = it } != null) {
                    val tokens = splitCsvLine(line!!)
                    if (tokens.size >= 2) {
                        val word = tokens[0].trim()
                        val meaning = tokens[1].trim()
                        val example = if (tokens.size > 2) tokens[2].trim() else ""
                        val ipa = if (tokens.size > 3) tokens[3].trim() else ""
                        val typeStr = if (tokens.size > 4) tokens[4].trim().uppercase() else "OTHER"
                        
                        val wordType = try { WordType.valueOf(typeStr) } catch (e: Exception) { WordType.OTHER }

                        vocabs.add(Vocabulary(
                            id = 0,
                            lessonId = lessonId,
                            word = word,
                            meaning = meaning,
                            exampleSentence = example,
                            ipa = ipa,
                            wordType = wordType,
                            createdAt = System.currentTimeMillis()
                        ))
                    }
                }
            }
        }
        vocabs
    }

    // A simple CSV splitter that handles quoted strings with commas
    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var currentToken = StringBuilder()
        var inQuotes = false
        for (char in line) {
            when {
                char == '\"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(currentToken.toString())
                    currentToken = StringBuilder()
                }
                else -> currentToken.append(char)
            }
        }
        result.add(currentToken.toString())
        return result
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) result = result?.substring(cut + 1)
        }
        return result
    }

    private fun importToDatabase() {
        if (parsedVocabs.isEmpty()) return
        
        lifecycleScope.launch {
            viewModel.bulkInsertVocabulary(parsedVocabs)
            Toast.makeText(requireContext(), "Đã import ${parsedVocabs.size} từ vựng thành công!", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
