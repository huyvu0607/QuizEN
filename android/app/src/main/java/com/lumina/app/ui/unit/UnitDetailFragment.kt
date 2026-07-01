package com.lumina.app.ui.unit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.PopupMenu
import com.lumina.app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentUnitDetailBinding
import androidx.navigation.fragment.findNavController
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.course.CourseViewModel
import com.lumina.app.ui.lesson.LessonAdapter
import com.lumina.app.ui.lesson.LessonUiItem
import com.lumina.app.data.source.remote.ai.GeminiService
import com.lumina.app.utils.NetworkUtils
import kotlinx.coroutines.launch

class UnitDetailFragment : Fragment() {
    private var _binding: FragmentUnitDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val courseRepository = CourseRepository(
            database.courseDao(),
            database.unitDao(),
            database.lessonDao(),
            database.vocabularyDao(),
            database.topicGroupDao(),
            geminiService = GeminiService(com.lumina.app.BuildConfig.GEMINI_API_KEY),
            firestoreSync = com.lumina.app.data.repository.FirestoreSyncManager()
        )
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(courseRepository = courseRepository, sessionManager = sessionManager)
    }

    private var unitId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnitDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        unitId = arguments?.getLong("unit_id") ?: -1
        val unitTitle = arguments?.getString("unit_title") ?: "Unit Details"
        binding.tvUnitTitle.text = unitTitle

        if (unitId != -1L) {
            viewModel.loadLessons(unitId)
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvLessons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = LessonAdapter(
                emptyList(),
                onItemClick = { lesson ->
                    val bundle = Bundle().apply {
                        putLong("lesson_id", lesson.id)
                    }
                    findNavController().navigate(R.id.vocabularyListFragment, bundle)
                },
                onFlashcardClick = { lesson ->
                    val bundle = Bundle().apply {
                        putLong("lesson_id", lesson.id)
                        putString("lesson_name", lesson.title)
                    }
                    findNavController().navigate(R.id.flashcardFragment, bundle)
                },
                onQuizClick = { lesson ->
                    val bundle = Bundle().apply {
                        putLong("lesson_id", lesson.id)
                        putString("lesson_name", lesson.title)
                        putString("unit_name", binding.tvUnitTitle.text.toString())
                    }
                    findNavController().navigate(R.id.quizSetupFragment, bundle)
                },
                onMoreClick = { view, lesson ->
                    showLessonOptionsMenu(view, lesson)
                }
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lessons.collect { lessons ->
                    (binding.rvLessons.adapter as? LessonAdapter)?.updateData(lessons)
                    
                    val totalVocab = lessons.sumOf { lesson ->
                        // The subtitle now contains real word count data in the format "$wordCount từ vựng"
                        // But it's safer to add a wordCount field to LessonUiItem too
                        lesson.wordCount
                    }
                    binding.tvUnitStats.text = "$totalVocab từ vựng"
                    binding.tvAiTitle.text = "AI đã phân nhóm $totalVocab từ thành ${lessons.size} chủ đề"
                }
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.cardAiBanner.setOnClickListener {
            analyzeUnitWithAi()
        }

        binding.fabAddLesson.setOnClickListener {
            if (unitId != -1L) {
                val bundle = Bundle().apply {
                    putLong("unit_id", unitId)
                    putString("unit_title", binding.tvUnitTitle.text.toString())
                }
                findNavController().navigate(R.id.addLessonFragment, bundle)
            }
        }
    }

    private fun showLessonOptionsMenu(view: View, lesson: LessonUiItem) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_lesson_options, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit_lesson -> {
                    // Mở dialog sửa bài học (tạm thời log hoặc dùng AlertDialog)
                    showEditLessonDialog(lesson)
                    true
                }
                R.id.action_delete_lesson -> {
                    showDeleteConfirmDialog(lesson)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showEditLessonDialog(lesson: LessonUiItem) {
        // Có thể dùng một BottomSheet mới hoặc đơn giản là AlertDialog để đổi tên
        val input = android.widget.EditText(requireContext()).apply {
            setText(lesson.title)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sửa tên bài học")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    viewModel.updateLesson(lesson.id, newTitle)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDeleteConfirmDialog(lesson: LessonUiItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa bài học")
            .setMessage("Bạn có chắc chắn muốn xóa bài học '${lesson.title}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteLesson(lesson.id)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun analyzeUnitWithAi() {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            android.widget.Toast.makeText(requireContext(), "Cần có mạng để AI phân tích Unit.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        val bundle = Bundle().apply {
            putLong("unit_id", unitId)
            putString("unit_title", binding.tvUnitTitle.text.toString())
        }
        findNavController().navigate(R.id.topicGroupFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
