package com.example.letslink

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import com.example.letslink.nav.HorizontalCoordinator


class Compass : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var currentDegree = 0f

    private lateinit var compass: ImageView
    private lateinit var friend1: ImageView
    private lateinit var friend2: ImageView
    private lateinit var friend3: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        compass = findViewById(R.id.compass)

        // Pulse compass
        val pulse = ObjectAnimator.ofPropertyValuesHolder(
            compass,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.05f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.05f, 1f),
            PropertyValuesHolder.ofFloat("alpha", 1f, 0.85f, 1f)
        )
        pulse.duration = 1000
        pulse.repeatCount = ValueAnimator.INFINITE
        pulse.start()


        friend1 = findViewById(R.id.friend4)
        friend2 = findViewById(R.id.friend5)
        friend3 = findViewById(R.id.friend3)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        //back button
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            val intent = Intent(this, HorizontalCoordinator::class.java)
            intent.putExtra("showFragment", "map") // 👈 tell coordinator to load FriendMapFragment
            startActivity(intent)
            finish()
        }



    }


    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val degree = Math.round(event.values[0]).toFloat()

        // Rotate compass base
        val rotateAnimation = RotateAnimation(
            currentDegree,
            -degree,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 210
        rotateAnimation.fillAfter = true
        compass.startAnimation(rotateAnimation)

        currentDegree = -degree

        // Example friend angles (later replace with GPS bearings)
        placeFriend(friend1, 45)
        placeFriend(friend2, 120)
        placeFriend(friend3, 250)
    }

    private fun placeFriend(friend: ImageView, angle: Int) {
        val radius = compass.width / 2.5 // distance from center
        val centerX = compass.x + compass.width / 2
        val centerY = compass.y + compass.height / 2

        val rad = Math.toRadians(angle.toDouble())
        val x = (centerX + radius * Math.cos(rad)).toFloat()
        val y = (centerY + radius * Math.sin(rad)).toFloat()

        friend.x = x - friend.width / 2
        friend.y = y - friend.height / 2
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
