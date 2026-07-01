package com.lumina.app.ui.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lumina.app.R
import com.lumina.app.data.repository.HomeRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentPracticeBinding
import com.lumina.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PracticeFragment : Fragment() {
    private var _binding: FragmentPracticeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PracticeViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val homeRepository = HomeRepository(
            database.userDao(),
            database.courseDao(),
            database.unitDao(),
            database.lessonDao(),
            database.vocabularyDao(),
            database.srsDao(),
            database.quizDao()
        )
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(homeRepository = homeRepository, sessionManager = sessionManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.cardDailyReview.setOnClickListener {
            // Start SRS review session
            Toast.makeText(requireContext(), "Bắt đầu ôn tập hằng ngày", Toast.LENGTH_SHORT).show()
        }

        binding.cardWeakWords.setOnClickListener {
            // Start weak words quiz
            val bundle = Bundle().apply {
                putString("mode", "Test")
                putBoolean("is_weak_words", true)
            }
            findNavController().navigate(R.id.quizSetupFragment, bundle)
        }

        binding.cardMixedQuiz.setOnClickListener {
            // Mixed quiz from all lessons
            findNavController().navigate(R.id.quizSetupFragment)
        }

        binding.cardFavoritePractice.setOnClickListener {
            Toast.makeText(requireContext(), "Tính năng từ yêu thích đang được phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.srsCount.collectLatest { count ->
                binding.tvSrsCount.text = if (count > 0) {
                    "Bạn có $count từ cần ôn tập hôm nay"
                } else {
                    "Tuyệt vời! Bạn đã hoàn thành bài ôn tập hôm nay"
                }
                binding.btnStartSrs.isEnabled = count > 0
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
