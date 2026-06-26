package com.lumina.app.ui.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentAddLessonBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.course.CourseViewModel

class AddLessonBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddLessonBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseViewModel by viewModels({ requireParentFragment() }) {
        val database = AppDatabase.getInstance(requireContext())
        val courseRepository = CourseRepository(
            database.courseDao(),
            database.unitDao(),
            database.lessonDao(),
            database.vocabularyDao()
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
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            val title = binding.tilLessonTitle.editText?.text.toString().trim()
            val order = 1 // tilOrder removed from layout
            
            if (title.isNotEmpty() && unitId != -1L) {
                viewModel.addLesson(unitId, title, order)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(unitId: Long) = AddLessonBottomSheetFragment().apply {
            arguments = Bundle().apply { putLong("unit_id", unitId) }
        }
    }
}
