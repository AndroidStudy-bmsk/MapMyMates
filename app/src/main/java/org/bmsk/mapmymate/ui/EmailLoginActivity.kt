package org.bmsk.mapmymate.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import org.bmsk.mapmymate.R
import org.bmsk.mapmymate.databinding.ActivityEmailLoginBinding

class EmailLoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityEmailLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmailLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.doneButton.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty()) {
                val data = Intent().apply {
                    putExtra("email", binding.emailEditText.text.toString())
                }
                setResult(RESULT_OK, data)
            } else {
                Toast.makeText(this, getString(R.string.do_input_email), Toast.LENGTH_SHORT).show()
            }
        }
    }
}