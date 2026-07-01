package com.lumina.app.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lumina.app.R
import com.lumina.app.databinding.FragmentQuizResultBinding
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class QuizResultFragment : Fragment() {

    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!

    // Share ViewModel với QuizFragment để lấy kết quả
    private val viewModel: QuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        setupListeners()
    }

    private fun setupUi() {
        val total = viewModel.questions.value.size
        val correct = viewModel.score.value
        val wrong = viewModel.wrongVocabularies.value.size
        val skipped = total - (correct + wrong)
        val percent = if (total > 0) (correct * 100) / total else 0

        binding.tvPercentage.text = "$percent%"
        binding.circularProgress.progress = percent
        binding.tvCorrectCount.text = correct.toString()
        binding.tvWrongCount.text = wrong.toString()
        binding.tvSkippedCount.text = skipped.toString()
        
        binding.tvXpEarned.text = "+${correct * 5} XP"
        binding.tvWrongWordsBadge.text = "$wrong từ"

        binding.tvFeedback.text = when {
            percent >= 90 -> "Xuất sắc!"
            percent >= 70 -> "Tuyệt vời!"
            percent >= 50 -> "Khá tốt!"
            else -> "Cố gắng lên nhé!"
        }

        if (percent == 100) {
            triggerFireworks()
        }

        // Wrong words list
        binding.rvWrongWords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWrongWords.adapter = WrongWordsAdapter(viewModel.wrongVocabularies.value) { vocab ->
            // Play audio logic if needed
        }
    }

    private fun triggerFireworks() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xbda5ff),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        binding.konfettiView.start(party)
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnHome.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnRetryWrong.setOnClickListener {
            // Logic to retry only wrong words
        }

        binding.btnNewQuiz.setOnClickListener {
            findNavController().navigateUp() // Back to Setup
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
