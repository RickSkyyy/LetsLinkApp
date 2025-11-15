package com.example.letslink.API_related

import android.util.Log
import android.widget.TextView
import com.example.letslink.R
import com.example.letslink.SessionManager
import com.example.letslink.adapter.GroupDiffCallback
import com.example.letslink.local_database.GroupDao
import com.example.letslink.local_database.UserDao
import com.example.letslink.model.Group
import com.example.letslink.model.GroupList
import com.example.letslink.model.Invites
import com.example.letslink.adapter.MembersAdapter
import com.example.letslink.model.GroupResponse
import com.example.letslink.model.InviteRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 *
 * This class hanndels coordination between the local Room database via GroupDao and the
 * LetsLinkAPI.
 * //(Philipp Lackner ,2021)
 *
 *
 */
class GroupRepo(
    private val groupDao: GroupDao,
    private val groupApiService: LetsLinkAPI,
    private val sessionManager: SessionManager,
    private val db: DatabaseReference,
    private val userDao: UserDao
) {
    private lateinit var membersAdapter : MembersAdapter
    /**
     * Fetches groups for a user by first syncing with Firebase, then returning local data
     * This ensures groups are available offline after initial sync
     */
    fun getGroupsByUserId(userId: UUID): Flow<List<Group>> {
        CoroutineScope(Dispatchers.IO).launch {
            syncGroupsWithFirebase(userId.toString())
        }
        return groupDao.getNotesByUserId(userId.toString())
    }

    /**
     * Fetches groups directly from Firebase where user is a member
     * Provides real-time updates from Firebase database
     */
    fun getGroupsFromFirebase(userId: String): Flow<List<Group>> = callbackFlow {
        val groupsRef = db.child("groups")

        // Query by creator (userId field)
        val query = groupsRef.orderByChild("userId").equalTo(userId)

        val listener = query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupsList = mutableListOf<Group>()

                snapshot.children.forEach { groupSnapshot ->
                    val groupData = groupSnapshot.getValue(Group::class.java)
                    groupData?.let { group ->
                        val groupIdString = groupSnapshot.key ?: group.groupId.toString()
                        try {
                            group.groupId = UUID.fromString(groupIdString).toString()
                        } catch (e: IllegalArgumentException) {
                            println("WARNING: Invalid group ID format: $groupIdString")
                        }
                        groupsList.add(group)
                    }
                }

                println("Fetched ${groupsList.size} groups from Firebase for user: $userId")
                trySend(groupsList)
            }

            override fun onCancelled(error: DatabaseError) {
                println("ERROR: Firebase groups fetch failed: ${error.message}")
                close(error.toException())
            }
        })

        awaitClose {
            groupsRef.removeEventListener(listener)
        }
    }
    /**
     * Syncs groups from Firebase to local Room database
     * Ensures offline availability of user's groups
     */
    suspend fun syncGroupsWithFirebase(userId: String) {
        try {
            println("DEBUG: Starting Firebase sync for user: $userId")
            val groupsRef = db.child("groups")
            val query = groupsRef.orderByChild("userId").equalTo(userId)
            val snapshot = query.get().await()

            println("DEBUG: Firebase query returned ${snapshot.childrenCount} groups")

            val firebaseGroups = mutableListOf<Group>()

            snapshot.children.forEach { groupSnapshot ->
                println("DEBUG: Processing group: ${groupSnapshot.key}")
                val groupData = groupSnapshot.getValue(Group::class.java)
                if (groupData != null) {
                    println("DEBUG: Group data: $groupData")
                    val groupIdString = groupSnapshot.key ?: groupData.groupId.toString()
                    try {
                        groupData.groupId = UUID.fromString(groupIdString).toString()

                        // Preserve the invite link from Firebase
                        val existingGroup = groupDao.getGroupById(groupData.groupId)
                        if (existingGroup != null && existingGroup.inviteLink != null) {
                            groupData.inviteLink = existingGroup.inviteLink
                        }

                        firebaseGroups.add(groupData)
                    } catch (e: IllegalArgumentException) {
                        println("WARNING: Invalid group ID format: $groupIdString")
                    }
                }
            }

            println("DEBUG: Found ${firebaseGroups.size} groups to sync")

            firebaseGroups.forEach { group ->
                groupDao.insertGroup(group)
                println("DEBUG: Inserted group: ${group.groupName} with invite link: ${group.inviteLink}")
            }

            println("Synced ${firebaseGroups.size} groups from Firebase to local DB for user: $userId")

        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR: Failed to sync groups from Firebase: ${e.message}")
        }
    }
    suspend fun getRecipientIdFromRoom(username: String): String? {
        return userDao.getUserByUsername(username)?.userId
    }

    suspend fun getRecipientIdFromFirebase(firstName: String): String? {
        println("DEBUG: Searching for user with firstName: '$firstName'")
        val usersRef = db.child("users")

        val query = usersRef.orderByChild("firstName").equalTo(firstName)

        return try {
            val snapshot = query.get().await()
            println("DEBUG: Firebase user query result - exists: ${snapshot.exists()}, children count: ${snapshot.childrenCount}")

            snapshot.children.forEach { userSnapshot ->
                println("DEBUG: Found user - key: ${userSnapshot.key}, value: ${userSnapshot.value}")
            }

            if (snapshot.exists()) {
                val userId = snapshot.children.firstOrNull()?.key
                println("DEBUG: Found user ID: $userId")
                userId
            } else {
                println("DEBUG: No user found with firstName: '$firstName'")

                // Debug: Check what users actually exist in Firebase
                val allUsers = usersRef.get().await()
                println("DEBUG: All users in Firebase:")
                allUsers.children.forEach { user ->
                    println("DEBUG: User ${user.key}: ${user.child("firstName").getValue(String::class.java)}")
                }

                null
            }
        } catch (e: Exception) {
            println("DEBUG: Error searching Firebase: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Handles the creation of a group by saving it locally first,
     * then synchronizing with the remote API, and then updating it
     */
    suspend fun createAndSyncGroup(group: Group): GroupResponse? {
        val currentUserId = sessionManager.getUserId()

        val apiRequest = GroupRequest(
            groupId = group.groupId.toString(),
            userId = currentUserId.toString(),
            description = group.description,
            groupName = group.groupName,
        )

        println("DEBUG: Sending API request: $apiRequest")

        return try {
            val response = groupApiService.createGroup(apiRequest)

            // Update the group with the invite link before saving to local database
            val updatedGroup = group.copy(inviteLink = response.inviteLink)
            groupDao.insertGroup(updatedGroup)

            println("Group synced successfully. Invite link saved to local DB: ${response.inviteLink}")
            response
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR: Failed to sync group ${group.groupId} with API.")
            null
        }
    }

    /**
     * Attempts to join an existing group via the API and a new group
     *
     *
     */
    suspend fun joinGroup(groupId: String, userID: UUID?): GroupResponse? {
        val currentUserId = userID?.toString() ?: sessionManager.getUserId()

        if (currentUserId == null) {
            println("ERROR: No valid user ID found for joining group")
            return null
        }

        val apiRequest = JoinGroupRequest(
            groupId = groupId,
            userId = UUID.fromString(currentUserId.toString())
        )

        return try {
            val response = groupApiService.joinGroup(apiRequest)

            println("DEBUG API RESPONSE: $response")

            val newGroup = Group(
                groupId = response.groupId.toString(),
                userId = currentUserId.toString(),
                groupName = response.groupName,
                description = response.description,
                inviteLink = response.inviteLink
            )

            groupDao.insertGroup(newGroup)

            println("Group joined successfully: ${response.groupName}")
            response
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR: Failed to join group $groupId. ${e.message}")
            null
        }
    }
    fun getReceivedInvites(userId: String): Flow<List<Invites>> = callbackFlow {
        val invitesRef = db.child("users").child(userId).child("receivedInvites")

   val typeIndicator = object : GenericTypeIndicator<Map<String, Invites>>() {}

        val listener = invitesRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                //  Read the entire collection
                val invitesMap: Map<String, Invites>? = snapshot.getValue(typeIndicator)

                // Convert List<Invites>
                val invitesList: List<Invites> = invitesMap?.values?.toList() ?: emptyList()

                trySend(invitesList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })

        awaitClose {
            invitesRef.removeEventListener(listener)
        }
    }

    fun getGroupsWithMemberNames(userId: String): Flow<List<GroupList>> = callbackFlow {
        val groupsRef = db.child("groups")
        val listener = groupsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupsList = mutableListOf<GroupList>()
                snapshot.children.forEach { groupSnapshot ->
                    val groupData = groupSnapshot.getValue(GroupList::class.java)
                    groupData?.let { groups ->
                        groups.groupId = groupSnapshot.key ?: ""
                        // This correctly resolves member IDs to names
                        fetchMemberNames(groups.members) { memberNames ->
                            val updatedGroup = groups.copy(
                                groupId = groupSnapshot.key ?: "",
                                members = memberNames
                            )
                            groupsList.add(updatedGroup)
                            if (groupsList.size == snapshot.children.count()) {
                                trySend(groupsList)
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { groupsRef.removeEventListener(listener) }
    }
    private fun fetchMemberNames(memberIds: List<String>, callback: (List<String>) -> Unit) {
        val memberNames = mutableListOf<String>()
        val database = FirebaseDatabase.getInstance().reference
        var completedRequests = 0

        if (memberIds.isEmpty()) {
            callback(emptyList())
            return
        }
        Log.d("MEMBER DEBUG", "Fetching names for ${memberIds.size} members: $memberIds")

        memberIds.forEach { memberId ->
            database.child("users").child(memberId).get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val userData = snapshot.value
                        val firstName = when (userData) {
                            is Map<*, *> -> userData["firstName"] as? String
                            else -> null
                        }
                        Log.d("MEMBER DEBUG", "Extracted firstName: $firstName")
                        if (!firstName.isNullOrEmpty()) {
                            memberNames.add(firstName)
                        } else {
                            val email = when (userData) {
                                is Map<*, *> -> userData["email"] as? String
                                else -> null
                            }
                            memberNames.add(email ?: "Unknown User")
                        }
                    } catch (e: Exception) {
                        Log.e("GroupDetailsF", "Error parsing user data: ${e.message}")
                        memberNames.add("Unknown User")
                    }

                    completedRequests++
                    if (completedRequests == memberIds.size) {
                        Log.d("MEMBER DEBUG", "All requests completed final member names: $memberNames")
                        callback(memberNames)
                    }
                }
                .addOnFailureListener { error ->
                    Log.e("DEBUG", "Failed to fetch user data for $memberId: ${error.message}")
                    memberNames.add("Unknown User")
                    completedRequests++
                    if (completedRequests == memberIds.size) {
                        callback(memberNames)
                    }
                }
        }
    }
    suspend fun assignInvite(
        recipientId: String,
        groupId: String,
        groupName: String,
        description: String
    ) {
        println("DEBUG: assignInvite called with recipientId: '$recipientId', groupId: '$groupId'")

        // First try to find the user in Firebase
        val foundUserId = getRecipientIdFromFirebase(recipientId)
        println("DEBUG: Found user ID from Firebase: $foundUserId")

        if (foundUserId == null) {
            println("DEBUG: User '$recipientId' not found in Firebase")
            return
        }

        val apiRequest = InviteRequest(
            groupId = groupId,
            userId = foundUserId,
            groupName = groupName,
            description = description
        )

        try {
            groupApiService.assignInviteToUser(apiRequest)
            println("Invite for group $groupId successfully assigned to user $foundUserId.")
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR: Failed to assign invite to user $foundUserId.")
        }
    }

}