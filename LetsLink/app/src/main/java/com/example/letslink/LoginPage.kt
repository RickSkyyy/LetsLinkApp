package com.example.letslink

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.letslink.nav.HorizontalCoordinator
import com.google.android.material.button.MaterialButton

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)


        val hyperLinkForgotPassword: TextView = findViewById(R.id.forgot_password_link)
        val signInButton: MaterialButton = findViewById(R.id.sign_in_button)
        val signUpLink: TextView = findViewById(R.id.sign_up_link)

        hyperLinkForgotPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordPage::class.java)
            startActivity(intent)
        }

        signInButton.setOnClickListener {
            val intent = Intent(this, HorizontalCoordinator::class.java)
            startActivity(intent)
        }

        signUpLink.setOnClickListener {
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }






        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}