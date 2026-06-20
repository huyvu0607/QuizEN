package com.lumina.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lumina.app.MainActivity
import com.lumina.app.data.repository.AuthRepository
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.databinding.FragmentRegisterBinding
import com.lumina.app.viewmodel.ViewModelFactory
import com.google.android.material.chip.Chip

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getInstance(requireContext())
        val userRepository = UserRepository(database.userDao())
        val authRepository = AuthRepository(requireContext().applicationContext)
        val factory = ViewModelFactory(userRepository, authRepository)
        viewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

        setupLevelDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupLevelDropdown() {
        val levels = arrayOf("A1", "A2", "B1", "B2", "C1", "C2")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        binding.actvLevel.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.tilName.editText?.text.toString().trim()
            val email = binding.tilEmail.editText?.text.toString().trim()
            val pass = binding.tilPassword.editText?.text.toString()
            val level = binding.actvLevel.text.toString().ifBlank { "A1" }

            val checkedChipId = binding.cgGoals.checkedChipId
            val goal = if (checkedChipId != View.NO_ID) {
                binding.cgGoals.findViewById<Chip>(checkedChipId)?.text?.toString() ?: "Giao tiếp"
            } else {
                "Giao tiếp"
            }

            viewModel.register(name, email, pass, level, goal)
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnRegister.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.btnRegister.isEnabled = true
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }
                is AuthState.Error -> {
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is AuthState.Idle -> {
                    binding.btnRegister.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}