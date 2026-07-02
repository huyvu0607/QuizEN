package com.lumina.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lumina.app.data.repository.AuthRepository
import com.lumina.app.data.repository.FirestoreSyncManager
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentForgotPasswordBinding
import com.lumina.app.viewmodel.ViewModelFactory

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getInstance(requireContext())
        val userRepository = UserRepository(database.userDao(), FirestoreSyncManager())
        val authRepository = AuthRepository(requireContext().applicationContext)
        val sessionManager = SessionManager(requireContext().applicationContext)
        val factory = ViewModelFactory(userRepository, authRepository, sessionManager)
        viewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            viewModel.resetPassword(email)
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnResetPassword.isEnabled = false
                }
                is AuthState.ResetEmailSent -> {
                    binding.btnResetPassword.isEnabled = true
                    Toast.makeText(requireContext(), "Email khôi phục đã được gửi!", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                }
                is AuthState.Error -> {
                    binding.btnResetPassword.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.btnResetPassword.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetState()
        _binding = null
    }
}
