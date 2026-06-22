package com.lumina.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lumina.app.MainActivity
import com.lumina.app.R
import com.lumina.app.data.repository.AuthRepository
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentLoginBinding
import com.lumina.app.viewmodel.ViewModelFactory

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getInstance(requireContext())
        val userRepository = UserRepository(database.userDao())
        val authRepository = AuthRepository(requireContext().applicationContext)
        val sessionManager = SessionManager(requireContext().applicationContext)
        val factory = ViewModelFactory(userRepository, authRepository, sessionManager)
        viewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val pass = binding.tilPassword.editText?.text.toString()
            viewModel.login(email, pass)
        }

        binding.btnGoogle.setOnClickListener {
            val webClientId = getString(R.string.default_web_client_id)
            viewModel.loginWithGoogle(webClientId)
        }

        binding.tvGuestHeader.setOnClickListener {
            viewModel.continueAsGuest()
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnGoogle.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogle.isEnabled = true
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }
                is AuthState.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogle.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is AuthState.Idle -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogle.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}