package com.lumina.app.ui.vocabulary

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.lumina.app.BuildConfig
import com.lumina.app.R
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.data.source.remote.DictionaryApiService
import com.lumina.app.data.source.remote.ai.GeminiService
import com.lumina.app.databinding.FragmentVocabularyListBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.course.CourseViewModel
import com.lumina.app.ui.vocabulary.VocabularyAdapter
import com.lumina.app.ui.vocabulary.VocabularyStatus
import kotlinx.coroutines.launch
import java.util.Locale

class VocabularyListFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentVocabularyListBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val viewModel: CourseViewModel by viewModels {
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
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(courseRepository = repository, sessionManager = sessionManager)
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
        
        tts = TextToSpeech(requireContext(), this)

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
                    speak(vocab.word, vocab.audioUrl)
                },
                onFavoriteClick = { vocab ->
                    viewModel.toggleFavorite(vocab.id, !vocab.isFavorite)
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

        binding.btnFlashcard.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("lesson_id", lessonId)
                putString("lesson_name", binding.tvLessonTitle.text.toString())
            }
            findNavController().navigate(R.id.flashcardFragment, bundle)
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

    private fun speak(text: String, audioUrl: String?) {
        if (!audioUrl.isNullOrBlank()) {
            playAudio(url = audioUrl, fallbackText = text)
        } else {
            speakTts(text)
        }
    }

    private fun playAudio(url: String?, fallbackText: String) {
        if (url.isNullOrBlank()) {
            speakTts(fallbackText)
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
                setOnErrorListener { _, _, _ ->
                    speakTts(fallbackText)
                    true
                }
            }
        } catch (e: Exception) {
            speakTts(fallbackText)
        }
    }

    private fun speakTts(text: String) {
        if (isTtsReady && text.isNotBlank()) {
            val params = Bundle()
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "VocabularyTTS")
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
