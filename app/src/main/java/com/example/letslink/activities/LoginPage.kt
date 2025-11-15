package com.example.letslink.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.letslink.R
import com.example.letslink.SessionManager
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.local_database.UserDao
import com.example.letslink.model.LoginEvent
import com.example.letslink.nav.HorizontalCoordinator
import com.example.letslink.viewmodels.LoginViewModel
import com.example.letslink.viewmodels.UserViewModel
import com.example.letslink.viewmodels.ViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.*

class LoginPage : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var userViewModel: UserViewModel

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    private lateinit var sessionManager: SessionManager

    // Biometric authentication
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var biometricLoginButton: MaterialButton

    companion object {
        private const val SHARED_PREFS_NAME = "LetsLinkPrefs"
        private const val REMEMBER_ME_KEY = "remember_me"
        private const val SAVED_EMAIL_KEY = "saved_email"
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", "en") ?: "en"
        val locale = Locale(lang)
        val context = updateBaseContextLocale(newBase, locale)
        super.attachBaseContext(context)
    }

    private fun updateBaseContextLocale(context: Context, locale: Locale): Context {
        val config = Configuration(context.resources.configuration)
        Locale.setDefault(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = android.os.LocaleList(locale)
            android.os.LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        } else {
            config.setLocale(locale)
        }
        return context.createConfigurationContext(config)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sessionManager = SessionManager(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<SignInButton>(R.id.google_sign_in_btn).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        val dao: UserDao = LetsLinkDB.getDatabase(applicationContext).userDao()
        val factory = ViewModelFactory(dao,sessionManager,this)
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        val hyperLinkForgotPassword: TextView = findViewById(R.id.forgot_password_link)
        val signInButton: MaterialButton = findViewById(R.id.sign_in_button)
        val signUpLink: TextView = findViewById(R.id.sign_up_link)

        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)

        // Initialize biometric components
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox)
        biometricLoginButton = findViewById(R.id.biometric_login_button)
        initBiometricAuth()
        checkBiometricLogin()
        loadSavedPreferences(emailEditText)

        hyperLinkForgotPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordPage::class.java)
            startActivity(intent)
        }

        var searchEmail = ""
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            searchEmail = email

            saveRememberMePreference(rememberMeCheckbox.isChecked, email)

            loginViewModel.onEvent(LoginEvent.checkEmail(email))
            loginViewModel.onEvent(LoginEvent.checkPassword(password))
            loginViewModel.onEvent(LoginEvent.Login)
        }

        biometricLoginButton.setOnClickListener {
            showBiometricPrompt()
        }

        lifecycleScope.launch {
            loginViewModel.loginState.collect { state ->
                if (state.isSuccess) {
                    val user = userViewModel.getUserByEmail(state.email)
                    if (user != null) {
                        Log.d("login-email-check", user.email)
                        sessionManager.saveUserSession(user.userId, user.email, user.firstName)
                        Log.d(
                            "LoginPage",
                            "User session saved: ${user.userId}, ${user.email}, ${user.firstName}"
                        )
                        val intent = Intent(this@LoginPage, HorizontalCoordinator::class.java)
                        startActivity(intent)
                        finish()
                    }

                }

                signUpLink.setOnClickListener {
                    val intent = Intent(this@LoginPage, RegisterPage::class.java)
                    startActivity(intent)
                }

                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                    )
                    insets
                }
            }
        }
    }

    private fun initBiometricAuth() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            Log.d("Biometric", getString(R.string.lp9_user_chose_password_login))
                        }
                        else -> {
                            Toast.makeText(this@LoginPage, getString(R.string.lp9_authentication_error, errString), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    performAutoLogin()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginPage, getString(R.string.lp9_authentication_failed), Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.lp9_biometric_login_title))
            .setSubtitle(getString(R.string.lp9_biometric_login_subtitle))
            .setNegativeButtonText(getString(R.string.lp9_use_password))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
    }

    private fun checkBiometricLogin() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val sharedPref = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
                if (sharedPref.getBoolean(REMEMBER_ME_KEY, false)) {
                    biometricLoginButton.visibility = View.VISIBLE
                }
            }
            else -> {
                biometricLoginButton.visibility = View.GONE
            }
        }
    }

    private fun showBiometricPrompt() {
        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.lp9_biometric_auth_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    private fun performAutoLogin() {
        val sharedPref = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val savedEmail = sharedPref.getString(SAVED_EMAIL_KEY, "")

        if (!savedEmail.isNullOrEmpty()) {
            lifecycleScope.launch {
                val user = userViewModel.getUserByEmail(savedEmail)
                if (user != null) {
                    sessionManager.saveUserSession(user.userId, user.email, user.firstName)
                    Toast.makeText(this@LoginPage, getString(R.string.lp9_welcome_back, user.firstName), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginPage, HorizontalCoordinator::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginPage, getString(R.string.lp9_user_account_not_found), Toast.LENGTH_SHORT).show()
                    biometricLoginButton.visibility = View.GONE
                }
            }
        } else {
            Toast.makeText(this@LoginPage, getString(R.string.lp9_no_saved_login), Toast.LENGTH_SHORT).show()
            biometricLoginButton.visibility = View.GONE
        }
    }

    private fun saveRememberMePreference(rememberMe: Boolean, email: String) {
        val sharedPref = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(REMEMBER_ME_KEY, rememberMe)
            if (rememberMe) {
                putString(SAVED_EMAIL_KEY, email)
                checkBiometricLogin()
            } else {
                remove(SAVED_EMAIL_KEY)
                biometricLoginButton.visibility = View.GONE
            }
            apply()
        }
    }

    private fun loadSavedPreferences(emailEditText: EditText) {
        val sharedPref = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val rememberMe = sharedPref.getBoolean(REMEMBER_ME_KEY, false)
        val savedEmail = sharedPref.getString(SAVED_EMAIL_KEY, "")

        rememberMeCheckbox.isChecked = rememberMe
        if (!savedEmail.isNullOrEmpty()) {
            emailEditText.setText(savedEmail)
        }
    }

    // Shakes the text fields and shows toast
    private fun showLoginError(emailEditText: EditText, passwordEditText: EditText) {
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        emailEditText.startAnimation(shake)
        passwordEditText.startAnimation(shake)
        Toast.makeText(this, getString(R.string.lp9_user_not_found_incorrect_credentials), Toast.LENGTH_SHORT)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)
                loginViewModel.onEvent(LoginEvent.GoogleLogin(account.idToken!!))
            } catch (e: Exception) {
                loginViewModel.onEvent(LoginEvent.LoginFailed(getString(R.string.lp9_google_sign_in_failed)))
            }
        }
    }
}