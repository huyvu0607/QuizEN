package com.lumina.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.lumina.app.R
import com.lumina.app.data.repository.HomeRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentHomeBinding
import com.lumina.app.viewmodel.ViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val homeRepository = HomeRepository(
            database.userDao(),
            database.courseDao(),
            database.vocabularyDao(),
            database.srsDao(),
            database.quizDao()
        )
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(homeRepository = homeRepository, sessionManager = sessionManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: HomeUiState) {
        // Greeting + streak badge
        binding.tvGreeting.text = getString(R.string.home_greeting, state.userName) + " 👋"
        binding.tvStreakBadge.text = getString(R.string.home_streak_days, state.streakDays)

        // Daily goal
        binding.tvXpProgress.text = getString(R.string.home_xp_progress, state.currentXp, state.goalXp)
        setProgressWidth(binding.progressDailyGoal, state.currentXp, state.goalXp)

        // Stat cards
        binding.statTotalWords.ivStatIcon.setImageResource(R.drawable.ic_layers)
        binding.statTotalWords.tvStatValue.text = state.totalWords.toString()
        binding.statTotalWords.tvStatLabel.text = getString(R.string.home_total_words)

        binding.statStreak.ivStatIcon.setImageResource(R.drawable.ic_flame)
        binding.statStreak.tvStatValue.text = state.streakDays.toString()
        binding.statStreak.tvStatLabel.text = getString(R.string.home_streak_label)

        binding.statAccuracy.ivStatIcon.setImageResource(R.drawable.ic_check_circle)
        binding.statAccuracy.tvStatValue.text = "${state.accuracyPercent}%"
        binding.statAccuracy.tvStatLabel.text = getString(R.string.home_accuracy_label)

        // Continue learning
        state.continueLearning?.let { course ->
            with(binding.mainContent) {
                tvCourseLevel.text = course.level
                tvCourseTitle.text = course.courseTitle
                tvContinuePercent.text = "${course.progressPercent}%"
                setProgressWidth(progressContinueLearning, course.progressPercent, 100)

                course.iconRes?.let { ivCourseIcon.setImageResource(it) }
                course.coverColor?.let { colorStr ->
                    try {
                        val color = android.graphics.Color.parseColor(colorStr)
                        flIconContainer.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(color)
                        ivCourseIcon.setColorFilter(android.graphics.Color.WHITE)
                    } catch (e: Exception) {
                        flIconContainer.backgroundTintList = null
                        ivCourseIcon.setColorFilter(android.graphics.Color.WHITE)
                    }
                } ?: run {
                    flIconContainer.backgroundTintList = null
                    ivCourseIcon.setColorFilter(android.graphics.Color.WHITE)
                }
            }
        }

        // Reminder
        binding.mainContent.tvReviewSubtitle.text =
            getString(R.string.home_review_subtitle, state.reviewWordsCount)

        // Course cards
        if (state.courses.isNotEmpty()) {
            val travel = state.courses[0]
            with(binding.courseCardTravel) {
                ivCourseIcon.setImageResource(travel.iconRes)
                travel.coverColor?.let { colorStr ->
                    try {
                        val color = android.graphics.Color.parseColor(colorStr)
                        flIconContainer.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(color)
                        ivCourseIcon.setColorFilter(android.graphics.Color.WHITE)
                    } catch (e: Exception) {
                        flIconContainer.backgroundTintList = null
                        ivCourseIcon.setColorFilter(
                            androidx.core.content.ContextCompat.getColor(
                                requireContext(),
                                R.color.colorPrimary
                            )
                        )
                    }
                } ?: run {
                    flIconContainer.backgroundTintList = null
                    ivCourseIcon.setColorFilter(
                        androidx.core.content.ContextCompat.getColor(
                            requireContext(),
                            R.color.colorPrimary
                        )
                    )
                }
                tvCourseName.text = travel.name
                tvCourseWords.text =
                    getString(R.string.home_words_progress, travel.wordsLearned, travel.wordsTotal)
                pbCourseProgress.max = travel.wordsTotal
                pbCourseProgress.progress = travel.wordsLearned
            }
        }

        if (state.courses.size >= 2) {
            val it = state.courses[1]
            with(binding.courseCardIt) {
                ivCourseIcon.setImageResource(it.iconRes)
                it.coverColor?.let { colorStr ->
                    try {
                        val color = android.graphics.Color.parseColor(colorStr)
                        flIconContainer.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(color)
                        ivCourseIcon.setColorFilter(android.graphics.Color.WHITE)
                    } catch (e: Exception) {
                        flIconContainer.backgroundTintList = null
                        ivCourseIcon.setColorFilter(
                            androidx.core.content.ContextCompat.getColor(
                                requireContext(),
                                R.color.colorPrimary
                            )
                        )
                    }
                } ?: run {
                    flIconContainer.backgroundTintList = null
                    ivCourseIcon.setColorFilter(
                        androidx.core.content.ContextCompat.getColor(
                            requireContext(),
                            R.color.colorPrimary
                        )
                    )
                }
                tvCourseName.text = it.name
                tvCourseWords.text =
                    getString(R.string.home_words_progress, it.wordsLearned, it.wordsTotal)
                pbCourseProgress.max = it.wordsTotal
                pbCourseProgress.progress = it.wordsLearned
            }
        }
    }

    private fun setProgressWidth(progressView: View, current: Int, max: Int) {
        val parent = progressView.parent as? View ?: return
        parent.post {
            val ratio = if (max > 0) current.toFloat() / max.toFloat() else 0f
            val params = progressView.layoutParams
            params.width = (parent.width * ratio.coerceIn(0f, 1f)).toInt()
            progressView.layoutParams = params
        }
    }

    private fun setupClickListeners() {
        binding.mainContent.btnStartReview.setOnClickListener {
            // TODO: navigate sang Review/Flashcard screen
        }

        binding.mainContent.cardContinueLearning.setOnClickListener {
            viewModel.uiState.value?.courses?.firstOrNull()?.let { course ->
                val bundle = Bundle().apply { putLong("course_id", course.id.toLong()) }
                findNavController().navigate(R.id.courseDetailFragment, bundle)
            }
        }

        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.coursesFragment)
        }

        binding.ivSettings.setOnClickListener {
            // TODO: navigate sang Settings
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}