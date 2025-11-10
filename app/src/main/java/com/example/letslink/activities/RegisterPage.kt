package com.example.letslink.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.letslink.R
import com.example.letslink.SessionManager
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.model.User
import com.example.letslink.model.UserEvent
import com.example.letslink.online_database.fb_userRepo
import com.example.letslink.viewmodels.UserViewModel
import com.example.letslink.viewmodels.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class RegisterPage : AppCompatActivity() {

    private lateinit var viewModel: UserViewModel
    private val fbUserRepo = fb_userRepo()
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_page)
        sessionManager = SessionManager(this)
        // Initialize ViewModel
        val dao = LetsLinkDB.getDatabase(applicationContext).userDao()
        val factory = ViewModelFactory(dao,sessionManager)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        val signInButton: TextView = findViewById(R.id.sign_in_link)
        val signUpButton: MaterialButton = findViewById(R.id.sign_up_button)
        val txtDateOfBirth = findViewById<TextView>(R.id.txt_date_of_birth)

        // Go to Login
        signInButton.setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
        }

        // Date Picker
        txtDateOfBirth.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date of birth")
                .setTheme(R.style.MyMaterialCalendarTheme)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(supportFragmentManager, "DATE_PICKER")

            // When date is selected
            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                val formattedDate =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                txtDateOfBirth.text = formattedDate
            }

            // Modify appearance once the dialog is shown
            supportFragmentManager.executePendingTransactions()
            datePicker.addOnDismissListener {
                // Re-run styling each time dialog opens
                datePicker.dialog?.window?.decorView?.post {
                    val root = datePicker.dialog?.window?.decorView ?: return@post
                    val orange = ContextCompat.getColor(this, R.color.orange_primary)
                    val midnight = ContextCompat.getColor(this, R.color.midnight)

                    // Main background
                    root.findViewById<View>(
                        com.google.android.material.R.id.mtrl_calendar_main_pane
                    )?.setBackgroundColor(midnight)

                    // Header (month/year)
                    val headerId = root.resources.getIdentifier(
                        "mtrl_calendar_header_title", "id", "com.google.android.material"
                    )
                    val headerText =
                        headerId.takeIf { it != 0 }?.let { root.findViewById<TextView>(it) }
                    headerText?.setTextColor(orange)

                    // Weekdays background
                    val weekDaysId = root.resources.getIdentifier(
                        "mtrl_calendar_days_of_week", "id", "com.google.android.material"
                    )
                    val weekDays =
                        weekDaysId.takeIf { it != 0 }?.let { root.findViewById<View>(it) }
                    weekDays?.setBackgroundColor(midnight)

                    // Make all text (numbers, weekdays, etc.) orange
                    val allViews = ArrayList<View>()
                    root.findViewsWithText(allViews, "", View.FIND_VIEWS_WITH_TEXT)
                    allViews.forEach { if (it is TextView) it.setTextColor(orange) }
                }
            }
        }

        // Sign Up button logic
        signUpButton.setOnClickListener {
            val txtFullName: TextView = findViewById(R.id.txt_full_name)
            val txtEmail: TextView = findViewById(R.id.txt_email)
            val txtPassword: TextView = findViewById(R.id.txt_password)
            val txtConfirmPassword: TextView = findViewById(R.id.txt_confirm_password)
            val txtDOB: TextView = findViewById(R.id.txt_date_of_birth)

            val fName = txtFullName.text.toString()
            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString().trim()
            val confirmPassword = txtConfirmPassword.text.toString().trim()
            val dateOfBirth = txtDOB.text.toString().trim()

            if (fName.isEmpty() || email.isEmpty() || password.isEmpty() ||
                confirmPassword.isEmpty() || dateOfBirth.isEmpty()
            ) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // Save locally
                viewModel.onEvent(UserEvent.setFirstName(fName))
                viewModel.onEvent(UserEvent.setEmail(email))
                viewModel.onEvent(UserEvent.setPassword(password))
                viewModel.onEvent(UserEvent.setDateOfBirth(dateOfBirth))
                viewModel.onEvent(UserEvent.createUser)

                lifecycleScope.launch {
                    viewModel.userState.collect { state ->
                        if (state.isSuccess) {
                            val user = viewModel.getUserByEmail(email)
                            if (user != null) {
                                Toast.makeText(
                                    this@RegisterPage,
                                    "Account created successfully (local)",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val newUser = User(
                                    user.userId, fName, password, dateOfBirth, email, null
                                )

                                // Save remotely on Firebase
                                fbUserRepo.register(newUser) { success, errorMessage, _ ->
                                    if (success) {
                                        Toast.makeText(
                                            this@RegisterPage,
                                            "Account created successfully (remote)",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(
                                            Intent(
                                                this@RegisterPage,
                                                LoginPage::class.java
                                            )
                                        )
                                    } else {
                                        Toast.makeText(
                                            this@RegisterPage,
                                            "Firebase error: $errorMessage",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("RegisterPage", "Error: ${e.message}")
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
