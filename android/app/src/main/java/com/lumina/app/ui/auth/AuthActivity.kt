package com.lumina.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.lumina.app.MainActivity
import com.lumina.app.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AuthActivity là LAUNCHER activity (xem AndroidManifest.xml), nên việc check
        // "đã đăng nhập chưa" phải nằm ở đây. Nếu đã có session Firebase, bỏ qua
        // màn hình đăng nhập/đăng ký, vào thẳng MainActivity.
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
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