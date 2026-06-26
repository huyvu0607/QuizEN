package com.lumina.app.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.PopupMenu
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import com.lumina.app.R
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentCoursesBinding
import com.lumina.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class CoursesFragment : Fragment() {
    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CourseViewModel by viewModels {
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
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CourseAdapter(
                emptyList(),
                onItemClick = { course ->
                    val bundle = Bundle().apply { putLong("course_id", course.id) }
                    findNavController().navigate(R.id.courseDetailFragment, bundle)
                },
                onMoreClick = { view, course ->
                    showCourseOptions(view, course)
                }
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.courses.collect { courses ->
                    (binding.rvCourses.adapter as? CourseAdapter)?.updateData(courses)
                }
            }
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
                    viewModel.deleteCourse(course.id)
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
