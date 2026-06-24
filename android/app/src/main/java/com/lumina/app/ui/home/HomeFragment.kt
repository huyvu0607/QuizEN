package com.lumina.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.lumina.app.R
import com.lumina.app.databinding.FragmentHomeBinding
import com.lumina.app.viewmodel.ViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory()
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
            binding.mainContent.tvCourseLevel.text = course.level
            binding.mainContent.tvCourseTitle.text = course.courseTitle
            binding.mainContent.tvContinuePercent.text = "${course.progressPercent}%"
            setProgressWidth(binding.mainContent.progressContinueLearning, course.progressPercent, 100)
        }

        // Reminder
        binding.mainContent.tvReviewSubtitle.text =
            getString(R.string.home_review_subtitle, state.reviewWordsCount)

        // Course cards (hiện cố định 2 card, sau dùng RecyclerView)
        if (state.courses.isNotEmpty()) {
            val travel = state.courses[0]
            binding.courseCardTravel.ivCourseIcon.setImageResource(travel.iconRes)
            binding.courseCardTravel.flIconContainer.setBackgroundResource(travel.iconBgRes)
            binding.courseCardTravel.tvCourseName.text = travel.name
            binding.courseCardTravel.tvCourseWords.text =
                getString(R.string.home_words_progress, travel.wordsLearned, travel.wordsTotal)
            binding.courseCardTravel.pbCourseProgress.max = travel.wordsTotal
            binding.courseCardTravel.pbCourseProgress.progress = travel.wordsLearned
        }

        if (state.courses.size >= 2) {
            val it = state.courses[1]
            binding.courseCardIt.ivCourseIcon.setImageResource(it.iconRes)
            binding.courseCardIt.flIconContainer.setBackgroundResource(it.iconBgRes)
            binding.courseCardIt.tvCourseName.text = it.name
            binding.courseCardIt.tvCourseWords.text =
                getString(R.string.home_words_progress, it.wordsLearned, it.wordsTotal)
            binding.courseCardIt.pbCourseProgress.max = it.wordsTotal
            binding.courseCardIt.pbCourseProgress.progress = it.wordsLearned
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
            // TODO: navigate sang Course detail
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