package com.lumina.app.ui.vocabulary

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
import androidx.navigation.fragment.findNavController
import com.lumina.app.R
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentVocabularyListBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.course.CourseViewModel
import com.lumina.app.ui.vocabulary.VocabularyAdapter
import com.lumina.app.ui.vocabulary.VocabularyStatus
import kotlinx.coroutines.launch

class VocabularyListFragment : Fragment() {
    private var _binding: FragmentVocabularyListBinding? = null
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVocabularyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        lessonId = arguments?.getLong("lesson_id") ?: -1
        if (lessonId != -1L) {
            viewModel.loadVocabularies(lessonId)
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvVocabulary.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = VocabularyAdapter(
                emptyList(),
                onAudioClick = { vocab ->
                    // Handle audio playback
                },
                onFavoriteClick = { vocab ->
                    // Handle favorite toggle
                },
                onItemClick = { vocab ->
                    val bundle = Bundle().apply {
                        putLong("lesson_id", lessonId)
                        putLong("vocabulary_id", vocab.id)
                    }
                    findNavController().navigate(R.id.addVocabularyFragment, bundle)
                }
            )
        }
    }

    private fun observeViewModel() {
        viewModel.currentLesson.observe(viewLifecycleOwner) { lesson ->
            lesson?.let {
                binding.tvLessonTitle.text = it.title
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.vocabularies.collect { vocabList ->
                    (binding.rvVocabulary.adapter as? VocabularyAdapter)?.updateData(vocabList)
                    binding.tvLessonStats.text = "${vocabList.size} từ vựng"
                }
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        binding.ivSettings.setOnClickListener {
            // Settings dialog
        }

        binding.fabAddVocabulary.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("lesson_id", lessonId)
            }
            findNavController().navigate(R.id.addVocabularyFragment, bundle)
        }

        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: View.NO_ID
            updateFilteredList(checkedId)
        }
    }

    private fun updateFilteredList(checkedId: Int) {
        val vocabList = viewModel.vocabularies.value
        val filteredList = when (checkedId) {
            R.id.chipLearned -> vocabList.filter { it.status == VocabularyStatus.LEARNED }
            R.id.chipNeedsReview -> vocabList.filter { it.status == VocabularyStatus.NEEDS_REVIEW }
            else -> vocabList
        }
        (binding.rvVocabulary.adapter as? VocabularyAdapter)?.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
