package com.lumina.app.ui.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.view.children
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.lumina.app.R
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.data.source.remote.ai.GeminiService
import com.lumina.app.databinding.FragmentAddLessonBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.course.CourseViewModel

class AddLessonFragment : Fragment() {
    private var _binding: FragmentAddLessonBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val courseRepository = CourseRepository(
            courseDao = database.courseDao(),
            unitDao = database.unitDao(),
            lessonDao = database.lessonDao(),
            vocabularyDao = database.vocabularyDao(),
            topicGroupDao = database.topicGroupDao(),
            dictionaryApiService = null,
            geminiService = GeminiService(com.lumina.app.BuildConfig.GEMINI_API_KEY)
        )
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(courseRepository = courseRepository, sessionManager = sessionManager)
    }

    private var unitId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLessonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        unitId = arguments?.getLong("unit_id") ?: -1
        val unitTitle = arguments?.getString("unit_title") ?: "Chọn Unit"
        binding.etUnitName.setText(unitTitle)

        setupIconSelection()
        setupListeners()
    }

    private fun setupIconSelection() {
        binding.gridIcons.children.forEach { child ->
            if (child is FrameLayout) {
                child.setOnClickListener {
                    // Reset all icons
                    binding.gridIcons.children.forEach { other ->
                        if (other is FrameLayout) {
                            other.setBackgroundResource(R.drawable.bg_icon_square_gray)
                            val img = other.getChildAt(0) as? ImageView
                            img?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                        }
                    }
                    // Highlight selected icon
                    child.setBackgroundResource(R.drawable.bg_icon_selected_border)
                    val img = child.getChildAt(0) as? ImageView
                    img?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                }
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etLessonTitle.text.toString().trim()
            val order = 1
            
            if (title.isNotEmpty() && unitId != -1L) {
                viewModel.addLesson(unitId, title, order)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập tên Lesson", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
