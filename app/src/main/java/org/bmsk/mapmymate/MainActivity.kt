package org.bmsk.mapmymate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kakao.sdk.common.util.Utility
import org.bmsk.mapmymate.databinding.ActivityMainBinding
import org.bmsk.mapmymate.ui.LoginActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startActivity(Intent(this, LoginActivity::class.java))
    }
}