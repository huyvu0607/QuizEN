package com.lumina.app.ui.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.PopupMenu
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import com.lumina.app.R
import com.lumina.app.databinding.FragmentCoursesBinding

class CoursesFragment : Fragment() {
    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        val dummyCourses = listOf(
            CourseUiItem(1, "Business English 101", "B2 - Upper", 45, "45/100 từ", R.drawable.ic_briefcase),
            CourseUiItem(2, "Travel Survival Kit", "A2 - Pre-Int", 24, "12/50 từ", R.drawable.ic_plane),
            CourseUiItem(3, "IELTS Vocabulary", "C1 - Advanced", 100, "500/500 từ", R.drawable.ic_book, true)
        )

        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CourseAdapter(
                dummyCourses,
                onItemClick = { course ->
                    findNavController().navigate(R.id.courseDetailFragment)
                },
                onMoreClick = { view, course ->
                    showCourseOptions(view, course)
                }
            )
        }
    }

    private fun showCourseOptions(anchor: View, course: CourseUiItem) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_course_options, popup.menu)

        // Làm cho mục Xóa có chữ màu đỏ
        val deleteItem = popup.menu.findItem(R.id.action_delete)
        val s = SpannableString(deleteItem.title)
        s.setSpan(ForegroundColorSpan(Color.parseColor("#EF4444")), 0, s.length, 0)
        deleteItem.title = s

        // Ép buộc hiển thị icon trong PopupMenu
        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popup)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    val addCourseSheet = AddCourseBottomSheetFragment.newInstance(course)
                    addCourseSheet.show(parentFragmentManager, "EditCourseBottomSheet")
                    true
                }
                R.id.action_delete -> {
                    // Xử lý xóa khóa học
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.fabAddCourse.setOnClickListener {
            val addCourseSheet = AddCourseBottomSheetFragment.newInstance()
            addCourseSheet.show(parentFragmentManager, "AddCourseBottomSheet")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
