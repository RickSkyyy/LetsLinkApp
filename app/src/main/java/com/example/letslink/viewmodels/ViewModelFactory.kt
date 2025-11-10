package com.example.letslink.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.letslink.SessionManager
import com.example.letslink.local_database.UserDao

class ViewModelFactory(
    private val dao: UserDao,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(dao) as T
        }
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(dao,sessionManager) as T
        }


        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
