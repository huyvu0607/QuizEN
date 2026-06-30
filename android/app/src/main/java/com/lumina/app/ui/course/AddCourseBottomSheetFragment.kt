package com.lumina.app.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lumina.app.R
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentAddCourseBinding
import com.lumina.app.viewmodel.ViewModelFactory

class AddCourseBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddCourseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val courseRepository = CourseRepository(
            database.courseDao(),
            database.unitDao(),
            database.lessonDao(),
            database.vocabularyDao(),
            firestoreSync = com.lumina.app.data.repository.FirestoreSyncManager()
        )
        val sessionManager = SessionManager(requireContext())
        ViewModelFactory(courseRepository = courseRepository, sessionManager = sessionManager)
    }

    private var courseToEdit: CourseUiItem? = null
    private var selectedColor: String = "#2161F3"
    private var selectedIcon: String = "book"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupLevelDropdown()
        setupListeners()
        setupCoverPickers()
        fillDataIfEditing()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    android.widget.Toast.makeText(requireContext(), "Lưu khóa học thành công", android.widget.Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    val message = it.exceptionOrNull()?.message ?: "Có lỗi xảy ra khi lưu"
                    android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
                }
                viewModel.clearSaveResult()
            }
        }
    }

    private fun fillDataIfEditing() {
        courseToEdit?.let { course ->
            binding.tvTitle.text = "Chỉnh sửa khóa học"
            binding.tilCourseName.editText?.setText(course.title)
            binding.actvLevel.setText(course.level, false)
            selectedColor = course.coverColor ?: "#2161F3"
            selectedIcon = course.coverIcon ?: "book"
            
            // Re-setup pickers to reflect loaded values
            setupCoverPickers()
        }
    }

    private fun setupLevelDropdown() {
        val levels = arrayOf("A1 - Beginner", "A2 - Pre-Int", "B1 - Intermediate", "B2 - Upper", "C1 - Advanced", "C2 - Mastery")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        binding.actvLevel.setAdapter(adapter)
    }

    private fun setupCoverPickers() {
        val colorViews = listOf(
            binding.colorBlue to "#2161F3",
            binding.colorPurple to "#9333EA",
            binding.colorGreen to "#84CC16",
            binding.colorPink to "#FCE7F3",
            binding.colorOrange to "#FFEDD5",
            binding.colorCyan to "#CFFAFE"
        )

        colorViews.forEach { (view, color) ->
            view.setOnClickListener {
                selectedColor = color
                // Reset all scales
                colorViews.forEach { it.first.scaleX = 1f; it.first.scaleY = 1f; it.first.alpha = 0.5f }
                // Highlight selected
                view.scaleX = 1.2f
                view.scaleY = 1.2f
                view.alpha = 1f
            }
            // Set initial state
            if (color == selectedColor) {
                view.scaleX = 1.2f
                view.scaleY = 1.2f
                view.alpha = 1f
            } else {
                view.alpha = 0.5f
            }
        }

        val iconViews = listOf(
            binding.iconBook to "book",
            binding.iconStar to "star",
            binding.iconTrophy to "trophy",
            binding.iconBrain to "brain",
            binding.iconGlobe to "globe"
        )

        iconViews.forEach { (frame, icon) ->
            frame.setOnClickListener {
                selectedIcon = icon
                // Reset all backgrounds and tints
                iconViews.forEach { (f, _) ->
                    f.setBackgroundResource(R.drawable.bg_icon_square_gray)
                    val img = f.getChildAt(0) as? android.widget.ImageView
                    img?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                }
                // Set selected background and tint
                frame.setBackgroundResource(R.drawable.bg_icon_selected_border)
                val img = frame.getChildAt(0) as? android.widget.ImageView
                img?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            }
            
            // Set initial state
            if (icon == selectedIcon) {
                frame.setBackgroundResource(R.drawable.bg_icon_selected_border)
                val img = frame.getChildAt(0) as? android.widget.ImageView
                img?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            } else {
                frame.setBackgroundResource(R.drawable.bg_icon_square_gray)
                val img = frame.getChildAt(0) as? android.widget.ImageView
                img?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }
        }
    }

    private fun setupListeners() {
        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            val name = binding.tilCourseName.editText?.text.toString().trim()
            val desc = binding.tilDescription.editText?.text.toString().trim()
            val level = binding.actvLevel.text.toString()
            
            if (name.isNotEmpty()) {
                if (courseToEdit == null) {
                    viewModel.addCourse(name, desc, level, selectedColor, selectedIcon)
                } else {
                    viewModel.updateCourse(courseToEdit!!.id, name, desc, level, selectedColor, selectedIcon)
                }
            } else {
                binding.tilCourseName.error = "Tên khóa học không được để trống"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddCourseBottomSheet"
        fun newInstance(course: CourseUiItem? = null) = AddCourseBottomSheetFragment().apply {
            courseToEdit = course
        }
    }
}
