package com.lumina.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.lumina.app.MainActivity
import com.lumina.app.data.repository.UserRepository
import com.lumina.app.data.source.local.AppDatabase
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.databinding.ActivityAuthBinding
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(applicationContext)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val database = AppDatabase.getInstance(applicationContext)

        // Kiểm tra xem user có thực sự tồn tại trong Room không
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val userInDb = if (userId != -1L) database.userDao().getUserById(userId) else null

            // Chỉ vào MainActivity khi:
            // 1. Firebase đã login
            // 2. SessionManager có ID
            // 3. Quan trọng nhất: User đó phải có trong Database local (Room)
            if (firebaseUser != null && sessionManager.isLoggedIn()) {
                val userIdToSave: Long
                val emailToSave: String
                
                if (userInDb == null) {
                    // Nếu user có trong Firebase nhưng chưa có trong Room (ví dụ mới cài lại app)
                    // Ta tạo lại bản ghi User local từ thông tin Firebase
                    val userRepository = UserRepository(database.userDao())
                    val newUser = userRepository.upsertFromAuth(
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "Người dùng",
                        avatarUrl = firebaseUser.photoUrl?.toString()
                    )
                    userIdToSave = newUser.id
                    emailToSave = newUser.email
                } else {
                    userIdToSave = userInDb.id
                    emailToSave = userInDb.email
                }

                sessionManager.saveSession(userIdToSave, emailToSave, firebaseUser.uid)
                startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                finish()
            } else {
                // Nếu dữ liệu không đồng bộ, xóa hết session để bắt đăng nhập lại từ đầu
                if (firebaseUser != null || sessionManager.isLoggedIn()) {
                    FirebaseAuth.getInstance().signOut()
                    sessionManager.clearSession()
                }
                
                binding = ActivityAuthBinding.inflate(layoutInflater)
                setContentView(binding.root)
                setupViewPager()
            }
        }
    }

    private fun setupViewPager() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> LoginFragment()
                    else -> RegisterFragment()
                }
            }
        }
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Đăng nhập"
                else -> "Đăng ký"
            }
        }.attach()
    }
}
