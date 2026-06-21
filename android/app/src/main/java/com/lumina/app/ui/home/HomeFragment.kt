package com.lumina.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lumina.app.R
import com.lumina.app.databinding.FragmentHomeBinding

/**
 * Màn hình Home — tổng quan tiến trình học của người dùng.
 * Hiện đang dùng dữ liệu mẫu (mock). Khi backend/API sẵn sàng,
 * thay phần loadMockData() bằng dữ liệu thật từ HomeViewModel.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        loadMockData()
        setupClickListeners()
    }

    private fun loadMockData() {
        val userName = "Nam"
        val streakDays = 7
        val totalWords = 248
        val accuracyPercent = 84
        val currentXp = 450
        val goalXp = 500

        val continueCourseLevel = "B2 - Intermediate"
        val continueCourseTitle = "Business English"
        val continueCourseProgress = 65 // %

        val reviewWordsCount = 12

        // Greeting + streak badge
        binding.tvGreeting.text = getString(R.string.home_greeting, userName) + " 👋"
        binding.tvStreakBadge.text = getString(R.string.home_streak_days, streakDays)

        // Daily goal progress
        binding.tvXpProgress.text = getString(R.string.home_xp_progress, currentXp, goalXp)
        setProgressWidth(binding.progressDailyGoal, currentXp, goalXp)

        // Stat cards (mỗi card là 1 <include>)
        binding.statTotalWords.ivStatIcon.setImageResource(R.drawable.ic_layers)
        binding.statTotalWords.tvStatValue.text = totalWords.toString()
        binding.statTotalWords.tvStatLabel.text = getString(R.string.home_total_words)

        binding.statStreak.ivStatIcon.setImageResource(R.drawable.ic_flame)
        binding.statStreak.tvStatValue.text = streakDays.toString()
        binding.statStreak.tvStatLabel.text = getString(R.string.home_streak_label)

        binding.statAccuracy.ivStatIcon.setImageResource(R.drawable.ic_check_circle)
        binding.statAccuracy.tvStatValue.text = "$accuracyPercent%"
        binding.statAccuracy.tvStatLabel.text = getString(R.string.home_accuracy_label)

        // Continue learning card (bên trong layout_home_main_content)
        binding.mainContent.tvCourseLevel.text = continueCourseLevel
        binding.mainContent.tvCourseTitle.text = continueCourseTitle
        binding.mainContent.tvContinuePercent.text = "$continueCourseProgress%"
        setProgressWidth(binding.mainContent.progressContinueLearning, continueCourseProgress, 100)

        // Reminder card (cũng nằm trong layout_home_main_content)
        binding.mainContent.tvReviewSubtitle.text =
            getString(R.string.home_review_subtitle, reviewWordsCount)

        // Course card 1: Travel Vocabulary
        binding.courseCardTravel.ivCourseIcon.setImageResource(R.drawable.ic_plane)
        binding.courseCardTravel.iconContainer.setBackgroundResource(R.drawable.bg_icon_square_purple)
        binding.courseCardTravel.tvCourseName.text = "Travel Vocabulary"
        binding.courseCardTravel.tvCourseWords.text = getString(R.string.home_words_progress, 45, 120)
        binding.courseCardTravel.progressCourse.setBackgroundResource(R.drawable.bg_progress_fill_purple)
        setProgressWidth(binding.courseCardTravel.progressCourse, 45, 120)

        // Course card 2: IT Jargon
        binding.courseCardIt.ivCourseIcon.setImageResource(R.drawable.ic_code)
        binding.courseCardIt.iconContainer.setBackgroundResource(R.drawable.bg_icon_square_gray)
        binding.courseCardIt.tvCourseName.text = "IT Jargon"
        binding.courseCardIt.tvCourseWords.text = getString(R.string.home_words_progress, 10, 50)
        binding.courseCardIt.progressCourse.setBackgroundResource(R.drawable.bg_progress_fill_black)
        setProgressWidth(binding.courseCardIt.progressCourse, 10, 50)
    }

    /**
     * Set chiều rộng của progress fill (View) theo tỉ lệ current/max,
     * dựa trên chiều rộng thực tế của progress track sau khi layout xong.
     */
    private fun setProgressWidth(progressView: View, current: Int, max: Int) {
        val parent = progressView.parent as? View ?: return
        parent.post {
            val ratio = if (max > 0) current.toFloat() / max.toFloat() else 0f
            val trackWidth = parent.width
            val params = progressView.layoutParams
            params.width = (trackWidth * ratio.coerceIn(0f, 1f)).toInt()
            progressView.layoutParams = params
        }
    }

    private fun setupClickListeners() {
        binding.mainContent.btnStartReview.setOnClickListener {
            // TODO: điều hướng sang màn hình ôn tập flashcard (Practice flow)
            // findNavController().navigate(R.id.action_homeFragment_to_reviewFragment)
        }

        binding.mainContent.cardContinueLearning.setOnClickListener {
            // TODO: điều hướng vào chi tiết Course đang học dở
        }

        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.coursesFragment)
        }

        binding.ivSettings.setOnClickListener {
            // TODO: mở màn hình Settings
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}