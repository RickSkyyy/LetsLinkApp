package com.example.letslink.online_database
import android.util.Log
import com.example.letslink.model.User
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.database.ValueEventListener

class fb_userRepo(
    private val auth:FirebaseAuth = FirebaseAuth.getInstance(),
    private val database : DatabaseReference = com.google.firebase.database.FirebaseDatabase.getInstance().reference
) {
    fun register(user : User, callback :(Boolean, String?, User?) -> Unit){
        auth.createUserWithEmailAndPassword(user.email,user.password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    FirebaseMessaging.getInstance().token.addOnCompleteListener{tokenTask ->
                        if(tokenTask.isSuccessful){
                            user.fcmToken = tokenTask.result ?: ""
                            Log.d("FCM Token", "FCM Token: ${user.fcmToken}")
                        }else{
                            Log.e("FCM Token", "Failed to get FCM token", tokenTask.exception)
                        }

                        database.child("users")
                            .child(user.userId.toString())
                            .setValue(user)
                            .addOnCompleteListener{ task ->
                                if(task.isSuccessful){
                                    callback(true,null,user)
                                }else{
                                    callback(false,task.exception?.message,null)
                                }
                            }
                    }

                }else{
                    callback(false,task.exception?.message,null)
                }



            }

    }
    fun getUsersFcmTokens(userIDs : List<String>) : List<String> {
        val tokens = mutableListOf<String>()
        val userRef = database.child("users")
        for(userID in userIDs) {
            userRef.child(userID).child("fcmToken").addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val token = snapshot.getValue(String::class.java)
                    if (token != null) {
                        Log.d("check-token",token)
                        tokens.add(token)
                    }
                }


                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
        return tokens
    }


}