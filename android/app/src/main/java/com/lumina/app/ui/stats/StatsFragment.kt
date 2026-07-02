package com.lumina.app.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lumina.app.R
import com.lumina.app.data.repository.StatsRepository
import com.lumina.app.data.repository.TopicStats
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentStatsBinding
import com.lumina.app.databinding.ItemTopicAccuracyBinding
import com.lumina.app.viewmodel.ViewModelFactory

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = StatsRepository(
            database.userDao(),
            database.vocabularyDao(),
            database.quizDao(),
            database.srsDao(),
            database.topicGroupDao()
        )
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(
            sessionManager = sessionManager,
            statsRepository = repository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Initialize cards
        binding.cardTotalWords.ivIcon.setImageResource(R.drawable.ic_layers)
        binding.cardTotalWords.tvLabel.text = "Tổng từ"

        binding.cardStreak.ivIcon.setImageResource(R.drawable.ic_flame)
        binding.cardStreak.tvLabel.text = "Streak"

        binding.cardXp.ivIcon.setImageResource(R.drawable.ic_star)
        binding.cardXp.tvLabel.text = "XP"

        binding.cardAccuracy.ivIcon.setImageResource(R.drawable.ic_check_circle)
        binding.cardAccuracy.tvLabel.text = "Tỉ lệ đúng"
    }

    private fun observeViewModel() {
        viewModel.stats.observe(viewLifecycleOwner) { data ->
            binding.cardTotalWords.tvValue.text = data.totalWords.toString()
            binding.cardStreak.tvValue.text = "${data.streakDays} ngày"
            binding.cardXp.tvValue.text = data.totalXp.toString()
            binding.cardAccuracy.tvValue.text = "${data.accuracyPercent}%"

            setupChart(data.weeklyActivity)
            setupTopicAccuracy(data.topicAccuracy)
            
            binding.tvReviewSubtitle.text = "Có ${data.dueCount} từ vựng đang chờ bạn ôn tập."
        }
    }

    private fun setupChart(activity: List<Int>) {
        binding.layoutChart.removeAllViews()
        val maxVal = activity.maxOrNull() ?: 1
        
        activity.forEachIndexed { index, value ->
            val bar = View(requireContext())
            val params = LinearLayout.LayoutParams(0, (value * 150 / maxVal).dpToPx(), 1f)
            params.setMargins(8.dpToPx(), 0, 8.dpToPx(), 0)
            bar.layoutParams = params
            
            val color = if (index == activity.size - 1) "#84CC16" else "#2161F3"
            bar.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(color))
            bar.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_icon_square_gray)
            
            binding.layoutChart.addView(bar)
        }
    }

    private fun setupTopicAccuracy(topics: List<TopicStats>) {
        binding.layoutTopicAccuracy.removeAllViews()
        topics.forEach { topic ->
            val itemBinding = ItemTopicAccuracyBinding.inflate(layoutInflater, binding.layoutTopicAccuracy, false)
            itemBinding.tvTopicName.text = topic.name
            itemBinding.tvAccuracyValue.text = "${topic.accuracy}%"
            itemBinding.pbAccuracy.progress = topic.accuracy
            itemBinding.pbAccuracy.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(topic.color))
            
            binding.layoutTopicAccuracy.addView(itemBinding.root)
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
