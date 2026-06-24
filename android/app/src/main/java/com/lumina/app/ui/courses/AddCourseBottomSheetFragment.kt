package com.lumina.app.ui.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lumina.app.databinding.FragmentAddCourseBinding

class AddCourseBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddCourseBinding? = null
    private val binding get() = _binding!!

    private var courseToEdit: CourseUiItem? = null

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
        fillDataIfEditing()
    }

    private fun fillDataIfEditing() {
        courseToEdit?.let { course ->
            binding.tvTitle.text = "Chỉnh sửa khóa học"
            binding.tilCourseName.editText?.setText(course.title)
            binding.actvLevel.setText(course.level, false)
            // Có thể thêm các field khác như mô tả, icon nếu CourseUiItem có
        }
    }

    private fun setupLevelDropdown() {
        val levels = arrayOf("A1 - Beginner", "A2 - Pre-Int", "B1 - Intermediate", "B2 - Upper", "C1 - Advanced", "C2 - Mastery")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        binding.actvLevel.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            // Xử lý lưu khóa học ở đây
            dismiss()
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
