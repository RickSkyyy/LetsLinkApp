package com.example.letslink

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.letslink.fragments.FriendMapFragment

class TempMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp_menu)

        val buttonEventVoting: Button = findViewById(R.id.buttonEventVoting)
        val buttonSplashPage: Button = findViewById(R.id.buttonSplashPage)
        val buttonMapFunctions: Button = findViewById(R.id.buttonMapFunctions)
        val buttonGroupFunctions: Button = findViewById(R.id.buttonGroupFunctions)

        buttonEventVoting.setOnClickListener {
            val intent = Intent(this, EventVoting::class.java)
            startActivity(intent)
        }

        buttonSplashPage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        buttonMapFunctions.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FriendMapFragment())
                .commit()
        }


        buttonGroupFunctions.setOnClickListener {
            val intent = Intent(this, GroupDetails::class.java)
            startActivity(intent)
        }


    }
}
