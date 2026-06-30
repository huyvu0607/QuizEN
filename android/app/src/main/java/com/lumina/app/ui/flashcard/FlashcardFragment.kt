package com.lumina.app.ui.flashcard

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.lumina.app.BuildConfig
import com.lumina.app.data.model.SrsRating
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.repository.SrsRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.data.source.remote.DictionaryApiService
import com.lumina.app.data.source.remote.ai.GeminiService
import com.lumina.app.databinding.FragmentFlashcardBinding
import com.lumina.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import com.lumina.app.R

class FlashcardFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentFlashcardBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // Giả sử ta dùng navArgs để nhận lessonId, hoặc lấy từ Bundle
    // private val args: FlashcardFragmentArgs by navArgs()

    private val viewModel: FlashcardViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = CourseRepository(
            courseDao = database.courseDao(),
            unitDao = database.unitDao(),
            lessonDao = database.lessonDao(),
            vocabularyDao = database.vocabularyDao(),
            topicGroupDao = database.topicGroupDao(),
            dictionaryApiService = DictionaryApiService.create(),
            geminiService = GeminiService(BuildConfig.GEMINI_API_KEY),
            firestoreSync = com.lumina.app.data.repository.FirestoreSyncManager()
        )
        val srsRepository = SrsRepository(database.srsDao())
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(
            courseRepository = repository,
            srsRepository = srsRepository,
            sessionManager = sessionManager
        )
    }

    private lateinit var adapter: FlashcardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo TTS
        tts = TextToSpeech(requireContext(), this)

        val lessonId = arguments?.getLong("lesson_id") ?: -1L
        val lessonName = arguments?.getString("lesson_name") ?: "Flashcards"
        
        binding.tvUnitTitle.text = lessonName

        // Khởi tạo trạng thái ẩn cho nút SRS
        binding.layoutSrsButtons.alpha = 0f
        binding.layoutSrsButtons.translationY = 100f

        setupViewPager()
        setupListeners()
        observeViewModel()

        if (lessonId != -1L) {
            viewModel.loadVocabularies(lessonId)
        }
    }

    private fun setupViewPager() {
        adapter = FlashcardAdapter(
            onAudioClick = { vocab ->
                speak(vocab.word, vocab.audioUrl, vocab.exampleSentence)
            },
            onFavoriteClick = { vocab ->
                // Toggle favorite logic
                Toast.makeText(requireContext(), "Đã đánh dấu từ khó!", Toast.LENGTH_SHORT).show()
            },
            onCardFlipped = { isFlipped ->
                toggleSrsButtons(isFlipped)
            }
        )
        binding.viewPager.adapter = adapter
        
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setCurrentIndex(position)
                
                // Ẩn nút SRS khi sang trang mới (chờ lật thẻ)
                toggleSrsButtons(false)

                // Tự động phát âm thanh khi chuyển sang từ mới
                val currentList = adapter.currentList
                if (position in currentList.indices) {
                    val vocab = currentList[position]
                    speak(vocab.word, vocab.audioUrl, vocab.exampleSentence)
                }
            }
        })
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnAgain.setOnClickListener { handleSrsClick(SrsRating.FORGOT) }
        binding.btnHard.setOnClickListener { handleSrsClick(SrsRating.HARD) }
        binding.btnGood.setOnClickListener { handleSrsClick(SrsRating.GOOD) }
    }

    private fun toggleSrsButtons(show: Boolean) {
        val alpha = if (show) 1f else 0f
        val translationY = if (show) 0f else 100f
        
        if (show) binding.layoutSrsButtons.visibility = View.VISIBLE

        binding.layoutSrsButtons.animate()
            .alpha(alpha)
            .translationY(translationY)
            .setDuration(300)
            .withEndAction {
                if (!show) binding.layoutSrsButtons.visibility = View.INVISIBLE
            }
            .start()
    }

    private fun handleSrsClick(rating: SrsRating) {
        val currentList = adapter.currentList
        val position = binding.viewPager.currentItem
        if (position in currentList.indices) {
            val vocab = currentList[position]
            val sessionManager = SessionManager(requireContext())
            val userId = sessionManager.getUserId()
            viewModel.updateSrs(vocab, rating, userId)
            
            // Move to next card
            if (position < currentList.size - 1) {
                binding.viewPager.currentItem = position + 1
            } else {
                Toast.makeText(requireContext(), "Đã hoàn thành lượt ôn tập!", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.vocabularies.collectLatest { list ->
                adapter.submitList(list)
                updateProgress(viewModel.currentIndex.value, list.size)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentIndex.collectLatest { index ->
                updateProgress(index, viewModel.vocabularies.value.size)
            }
        }
    }

    private fun updateProgress(current: Int, total: Int) {
        if (total > 0) {
            val progress = ((current + 1) * 100) / total
            binding.progressBar.setProgress(progress, true)
            binding.tvProgress.text = "${current + 1} / $total từ"
        } else {
            binding.progressBar.progress = 0
            binding.tvProgress.text = "0 / 0 từ"
        }
    }

    private fun speak(text: String, audioUrl: String?, example: String?) {
        if (!audioUrl.isNullOrBlank()) {
            playAudio(url = audioUrl, fallbackText = text, example = example)
        } else {
            speakTts(text, example)
        }
    }

    private fun playAudio(url: String?, fallbackText: String, example: String?) {
        if (url.isNullOrBlank()) {
            speakTts(fallbackText, example)
            return
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setVolume(1.0f, 1.0f)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener { 
                    // Sau khi đọc từ xong, đọc ví dụ bằng TTS
                    example?.let { speakTts("", it) }
                }
                setOnErrorListener { _, _, _ ->
                    speakTts(fallbackText, example)
                    true
                }
            }
        } catch (e: Exception) {
            speakTts(fallbackText, example)
        }
    }

    private fun speakTts(text: String, example: String?) {
        if (isTtsReady) {
            val params = Bundle()
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            
            if (text.isNotBlank()) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "FlashcardTTS_Word")
                // Đọc ví dụ sau khi đọc từ (dùng QUEUE_ADD)
                example?.let { 
                    tts?.speak(it, TextToSpeech.QUEUE_ADD, params, "FlashcardTTS_Example") 
                }
            } else if (!example.isNullOrBlank()) {
                tts?.speak(example, TextToSpeech.QUEUE_FLUSH, params, "FlashcardTTS_Example")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        tts?.stop()
        tts?.shutdown()
        _binding = null
    }
}
