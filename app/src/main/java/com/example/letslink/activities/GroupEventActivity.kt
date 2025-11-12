package com.example.letslink.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.letslink.R
import com.example.letslink.online_database.fb_EventsRepo
import com.example.letslink.online_database.fb_TaskRepo
import kotlinx.coroutines.launch
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.API_related.GroupRepo
import com.example.letslink.Network.PushApiClient
import com.example.letslink.adapter.EventTaskAdapter
import com.example.letslink.adapter.TaskAdapter
import com.example.letslink.model.Event
import com.example.letslink.online_database.fb_userRepo

class GroupEventActivity : AppCompatActivity() {
    private lateinit var fbEventsrepo: fb_EventsRepo
    private lateinit var userRepo : fb_userRepo
    private lateinit var event : Event
    private lateinit var taskRepo : fb_TaskRepo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_group_event)

        val context = this
        val eventId = intent.getStringExtra("event_data")

        fbEventsrepo = fb_EventsRepo(this)
        taskRepo = fb_TaskRepo(this)
        userRepo = fb_userRepo()


        val tasksRV = findViewById<RecyclerView>(R.id.tasksRV)
        tasksRV.layoutManager = LinearLayoutManager(this)
        tasksRV.adapter = EventTaskAdapter(this, mutableListOf(),{})



        lifecycleScope.launch{
             event = fbEventsrepo.getEventById(eventId!!)!!
            if(event != null) {
                val eventName = findViewById<TextView>(R.id.eventName)
                val eventDescription = findViewById<TextView>(R.id.eventDescription)
                val eventLocation = findViewById<TextView>(R.id.eventLocation)
                val eventTime = findViewById<TextView>(R.id.eventTime)
                val tasks = taskRepo.getTasksForEvent(eventId)
                Log.d("tasks","${tasks.size}")
                if (tasks.isNotEmpty()){
                    val adapter = EventTaskAdapter(
                        context,
                        tasks,
                        { task ->
                        taskRepo.updateTaskStatus(task)
                        val groups = event.groups
                        if (groups != null) {
                            Log.d("group size","${groups.size}")
                            for(group in groups){
                                fbEventsrepo.getGroupMembersV2(group){success, members,groupName ->
                                    if(success) {
                                        try{
                                            userRepo.getUsersFcmTokens(members){tokens->
                                                if(tokens.isNotEmpty()){
                                                    Log.d("calling API","${tokens.size}")
                                                    PushApiClient.sendTaskUpdateStatus(
                                                        context,
                                                        tokens,
                                                        groupName,
                                                        task.taskName,
                                                        task.taskStatus
                                                    )
                                                }else{
                                                    Log.d("token-check","No tokens found")
                                                }
                                            }
                                        }catch(e : Exception){
                                            Log.d("check-error",e.toString())
                                        }

                                    }else{
                                        Log.d("fb_EventsRepo","No members found")
                                    }
                                }
                            }
                        }else{
                            Log.d("fb_EventsRepo","No groups found")
                        }
                    })
                    tasksRV.adapter = adapter
                }
                eventName.text =  "Event Name: ${event.title}"
                eventDescription.text = "Event Name: ${ event.description}"
                eventLocation.text ="Event Name: ${ event.location}"
                eventTime.text = "Duration: ${event.startTime} - ${event.endTime}"



            }else
                Log.d("fb_EventsRepo", "Event not found")
        }
    }
}