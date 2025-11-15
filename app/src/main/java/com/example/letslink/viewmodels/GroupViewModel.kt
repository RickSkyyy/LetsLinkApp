package com.example.letslink.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.example.api_test.GroupState
import com.example.letslink.API_related.GroupRepo
import com.example.letslink.SessionManager
import com.example.letslink.model.Group
import com.example.letslink.local_database.GroupEvent
import com.example.letslink.model.GroupList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class GroupViewModel (
    private val repository: GroupRepo,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _noteState = MutableStateFlow(GroupState())
    val noteState = _noteState.asStateFlow()


    private val currentUserId = MutableStateFlow<UUID?>(null)
    val groupsWithMembers = MutableStateFlow<List<GroupList>>(emptyList())
    fun loadGroupsWithMembers(userId: String) {
        viewModelScope.launch {
            repository.getGroupsWithMemberNames(userId).collect { groups ->
                groupsWithMembers.value = groups
            }
        }
    }
    // groups Flow
    val groups: StateFlow<List<Group>> = currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                // request the repository for the flow of groups
                repository.getGroupsByUserId(userId)
            } else {
                // If no user id, return an empty flow
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // put user id automatically
        val userId: UUID? = sessionManager.getUserId()
        if (userId != null) {
            // Update both the UI state and the internal ID flow
            _noteState.update { it.copy(userId = userId.toString()) }
            currentUserId.value = userId
        }
    }

    /**
     * Handles an invite link by joining the group.
     * The groupId is needed
     */
    fun joinGroupFromInvite(groupId: String) {
        val userID = sessionManager.getUserId()
        if (userID == null) {
            _noteState.update { it.copy(errorMessage = "Error: Cannot join group. User session is invalid.") }
            return
        }

        viewModelScope.launch {
            _noteState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Call the repository to join the group.
                val response = repository.joinGroup(groupId, userID)

                if (response != null) {
                    _noteState.update { it.copy(
                        isLoading = false,
                        errorMessage = null,
                        isSuccess = true
                    ) }
                    println("User ${userID} successfully joined group ${groupId}.")
                } else {
                    _noteState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Failed to join group. Server response was null.",
                        isSuccess = false
                    ) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _noteState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to join group due to: ${e.message}",
                    isSuccess = false
                ) }
            }
        }
    }
    fun joinGroupFromButton(groupId: String) {
        val userID = sessionManager.getUserId()
        if (userID == null) {
            _noteState.update { it.copy(errorMessage = "Error: Cannot join group. User session is invalid.") }
            return
        }

        viewModelScope.launch {
            _noteState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Call the repository to join the group.
                val response = repository.joinGroup(groupId, userID)

                if (response != null) {
                    _noteState.update { it.copy(
                        isLoading = false,
                        errorMessage = null,
                        isSuccess = true
                    ) }
                    println("User ${userID} successfully joined group ${groupId}.")
                } else {
                    _noteState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Failed to join group. Server response was null.",
                        isSuccess = false
                    ) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _noteState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to join group due to: ${e.message}",
                    isSuccess = false
                ) }
            }
        }
    }
    fun sendPersonalizedInvite(recipientUsername: String, group: Group) {
        viewModelScope.launch {
            println("DEBUG: sendPersonalizedInvite called with username: '$recipientUsername', group: ${group.groupName}")

            val result = repository.assignInvite(
                recipientUsername,
                group.groupId.toString(),
                group.groupName,
                group.description
            )

            println("DEBUG: assignInvite result: $result")
        }
    }
    suspend fun getRecipientId(username: String): String? {
        // Try local Room DB first (fastest)
        val localId = repository.getRecipientIdFromRoom(username)
        if (localId != null) return localId

        //  If not local, search the Firebase Realtime Database
        return repository.getRecipientIdFromFirebase(username)
    }
    fun onEvent(event: GroupEvent)
    {
        when(event){
            GroupEvent.createNote -> {
                val userID = sessionManager.getUserId()
                val title = noteState.value.groupName
                val description = noteState.value.description

                // FIX: Check if userID is null properly
                if(userID == null || title.isBlank() || description.isBlank())
                {
                    if(userID == null){
                        _noteState.update { it.copy(errorMessage = "Error: Current user is not authorised to enter notes") }
                        return
                    }
                    _noteState.update { it.copy(errorMessage = "Error regarding entering notes") }
                    return
                }

                // FIX: Use the non-null userID variable
                val newGroup = Group(
                    groupId = UUID.randomUUID().toString(),
                    userId = userID.toString(),
                    groupName = title,
                    description = description
                )

                viewModelScope.launch {
                    try {
                        _noteState.update { it.copy(isLoading = true, errorMessage = null) }

                        val apiResponse = repository.createAndSyncGroup(newGroup)
                        Log.d("GroupViewModel", "API Response: $apiResponse")
                        if (apiResponse != null) {
                            _noteState.update { it.copy(
                                userId = "",
                                groupName = "",
                                description = "",
                                isSuccess = true,
                                isLoading = false,
                                inviteLink = apiResponse.inviteLink
                            ) }
                        } else {
                            _noteState.update { it.copy(
                                errorMessage = "Group saved locally but failed to generate invite link.",
                                isSuccess = false,
                                isLoading = false,
                                inviteLink = null
                            ) }
                        }
                    }
                    catch (e: Exception){
                        e.printStackTrace()
                        _noteState.update { it.copy(
                            errorMessage = "Note feature failed due to: ${e.message}",
                            isSuccess = false,
                            isLoading = false
                        ) }
                    }
                }
            }
            is GroupEvent.setDecription -> {
                _noteState.update { it.copy(
                    description  = event.decription,
                    errorMessage = null
                ) }
            }
            is GroupEvent.setTitle -> {
                _noteState.update { it.copy(
                    groupName = event.title,
                    errorMessage = null
                )}
            }
            is GroupEvent.setUserID -> {
                _noteState.update { it.copy(
                    userId = event.userId,
                    errorMessage = null
                ) }
            }
            is GroupEvent.deleteNotes -> {
                viewModelScope.launch {
                    TODO()
                }
            }
        }
    }

    companion object {
        fun provideFactory(repository: GroupRepo, sessionManager: SessionManager): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")

            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GroupViewModel(repository,sessionManager) as T
            }
        }
    }
}