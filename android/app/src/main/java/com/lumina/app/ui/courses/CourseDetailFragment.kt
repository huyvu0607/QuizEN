package com.lumina.app.ui.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lumina.app.databinding.FragmentCourseDetailBinding

class CourseDetailFragment : Fragment() {
    private var _binding: FragmentCourseDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        val dummyUnits = listOf(
            UnitItem(1, "Education & Learning", "15 từ · 3 Bài học", UnitStatus.COMPLETED),
            UnitItem(2, "Work & Careers", "Đang học · 1/3 Bài học", UnitStatus.IN_PROGRESS),
            UnitItem(3, "Science & Technology", "20 từ · 4 Bài học", UnitStatus.LOCKED),
            UnitItem(4, "Health & Medicine", "12 từ · 2 Bài học", UnitStatus.LOCKED),
            UnitItem(5, "Environment", "18 từ · 3 Bài học", UnitStatus.LOCKED)
        )

        binding.rvUnits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = UnitAdapter(dummyUnits) { unit ->
                // Xử lý khi nhấn vào Unit (ví dụ: mở bài học)
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.fabAddUnit.setOnClickListener {
            findNavController().navigate(com.lumina.app.R.id.addUnitFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
