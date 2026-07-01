package com.lumina.app.ui.unit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lumina.app.R
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.data.source.remote.DictionaryApiService
import com.lumina.app.data.source.remote.ai.GeminiService
import com.lumina.app.databinding.FragmentTopicGroupsBinding
import com.lumina.app.ui.course.CourseViewModel
import com.lumina.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TopicGroupFragment : Fragment() {
    private var _binding: FragmentTopicGroupsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = CourseRepository(
            database.courseDao(),
            database.unitDao(),
            database.lessonDao(),
            database.vocabularyDao(),
            database.topicGroupDao(),
            DictionaryApiService.create(),
            GeminiService(com.lumina.app.BuildConfig.GEMINI_API_KEY),
            firestoreSync = com.lumina.app.data.repository.FirestoreSyncManager()
        )
        ViewModelFactory(courseRepository = repository, sessionManager = SessionManager(requireContext()))
    }

    private var unitId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        unitId = arguments?.getLong("unit_id") ?: -1
        val unitTitle = arguments?.getString("unit_title") ?: "Unit Details"
        binding.tvSubtitle.text = "Unit: $unitTitle"

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        if (unitId != -1L) {
            viewModel.loadTopicGroups(unitId)
        }
    }

    private fun setupRecyclerView() {
        binding.rvTopicGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TopicGroupAdapter(
                items = emptyList(),
                onWordClick = { vocabId ->
                    val bundle = Bundle().apply {
                        putLong("vocabulary_id", vocabId)
                    }
                    findNavController().navigate(R.id.addVocabularyFragment, bundle)
                },
                onWordLongClick = { vocabId, word ->
                    showMoveWordDialog(vocabId, word)
                },
                onGroupMenuClick = { view, group ->
                    showGroupOptionsMenu(view, group)
                }
            )
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        binding.btnRefresh.setOnClickListener {
            if (viewModel.vocabularies.value.isEmpty()) {
                Toast.makeText(requireContext(), "Cần thêm từ vựng vào bài học trước khi sử dụng AI.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), "AI đang phân loại lại...", Toast.LENGTH_SHORT).show()
            viewModel.triggerAiGrouping(unitId)
        }

        binding.btnAddGroup.setOnClickListener {
            showAddGroupDialog()
        }

        binding.btnModeLesson.setOnClickListener {
            findNavController().navigateUp() // Quay lại UnitDetailFragment (mặc định xem theo Lesson)
        }
    }

    private fun showAddGroupDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Tên chủ đề mới"
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Thêm nhóm chủ đề")
            .setView(input)
            .setPositiveButton("Thêm") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.addTopicGroup(unitId, name)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showGroupOptionsMenu(view: View, group: TopicGroupUiItem) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add("Đổi tên nhóm")
        popup.menu.add("Xóa nhóm")
        popup.setOnMenuItemClickListener { item: android.view.MenuItem ->
            when (item.title) {
                "Đổi tên nhóm" -> {
                    showRenameGroupDialog(group)
                    true
                }
                "Xóa nhóm" -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xóa nhóm chủ đề")
                        .setMessage("Bạn có chắc muốn xóa nhóm '${group.name}'? Các từ vựng sẽ không bị xóa khỏi bài học gốc.")
                        .setPositiveButton("Xóa") { _, _ ->
                            viewModel.deleteTopicGroup(group.id, unitId)
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showRenameGroupDialog(group: TopicGroupUiItem) {
        val input = android.widget.EditText(requireContext()).apply {
            setText(group.name)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Đổi tên nhóm")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.renameTopicGroup(group.id, unitId, name)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showMoveWordDialog(vocabId: Long, word: String) {
        val groups = viewModel.topicGroups.value
        val groupNames = groups.map { it.name }.toTypedArray()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Di chuyển từ '$word'")
            .setItems(groupNames) { _, which: Int ->
                val targetGroup = groups[which]
                viewModel.moveWordToGroup(vocabId, targetGroup.id, unitId)
                Toast.makeText(requireContext(), "Đã chuyển '$word' sang nhóm '${targetGroup.name}'", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topicGroups.collectLatest { groups ->
                if (groups.isEmpty() && 
                    !viewModel.isLoadingAiGrouping.value && 
                    viewModel.errorAi.value == null &&
                    !viewModel.isUnitAlreadyGrouped(unitId)) {
                    // Chỉ tự động kích hoạt AI lần đầu tiên cho Unit này
                    viewModel.triggerAiGrouping(unitId)
                }
                (binding.rvTopicGroups.adapter as? TopicGroupAdapter)?.updateData(groups)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoadingAiGrouping.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnRefresh.isEnabled = !isLoading
                if (isLoading) {
                    binding.rvTopicGroups.alpha = 0.5f
                } else {
                    binding.rvTopicGroups.alpha = 1.0f
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorAi.collectLatest { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearAiError()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
