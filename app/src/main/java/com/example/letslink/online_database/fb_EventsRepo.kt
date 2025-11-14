package com.example.letslink.online_database

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.local_database.UserDao
import com.example.letslink.model.Event
import com.example.letslink.model.Group
import com.example.letslink.model.GroupResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class fb_EventsRepo(context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private  val userDao : UserDao = LetsLinkDB.getDatabase(context).userDao()
    fun createEvent(title:String, description:String, location:String, startTime:String, endTime:String, date:String,userid : String,groups : List<String> ,isSynced : Boolean, imageUrl : String, callback :(Boolean, String) -> Unit){
        val eventId = database.child("events").push().key ?: ""
        val event =
            Event(eventId, userid, title, description, location, startTime, endTime, date,groups,isSynced,imageUrl)
        database.child("events").child(eventId).setValue(event)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    callback(true, eventId)
                }else{
                    callback(false, "Failed to create event, please try again later")
                }
            }
    }
    suspend fun getEventsThatBelongToUser(): List<Event>{
        val user = auth.currentUser
        var events = mutableListOf<Event>()
        if(user != null){
            val userEmail = user.email
            val localUser = userDao.getUserByEmail(userEmail!!)
            if(localUser != null){
                val userId = localUser.userId
                val snapshot = database.child("events")
                    .orderByChild("ownerId")
                    .equalTo(userId)
                    .get()
                    .await()

                for(child in snapshot.children){
                    Log.d("__fb__--", "Child key: ${child.key}")
                    val event = child.getValue(Event::class.java)
                    if(event != null){
                        events.add(event)
                    }
                }
            }
        }

        return events
    }
    suspend fun getEventById(eventId : String) : Event? = suspendCoroutine{ cont ->
        val dbRef = FirebaseDatabase.getInstance().getReference("events").child(eventId)

        dbRef.get().addOnCompleteListener { task ->
            if(task.isSuccessful){
                val snapshot = task.result
                val event = snapshot.getValue(Event::class.java)
                cont.resume(event)
            }else{
                cont.resume(null)
            }

        }


    }
    //create a method that receives the groupID and returns the members of that group
    fun getGroupMembers (grouId : String, callback :(Boolean,List<String>) ->Unit){
        database.child("groups").child(grouId).get()
            .addOnSuccessListener { snapshot ->
                val members = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                if(members.isNotEmpty()){
                    callback(true,members)
                    Log.d("--group members fetched Successfull","${members.size}")
                }else{
                    callback(false, emptyList())
                }
            }
    }
    fun getGroupMembersV2 (grouId : String, callback :(Boolean,List<String>,String) ->Unit){
        database.child("groups").child(grouId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val group = snapshot.getValue(GroupResponse::class.java)
                    if(group != null){
                        callback(true,group.members,group.groupName)
                    }else{
                        callback(false, emptyList(),"")
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}