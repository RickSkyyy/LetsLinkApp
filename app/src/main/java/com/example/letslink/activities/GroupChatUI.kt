// GroupChatActivity.kt
package com.example.letslink.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.R
import com.example.letslink.adapters.MessagesAdapter
import com.example.letslink.models.ChatMessage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.letslink.local_database.GroupDao
import com.example.letslink.local_database.LetsLinkDB
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.view.WindowCompat
import com.example.letslink.Network.PushApiClient
import com.example.letslink.online_database.fb_ChatRepo
import com.example.letslink.online_database.fb_userRepo

class GroupChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessagesAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabMenu: LinearLayout
    private lateinit var groupName : TextView
    private lateinit var groupDao : GroupDao
    private lateinit var chatRepo : fb_ChatRepo
    private lateinit var userRepo : fb_userRepo
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    private var isFabOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var groupname : String = ""
        val currentUser = auth.currentUser
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.fragment_group_chat_u_i) // your layout
        recyclerView = findViewById(R.id.messages_recycler)
        messageInput = findViewById(R.id.message_inputt)

        sendButton = findViewById(R.id.send_button)
        fabMain = findViewById(R.id.fab_main)
        fabMenu = findViewById(R.id.fab_menu)

        adapter = MessagesAdapter(mutableListOf(),currentUser?.uid!!)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        groupDao = LetsLinkDB.getDatabase(this).groupDao()
        chatRepo = fb_ChatRepo()
        userRepo = fb_userRepo()


        val groupID = intent.getStringExtra("groupId")
        Log.d("check-group-id",groupID!!)
        lifecycleScope.launch {
            val group = groupDao.getGroupById(groupID!!)
            groupName = findViewById(R.id.group_name)
            groupName.text = group?.groupName
            groupname = group?.groupName!!
        }
        chatRepo.loadMessages(groupID!!) { messages ->
            Log.d("--check number of messages",messages.size.toString())
            adapter = MessagesAdapter(messages.toMutableList(), currentUser?.uid!!)
            recyclerView.adapter = adapter
            recyclerView.scrollToPosition(adapter.itemCount - 1)
        }



        // Send button
        sendButton.setOnClickListener {
        val message = messageInput.text.toString()
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            if(isInternetAvailable(this)) {
                //fetch group from database online then get the members and send the chat message to them
            chatRepo.getGroupMembers(groupID!!){members->
                if(members.isNotEmpty()){

                    Log.d("check-members",members.size.toString())
                    try {
                        if (message.isEmpty()) {
                            Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show()
                            return@getGroupMembers
                        }
                        Log.d("check-message", message)
                        chatRepo.sendMessage(groupID, message) { success, message ->
                            if (success) {
                                //add to the adapter
                                messageInput.text.clear()
                                Log.d("check-message", message?.message!!)
                            } else {
                                Log.d("check-message", "failed")
                            }
                        }
                        userRepo.getUsersFcmTokens(members) { tokens ->
                            if (tokens.isNotEmpty()) {
                                PushApiClient.sendMessageNotification(
                                    this,
                                    tokens,
                                    groupname,
                                    message
                                )
                            }
                        }
                    }catch(e : Exception){
                        Log.d("check-error",e.toString())
                    }
                }

            }


            }else{
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                //queue the messages in a database "offlineMessage" where it stores the groupID, userID who sent the message and the message itself.
            }

        }

        // FAB toggle
        fabMain.setOnClickListener {
            if (isFabOpen) {
                fabMenu.visibility = LinearLayout.GONE
                fabMain.setImageResource(R.drawable.ic_add)
            } else {
                fabMenu.visibility = LinearLayout.VISIBLE
                fabMain.setImageResource(R.drawable.ic_close)
            }
            isFabOpen = !isFabOpen
        }
    }

    private fun isInternetAvailable(context : Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }

    }
}
