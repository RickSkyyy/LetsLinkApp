package com.example.letslink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class GroupDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        // Load group image
        val groupImage: ImageView = findViewById(R.id.img_group_avatar)
        Glide.with(this)
            .load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/DFGR9ODRc7/dzsyxlnx_expires_30_days.png")
            .into(groupImage)

        // Header buttons
        val buttonProfile: View = findViewById(R.id.btn_view_members)
        buttonProfile.setOnClickListener { println("Profile pressed") }

        val buttonSettings: View = findViewById(R.id.btn_group_settings)
        buttonSettings.setOnClickListener { println("Settings pressed") }

        // Members (example for first three members)
        val member1Icon: View = findViewById(R.id.layout_member_1)
        member1Icon.setOnClickListener { println("Member 1 pressed") }

        val member2Icon: View = findViewById(R.id.layout_member_2)
        member2Icon.setOnClickListener { println("Member 2 pressed") }

        val member3Icon: View = findViewById(R.id.layout_member_3)
        member3Icon.setOnClickListener { println("Member 3 pressed") }

        val buttonStartChat: View = findViewById(R.id.btn_start_chat)
        buttonStartChat.setOnClickListener {
            val intent = Intent(this, GroupChatActivity::class.java)
            startActivity(intent)
        }

       // val buttonStartChat: View = findViewById(R.id.btn_start_chat)
       // buttonStartChat.setOnClickListener { println("Messages stats pressed") }


        // Group stats buttons
        val messagesStatLayout: View = findViewById(R.id.card_messages)
        messagesStatLayout.setOnClickListener { println("Messages stats pressed") }

        val eventsStatLayout: View = findViewById(R.id.card_events)
        eventsStatLayout.setOnClickListener { println("Events stats pressed") }

        val tasksStatLayout: View = findViewById(R.id.card_tasks)
        tasksStatLayout.setOnClickListener { println("Tasks stats pressed") }

        // Action buttons
        val buttonCreateEvent: View = findViewById(R.id.btn_create_event)
        buttonCreateEvent.setOnClickListener { println("Create Event pressed") }

        val buttonAddTask: View = findViewById(R.id.btn_add_task)
        buttonAddTask.setOnClickListener { println("Add Task pressed") }


    }
}

