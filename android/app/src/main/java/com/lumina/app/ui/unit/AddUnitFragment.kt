package com.lumina.app.ui.unit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.view.children
import com.lumina.app.R
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentAddUnitBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.course.CourseViewModel

class AddUnitFragment : Fragment() {
    private var _binding: FragmentAddUnitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val courseRepository = CourseRepository(
            courseDao = database.courseDao(),
            unitDao = database.unitDao(),
            lessonDao = database.lessonDao(),
            vocabularyDao = database.vocabularyDao(),
            firestoreSync = com.lumina.app.data.repository.FirestoreSyncManager()
        )
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(courseRepository = courseRepository, sessionManager = sessionManager)
    }

    private var courseId: Long = -1
    private var selectedIcon: String? = "plane"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddUnitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        courseId = arguments?.getLong("course_id") ?: -1
        if (courseId != -1L) {
            viewModel.loadUnits(courseId) // Load to get current course title
        }
        
        setupIconSelection()
        setupListeners()
        observeViewModel()
    }

    private fun setupIconSelection() {
        binding.gridIcons.children.forEach { child ->
            if (child is FrameLayout) {
                child.setOnClickListener {
                    selectedIcon = child.tag?.toString() ?: "plane"

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

    private fun observeViewModel() {
        viewModel.currentCourse.observe(viewLifecycleOwner) { course ->
            course?.let {
                binding.etCourseName.setText(it.title)
            }
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    val msg = it.exceptionOrNull()?.message ?: "Lỗi khi lưu Unit"
                    android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = true
                }
                viewModel.clearSaveResult()
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        binding.ivSettings.setOnClickListener {
            // Settings logic
        }

        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etUnitName.text.toString().trim()
            val order = 1
            
            if (title.isNotEmpty() && courseId != -1L) {
                binding.btnSave.isEnabled = false
                viewModel.addUnit(courseId, title, selectedIcon, order)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
