package com.lumina.app.ui.quiz

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lumina.app.R
import com.lumina.app.databinding.FragmentQuizSetupBinding

class QuizSetupFragment : Fragment() {

    private var _binding: FragmentQuizSetupBinding? = null
    private val binding get() = _binding!!

    private var selectedMode = "Practice" // "Practice" or "Test"
    private var selectedSource = "Lesson" // "Lesson", "AI", "Mixed", "Weak"

    private var isAudioEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lessonId = arguments?.getLong("lesson_id") ?: -1L
        val lessonName = arguments?.getString("lesson_name") ?: "Lesson"
        val unitName = arguments?.getString("unit_name") ?: "Current Unit"

        binding.tvSourceSubtitle.text = "$unitName: $lessonName"

        setupListeners()
        updateModeUi()
        updateAudioUi()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnModePractice.setOnClickListener {
            selectedMode = "Practice"
            updateModeUi()
        }

        binding.btnModeTest.setOnClickListener {
            selectedMode = "Test"
            updateModeUi()
        }

        binding.btnAudioOption.setOnClickListener {
            isAudioEnabled = !isAudioEnabled
            updateAudioUi()
        }

        binding.sliderQuestions.addOnChangeListener { _, value, _ ->
            binding.tvQuestionCount.text = value.toInt().toString()
        }

        binding.btnStartQuiz.setOnClickListener {
            val bundle = Bundle().apply {
                putString("mode", selectedMode)
                putInt("question_count", binding.sliderQuestions.value.toInt())
                putBoolean("use_multiple_choice", binding.cbMultipleChoice.isChecked)
                putBoolean("use_fill_blank", binding.cbFillBlank.isChecked)
                putBoolean("use_audio", isAudioEnabled)
                
                // Pass source info
                putLong("lesson_id", arguments?.getLong("lesson_id") ?: -1L)
                putLong("topic_group_id", arguments?.getLong("topic_group_id") ?: -1L)
            }
            findNavController().navigate(R.id.quizFragment, bundle)
        }
    }

    private fun updateModeUi() {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val textPrimary = Color.parseColor("#1E293B")

        if (selectedMode == "Practice") {
            // Active Practice
            binding.btnModePractice.setCardBackgroundColor(primaryColor)
            binding.btnModePractice.cardElevation = 8f
            binding.ivModePractice.imageTintList = ColorStateList.valueOf(whiteColor)
            binding.tvModePractice.setTextColor(whiteColor)

            // Inactive Test
            binding.btnModeTest.setCardBackgroundColor(whiteColor)
            binding.btnModeTest.cardElevation = 2f
            binding.ivModeTest.imageTintList = ColorStateList.valueOf(textPrimary)
            binding.tvModeTest.setTextColor(textPrimary)
        } else {
            // Active Test
            binding.btnModeTest.setCardBackgroundColor(primaryColor)
            binding.btnModeTest.cardElevation = 8f
            binding.ivModeTest.imageTintList = ColorStateList.valueOf(whiteColor)
            binding.tvModeTest.setTextColor(whiteColor)

            // Inactive Practice
            binding.btnModePractice.setCardBackgroundColor(whiteColor)
            binding.btnModePractice.cardElevation = 2f
            binding.ivModePractice.imageTintList = ColorStateList.valueOf(textPrimary)
            binding.tvModePractice.setTextColor(textPrimary)
        }
    }

    private fun updateAudioUi() {
        if (isAudioEnabled) {
            binding.btnAudioOption.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            binding.btnAudioOption.iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            binding.btnAudioOption.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        } else {
            binding.btnAudioOption.strokeColor = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
            binding.btnAudioOption.iconTint = ColorStateList.valueOf(Color.parseColor("#94A3B8"))
            binding.btnAudioOption.setTextColor(Color.parseColor("#94A3B8"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
