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
import com.lumina.app.R
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentCourseDetailBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.lumina.app.ui.unit.UnitAdapter
import com.lumina.app.ui.unit.UnitStatus
import kotlinx.coroutines.launch

class CourseDetailFragment : Fragment() {
    private var _binding: FragmentCourseDetailBinding? = null
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

    private var courseId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        courseId = arguments?.getLong("course_id") ?: -1
        if (courseId != -1L) {
            viewModel.loadUnits(courseId)
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvUnits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = UnitAdapter(emptyList()) { unit ->
                val bundle = Bundle().apply {
                    putLong("unit_id", unit.id.toLong())
                    putString("unit_title", unit.title)
                }
                findNavController().navigate(R.id.unitDetailFragment, bundle)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentCourse.observe(viewLifecycleOwner) { course ->
            course?.let {
                binding.tvCourseTitle.text = it.title
                binding.tvLevelBadge.text = "Level ${it.level?.name ?: "N/A"}"
                // Cập nhật các thông tin khác nếu cần
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.units.collect { units ->
                    (binding.rvUnits.adapter as? UnitAdapter)?.updateData(units)
                    
                    // Cập nhật thống kê và tiến độ
                    val totalUnits = units.size
                    val totalWords = units.sumOf { it.wordCount }
                    val completedUnits = units.count { it.status == UnitStatus.COMPLETED }
                    
                    binding.tvCourseStats.text = "$totalUnits Units · $totalWords từ"
                    
                    // Tính % tiến độ
                    val progressPercent = if (totalUnits > 0) (completedUnits * 100) / totalUnits else 0
                    binding.tvProgressPercent.text = "$progressPercent%"
                    
                    // Cập nhật mô tả tiến độ
                    binding.tvProgressDesc.text = if (totalUnits > 0) {
                        "Bạn đã hoàn thành $completedUnits trên tổng số $totalUnits Unit."
                    } else {
                        "Chưa có Unit nào trong khóa học này."
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.fabAddUnit.setOnClickListener {
            val bundle = Bundle().apply { putLong("course_id", courseId) }
            findNavController().navigate(R.id.addUnitFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
