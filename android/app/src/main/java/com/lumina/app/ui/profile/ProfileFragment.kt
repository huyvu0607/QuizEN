package com.lumina.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lumina.app.R
import com.lumina.app.data.repository.AuthRepository
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.FragmentProfileBinding
import com.lumina.app.ui.auth.AuthActivity
import com.lumina.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import java.util.Locale

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val factory = ViewModelFactory(
            userRepository = UserRepository(database.userDao()),
            authRepository = AuthRepository(requireContext().applicationContext),
            sessionManager = SessionManager(requireContext().applicationContext),
            vocabularyDao = database.vocabularyDao()
        )
        factory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.apply {
                    tvDisplayName.text = state.displayName
                    tvEmail.text = state.email
                    tvLevelBadge.text = getString(R.string.level_placeholder, state.level)
                    tvStreakBadge.text = getString(R.string.streak_placeholder, state.streakDays)
                    tvTotalWords.text = String.format(Locale.getDefault(), "%,d", state.totalWords)
                    tvLearningDays.text = state.learningDays.toString()
                    tvTotalXp.text = String.format(Locale.getDefault(), "%,d", state.totalXp)
                    tvXpGoalValue.text = getString(R.string.xp_goal_placeholder, state.xpGoal)
                    tvLanguageLevelValue.text = getString(R.string.lang_level_placeholder, state.level)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}