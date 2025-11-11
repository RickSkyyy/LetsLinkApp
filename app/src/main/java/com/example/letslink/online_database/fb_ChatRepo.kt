package com.example.letslink.online_database

import android.util.Log
import com.example.letslink.model.GroupResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class fb_ChatRepo {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun createChat(groupID:String){

    }

    //create function that must check for existing chats that have the group ids
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




}