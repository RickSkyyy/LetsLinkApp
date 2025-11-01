package com.example.letslink.Network
import android.content.Context
import android.util.Log
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import com.android.volley.toolbox.StringRequest

object PushApiClient {
    fun sendMessageNotification(
        context : Context,
        fcmTokens : List<String>,
        groupName : String,
        text : String,
    ){
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)
            ?.addOnCompleteListener {
                val idToken = it.result.token

                val requestBody = JSONObject().apply{
                    put("groupName", groupName)
                    put("messageText", text)
                    put("tokens", fcmTokens)
                }
                Log.d("API body", requestBody.toString())
                val request = object : StringRequest(
                    Method.POST,
                    "https://letslinkpushapi.onrender.com/send-message",
                    {response -> Log.d("API response", response)},
                    {error -> Log.e("API error", error.toString())}
                ){
                    override fun getBody(): ByteArray = requestBody.toString().toByteArray()
                    override fun getBodyContentType(): String = "application/json"
                    override fun getHeaders(): MutableMap<String, String> {
                        return mutableMapOf("Authorization" to "Bearer $idToken")
                    }
                }
                Volley.newRequestQueue(context).add(request)
            }
    }
    fun sendTaskUpdateStatus(
        context : Context,
        fcmTokens : List<String>,
        groupName: String,
        taskName : String,
        newStatus : String
    ){
        {
            FirebaseAuth.getInstance().currentUser?.getIdToken(true)
                ?.addOnCompleteListener {
                    val idToken = it.result.token

                    val requestBody = JSONObject().apply{
                        put("groupName", groupName)
                        put("taskName", taskName)
                        put("newStatus",newStatus)
                        put("tokens", fcmTokens)
                    }
                    Log.d("API body", requestBody.toString())
                    val request = object : StringRequest(
                        Method.POST,
                        "https://letslinkpushapi.onrender.com/send-task-update",
                        {response -> Log.d("API response", response)},
                        {error -> Log.e("API error", error.toString())}
                    ){
                        override fun getBody(): ByteArray = requestBody.toString().toByteArray()
                        override fun getBodyContentType(): String = "application/json"
                        override fun getHeaders(): MutableMap<String, String> {
                            return mutableMapOf("Authorization" to "Bearer $idToken")
                        }
                    }
                    Volley.newRequestQueue(context).add(request)
                }
        }
    }
}