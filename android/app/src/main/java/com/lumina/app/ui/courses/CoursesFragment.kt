package com.lumina.app.ui.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lumina.app.databinding.FragmentPlaceholderBinding

class CoursesFragment : Fragment() {
    private var _binding: FragmentPlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvPlaceholder.text = "Courses — sắp ra mắt"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}