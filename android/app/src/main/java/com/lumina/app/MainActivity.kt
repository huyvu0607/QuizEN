package com.lumina.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// LƯU Ý: Đoạn check "đã đăng nhập chưa" đã được CHUYỂN sang AuthActivity.kt,
// vì AuthActivity mới là activity LAUNCHER thật sự (xem AndroidManifest.xml),
// MainActivity chỉ được mở SAU KHI AuthActivity xác nhận user đã đăng nhập.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}