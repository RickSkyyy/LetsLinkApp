package com.example.letslink.online_database

import android.util.Log
import com.example.letslink.model.Chat
import com.example.letslink.model.GroupResponse
import com.example.letslink.model.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class fb_ChatRepo {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun createChat(groupID:String, callback : (Boolean, Chat?) -> Unit){
        checkExistingChat(groupID){eChat->
            if(eChat != null){
                Log.d("repo-check","chat already exists")
                callback(true,eChat)
            }else{
                //create a chat for this group
                database.child("chats").child(groupID).setValue(Chat(groupID = groupID))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("repo-check", "chat created")
                            //fetch the chat that was just created
                            database.child("chats").child(groupID).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val chatData = snapshot.getValue(Chat::class.java)
                                    if(chatData != null){
                                        callback(true,chatData)
                                    }else{
                                        Log.d("repo-check","chat data is null")
                                    }
                                }
                                override fun onCancelled(p0: DatabaseError) {
                                    Log.d("repo-check","chat fetch failed")
                                }
                            })
                        }
                    }
            }
        }

    }
    private fun checkExistingChat(groupID : String, onResult: (Chat?) -> Unit) {

        database.child("chats").orderByChild("groupID").equalTo(groupID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var existingChat : Chat? = null
                for(cnap in snapshot.children){
                    existingChat = cnap.getValue(Chat::class.java)
                    break
                }
                onResult(existingChat)
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }
        })

    }

    fun sendMessage(chatID : String, message : String, callback : (Boolean, Message) -> Unit) {
        val currentUser = auth.currentUser

        val messageID = database.child("messages").child(chatID).push().key ?: ""
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMessage = Message(
            messageID = messageID,
            senderID = currentUser?.uid ?: "",
            chatID = chatID,
            message = message,
            time = time,
            messageSenderName = currentUser?.displayName ?: ""
        )
        database.child("messages").child(chatID).child(messageID).setValue(newMessage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true,newMessage)
                    Log.d("repo-check", "message sent")
                }else{
                    Log.d("repo-check","message send failed")
                    callback(false,newMessage)
                }
            }
    }
    fun getGroupMembers(groupID : String, onResult : (List<String>) -> Unit){
        val groupRef = database.child("groups").child(groupID)

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupData = snapshot.getValue(GroupResponse::class.java)
                if(groupData != null){
                    onResult(groupData.members)
                }else{
                    Log.d("repo-check","group data is null")
                    onResult(emptyList())
                }
            }
            override fun onCancelled(error: DatabaseError) {

                }
        })
    }
    fun loadMessages(chatID : String, onResult : (List<Message>) -> Unit){
        database.child("messages").child(chatID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for(mSnap in snapshot.children){
                    val message = mSnap.getValue(Message::class.java)
                    if(message != null){
                        messages.add(message)
                    }
                }
                onResult(messages)
            }


            override fun onCancelled(p0: DatabaseError) {
                onResult(emptyList())
            }
        })

    }






}