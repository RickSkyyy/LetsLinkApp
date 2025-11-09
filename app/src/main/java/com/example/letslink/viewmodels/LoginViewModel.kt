package com.example.letslink.viewmodels

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.letslink.local_database.UserDao
import com.example.letslink.API_related.UUIDConverter
import com.example.letslink.SessionManager
import com.example.letslink.model.LoginEvent
import com.example.letslink.model.LoginState
import com.example.letslink.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import com.google.firebase.database.FirebaseDatabase
//Acts as a bridge to allow whatever is inputted in the ui to be written to room (Lackner, 2025b)
class LoginViewModel(private val dao: UserDao, private val sessionManager: SessionManager) : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var uuidConverter : UUIDConverter

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()
    var _loggedInUser: User? = null


    ////hash passwords (SSOJet ,2022)
    fun hasPass(hashPassword : String): String
    {
        val bytes = hashPassword.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        return digest.fold("") { str, byte -> str + "%02x".format(byte) }
    }
    fun verifyPassword(providedPassword: String, storedHash: String): Boolean {
        val providedPasswordHash = hasPass(providedPassword)
        return providedPasswordHash == storedHash
    }
    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.checkEmail -> {
                _loginState.update { it.copy(email = event.email, errorMessage = null) }
            }
            is LoginEvent.checkPassword -> {
                _loginState.update { it.copy(password = event.password, errorMessage = null) }
            }
            LoginEvent.Login -> {
                attemptLogin()
            }

            is LoginEvent.GoogleLogin -> siginInWithGoogle(event.idToken)
            is LoginEvent.LoginFailed -> _loginState.update{it.copy(errorMessage = event.message)}
        }
    }
    private fun siginInWithGoogle(idToken:String){
        _loginState.update{it.copy(isLoading = true, errorMessage = null)}
        uuidConverter = UUIDConverter()
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        viewModelScope.launch{
            try {
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _loginState.update {
                                it.copy(
                                    email = task.result.user?.email!!,
                                    isLoading = false,
                                    isSuccess = true,
                                    errorMessage = null
                                )
                            }
                            val id = task.result.user?.uid
                            val name = task.result.user?.displayName
                            val email = task.result.user?.email

                            if (name != null && email != null) {
                                _loggedInUser?.userId = id!!
                                _loggedInUser?.firstName = name
                                _loggedInUser?.email = email

                                sessionManager.saveUserSession(userId = id.toString(),email,name)

                            }

                        } else {
                            _loginState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = task.exception?.message
                                )
                            }
                        }
                    }

            } catch (e:Exception){
                _loginState.update { it.copy(isLoading = false, isSuccess = false, errorMessage = e.message) }
                Log.d("LoginViewModel", "Google login failed with exception: ${e.message}")
            }

        }
    }
    private fun attemptLogin() {
        val state = loginState.value
        Log.d("LoginViewModel", "Attempting login for user: ${state.email}")

        if (state.email.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Email is required") }
            Log.d("LoginViewModel", "Email is blank.")
            return
        }

        if (state.password.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Password is required") }
            Log.d("LoginViewModel", "Password is blank.")
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // First try Firebase Authentication
                Log.d("LoginViewModel", "Attempting Firebase authentication...")

                firebaseAuth.signInWithEmailAndPassword(state.email, state.password)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            // Firebase authentication successful
                            val firebaseUser = authTask.result.user
                            Log.d("LoginViewModel", "Firebase authentication successful for: ${firebaseUser?.email}")

                            // Now get user profile from Realtime Database
                            val userId = firebaseUser?.uid
                            if (userId != null) {
                                getUserFromFirebaseDatabase(userId, state.email)
                            sessionManager.saveUserSession(userId,state.email,state.name)
                            } else {
                                _loginState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "User ID not found"
                                    )
                                }
                            }
                        } else {
                            // Firebase authentication failed
                            Log.d("LoginViewModel", "Firebase auth failed: ${authTask.exception?.message}")

                            // Fallback to local database check
                            checkLocalDatabase(state.email, state.password)
                        }
                    }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login attempt failed with exception: ${e.message}")
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Login failed: ${e.message}"
                    )
                }
            }
        }
    }
    private fun getUserFromFirebaseDatabase(firebaseUid: String, email: String) {
        Log.d("LoginViewModel", "Searching for user by email: $email")

        val query = database.child("users").orderByChild("email").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userEntry = snapshot.children.firstOrNull()
                    Log.d("LoginViewModel", "Firebase data: ${userEntry?.value}")

                    // MANUAL EXTRACTION - don't use automatic parsing
                    val userData = userEntry?.value as? Map<String, Any>

                    if (userData != null) {
                        // Handle the userId whether it's a string or UUID object
                        val userIdValue = userData["userId"]
                        val actualUserId = when (userIdValue) {
                            is String -> userIdValue
                            is Map<*, *> -> {
                                // It's stored as UUID object - extract the bits
                                val bits = userIdValue as Map<String, Long>
                                val mostSigBits = bits["mostSignificantBits"] ?: 0L
                                val leastSigBits = bits["leastSignificantBits"] ?: 0L
                                java.util.UUID(mostSigBits, leastSigBits).toString()
                            }
                            else -> userEntry.key ?: "" // Fallback to database key
                        }

                        val user = User(
                            userId = actualUserId,
                            firstName = userData["firstName"] as? String ?: "",
                            password = userData["password"] as? String ?: "",
                            dateOfBirth = userData["dateOfBirth"] as? String ?: "",
                            email = userData["email"] as? String ?: email,
                            fcmToken = userData["fcmToken"] as? String ?: "",
                            liveLocation = userData["liveLocation"] as? String ?: ""
                        )

                        Log.d("LoginViewModel", "User found with ID: $actualUserId")

                        _loggedInUser = user
                        _loginState.update {
                            it.copy(
                                userId = actualUserId,
                                name = user.firstName,
                                email = email,
                                isLoading = false,
                                isSuccess = true,
                                errorMessage = null
                            )
                        }

                        viewModelScope.launch {
                            syncUserToLocalDatabase(user)
                        }
                    }
                } else {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Account not found. Please register first."
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LoginViewModel", "Firebase Database error: ${error.message}")
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Database error: ${error.message}"
                    )
                }
            }
        })
    }

    private fun checkLocalDatabase(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Falling back to local database check...")
                var user = dao.getUserByEmail(email)

                if (user == null && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    user = dao.getUserByEmail(email)
                    Log.d("LoginViewModel", "Initial username lookup failed. Attempting lookup by email.")
                }

                if (user == null) {
                    Log.d("LoginViewModel", "User lookup failed. User not found.")
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Invalid email or password"
                        )
                    }
                    return@launch
                }

                Log.d("LoginViewModel", "User lookup successful in local database: ${user.email}")

                if (!verifyPassword(password, user.password)) {
                    Log.d("LoginViewModel", "Password mismatch.")
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Invalid email or password"
                        )
                    }
                    return@launch
                }

                Log.d("LoginViewModel", "Password match successful. Login complete.")
                _loggedInUser = user
                _loginState.update {
                    it.copy(
                        userId = user.userId,
                        name = user.firstName,
                        email = user.email,
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Local database login failed: ${e.message}")
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Login failed: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun syncUserToLocalDatabase(user: User) {
        try {
            val existingUser = dao.getUserByEmail(user.email)
            if (existingUser == null) {
                dao.upsertUser(user)
                Log.d("LoginViewModel", "User synced to local database: ${user.email}")
            } else {
                dao.upsertUser(user)
                Log.d("LoginViewModel", "User updated in local database: ${user.email}")
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Failed to sync user to local database: ${e.message}")
        }
    }


    class LoginViewModelFactory(private val userDao: UserDao, private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(userDao,sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}