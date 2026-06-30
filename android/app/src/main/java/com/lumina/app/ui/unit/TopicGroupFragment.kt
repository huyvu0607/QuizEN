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
            GeminiService(com.lumina.app.BuildConfig.GEMINI_API_KEY)
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
            adapter = TopicGroupAdapter(emptyList()) { vocabId ->
                val bundle = Bundle().apply {
                    putLong("vocabulary_id", vocabId)
                }
                findNavController().navigate(R.id.addVocabularyFragment, bundle)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        binding.btnRefresh.setOnClickListener {
            Toast.makeText(requireContext(), "AI đang phân loại lại...", Toast.LENGTH_SHORT).show()
            viewModel.triggerAiGrouping(unitId)
        }

        binding.btnModeLesson.setOnClickListener {
            findNavController().navigateUp() // Quay lại UnitDetailFragment (mặc định xem theo Lesson)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topicGroups.collectLatest { groups ->
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
