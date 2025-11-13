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

    suspend fun pushLocalDatabaseToFirebase () : Boolean{

        taskDao = LetsLinkDB.getDatabase(context).taskDao()
        eventDao = LetsLinkDB.getDatabase(context).eventDao()

       return try {
            val local_tasks = taskDao.getTasksToSync()
            Log.d("sync-task length","${local_tasks.size}")
            for(task  in local_tasks){
                if(task.taskId.isBlank())
                    continue

                Log.d("sync-task",task.taskId)
                val taskRef = database.child("tasks").child(task.taskId)
                taskRef.setValue(task).await()
                taskDao.updateTaskSyncStatus(task.taskId)
            }
             true
        }catch (e: Exception){
            Log.d("sync-error",e.toString())
            false
        }
        //now do the events
    }
    //features to sync offline and online databases:
    /*
    * - Create event ✅
    * - fetch event's that belong to the current user logged in ✅
    * - Create task for specific Event✅
    * - fetch tasks for the event it was created for✅
    *
    * */
}