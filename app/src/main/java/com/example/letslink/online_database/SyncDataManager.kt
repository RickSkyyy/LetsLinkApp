package com.example.letslink.online_database

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.letslink.local_database.EventDao
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.local_database.TaskDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.tasks.await

class SyncDataManager(private val context: Context) {
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var taskDao : TaskDao
    private lateinit var eventDao : EventDao
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


     fun isInternetAvailable(context : Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }

    }

    suspend fun pushLocalDatabaseToFirebase (callback: (Boolean, Boolean) -> Unit) {

        taskDao = LetsLinkDB.getDatabase(context).taskDao()
        eventDao = LetsLinkDB.getDatabase(context).eventDao()
        var bFlag1 : Boolean
        var bFlag2 : Boolean

        try {
            val local_tasks = taskDao.getTasksToSync()
            Log.d("sync-task length","${local_tasks.size}")
            for(task  in local_tasks){
                if(task.taskId.isBlank())
                    continue

                Log.d("sync-task",task.taskId)
                task.isSynced = true
                val taskRef = database.child("tasks").child(task.taskId)
                taskRef.setValue(task).await()
                taskDao.updateTaskSyncStatus(task.taskId)
            }
             bFlag1 = true
        }catch (e: Exception){
            Log.d("sync-error",e.toString())
            bFlag1 = false
        }
        try {
            val local_events = eventDao.getEventsToSync()
            Log.d("sync-event length","${local_events.size}")
            for(event in local_events) {
                if (event.eventId.isBlank())
                    continue
                Log.d("sync-event", event.eventId)
                event.isSynced = true
                val eventRef = database.child("events").child(event.eventId)
                eventRef.setValue(event).await()
                eventDao.updateEvents(event.eventId)
            }
            bFlag2 = true
        }catch(e:Exception){
            bFlag2 = false
        }
        //now do the events

        callback(bFlag1,bFlag2)

    }
}
