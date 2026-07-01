package com.lumina.app.ui.quiz

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.lumina.app.R
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.repository.SrsRepository
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentQuizBinding
import com.lumina.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    // Sử dụng activityViewModels để chia sẻ dữ liệu với QuizResultFragment
    private val viewModel: QuizViewModel by activityViewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repo = CourseRepository(
            database.courseDao(), database.unitDao(), database.lessonDao(), 
            database.vocabularyDao(), database.topicGroupDao()
        )
        val srsRepo = SrsRepository(database.srsDao())
        val userRepo = UserRepository(database.userDao())
        ViewModelFactory(courseRepository = repo, srsRepository = srsRepo, userRepository = userRepo)
    }

    private var timer: CountDownTimer? = null
    private var isAnswered = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lessonId = arguments?.getLong("lesson_id") ?: -1L
        val topicGroupId = arguments?.getLong("topic_group_id") ?: -1L
        val count = arguments?.getInt("question_count") ?: 10
        val useMultipleChoice = arguments?.getBoolean("use_multiple_choice") ?: true
        val useFillBlank = arguments?.getBoolean("use_fill_blank") ?: true

        val types = mutableListOf<QuizType>()
        if (useMultipleChoice) types.add(QuizType.MULTIPLE_CHOICE)
        if (useFillBlank) types.add(QuizType.FILL_IN_THE_BLANK)

        viewModel.generateQuiz(lessonId, topicGroupId, count, types)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnExit.setOnClickListener {
            findNavController().navigateUp()
        }

        val mcButtons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        mcButtons.forEachIndexed { index, btn ->
            btn.setOnClickListener { 
                if (!isAnswered) {
                    val rawAnswer = viewModel.questions.value[viewModel.currentQuestionIndex.value].options[index]
                    handleAnswer(rawAnswer, btn) 
                }
            }
        }

        binding.btnNext.setOnClickListener {
            if (isAnswered) {
                moveToNextQuestion()
            } else {
                val question = viewModel.questions.value[viewModel.currentQuestionIndex.value]
                if (question.type == QuizType.FILL_IN_THE_BLANK) {
                    handleAnswer(binding.etAnswerInput.text.toString(), null)
                }
            }
        }
    }

    private fun handleAnswer(answer: String, selectedBtn: MaterialButton?) {
        isAnswered = true
        timer?.cancel()
        
        val question = viewModel.questions.value[viewModel.currentQuestionIndex.value]
        val isCorrect = answer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
        
        val sessionManager = SessionManager(requireContext())
        viewModel.submitAnswer(answer, sessionManager.getUserId())

        if (question.type == QuizType.MULTIPLE_CHOICE) {
            highlightMcAnswers(selectedBtn, question.correctAnswer)
        } else {
            highlightFillBlankAnswer(isCorrect, question.correctAnswer)
        }

        binding.btnNext.text = "Tiếp tục"
        binding.btnNext.visibility = View.VISIBLE
    }

    private fun highlightMcAnswers(selectedBtn: MaterialButton?, correctAnswer: String) {
        val mcButtons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        val question = viewModel.questions.value[viewModel.currentQuestionIndex.value]
        
        mcButtons.forEachIndexed { index, btn ->
            val optionText = question.options[index]
            if (optionText == correctAnswer) {
                btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#22C55E")))
                btn.setBackgroundColor(Color.parseColor("#F0FDF4"))
                btn.setIconResource(R.drawable.ic_check_circle)
                btn.setIconTint(ColorStateList.valueOf(Color.parseColor("#22C55E")))
            } else if (btn == selectedBtn) {
                btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#EF4444")))
                btn.setBackgroundColor(Color.parseColor("#FEF2F2"))
                btn.setIconResource(R.drawable.ic_close)
                btn.setIconTint(ColorStateList.valueOf(Color.parseColor("#EF4444")))
            }
        }
    }

    private fun highlightFillBlankAnswer(isCorrect: Boolean, correctAnswer: String) {
        if (isCorrect) {
            binding.tilAnswerInput.boxStrokeColor = Color.parseColor("#22C55E")
            binding.tilAnswerInput.helperText = "Chính xác!"
        } else {
            binding.tilAnswerInput.boxStrokeColor = Color.parseColor("#EF4444")
            binding.tilAnswerInput.error = "Sai rồi! Đáp án đúng là: $correctAnswer"
        }
    }

    private fun moveToNextQuestion() {
        if (viewModel.currentQuestionIndex.value < viewModel.questions.value.size - 1) {
            viewModel.nextQuestion()
            resetUiForNextQuestion()
        } else {
            // Navigate to results
            findNavController().navigate(R.id.quizResultFragment)
        }
    }

    private fun resetUiForNextQuestion() {
        isAnswered = false
        binding.btnNext.text = "Kiểm tra"
        binding.tilAnswerInput.helperText = null
        binding.tilAnswerInput.error = null
        binding.tilAnswerInput.boxStrokeColor = Color.parseColor("#E2E8F0")
        binding.etAnswerInput.text?.clear()
        
        val mcButtons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        mcButtons.forEach { btn ->
            btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E2E8F0")))
            btn.setBackgroundColor(Color.WHITE)
            btn.setIconResource(0)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.questions.collectLatest { questions ->
                if (questions.isNotEmpty()) {
                    updateQuestionUi()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentQuestionIndex.collectLatest { _ ->
                if (viewModel.questions.value.isNotEmpty()) {
                    updateQuestionUi()
                }
            }
        }
    }

    private fun updateQuestionUi() {
        val questions = viewModel.questions.value
        val index = viewModel.currentQuestionIndex.value
        if (index >= questions.size) return
        
        val question = questions[index]

        binding.tvQuestionCount.text = "Câu ${index + 1} / ${questions.size}"
        binding.quizProgressBar.progress = ((index + 1) * 100) / questions.size

        if (question.type == QuizType.MULTIPLE_CHOICE) {
            binding.layoutMultipleChoice.visibility = View.VISIBLE
            binding.layoutFillBlank.visibility = View.GONE
            binding.btnNext.visibility = View.GONE
            
            binding.tvQuestionWord.text = question.questionText
            binding.tvQuestionIpa.text = question.vocabulary.ipa
            
            val letters = listOf("A", "B", "C", "D")
            val mcButtons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
            mcButtons.forEachIndexed { i, btn ->
                btn.text = "${letters[i]}    ${question.options.getOrNull(i) ?: ""}"
            }
        } else {
            binding.layoutMultipleChoice.visibility = View.GONE
            binding.layoutFillBlank.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE
            binding.btnNext.text = "Kiểm tra"
            
            binding.tvSentence.text = question.questionText
            binding.tvHintText.text = "Gợi ý: ${question.hint ?: ""}_______"
        }

        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        val timeLimit = if (arguments?.getString("mode") == "Test") 30000L else 120000L
        
        timer = object : CountDownTimer(timeLimit, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = "${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                if (!isAnswered) {
                    handleAnswer("", null)
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
