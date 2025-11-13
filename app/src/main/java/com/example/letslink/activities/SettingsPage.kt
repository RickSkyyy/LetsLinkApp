package com.example.letslink.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SwitchCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.letslink.R // Make sure to use your correct R package
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.local_database.UserDao
import com.example.letslink.online_database.SyncDataManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(),android.location.LocationListener {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notifications_enabled", isGranted).apply()
        }

    private lateinit var locationSwitch: SwitchCompat
    private lateinit var database : DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userDao : UserDao
    private lateinit var locationManager : LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var isSharing = false
    private lateinit var syncManager : SyncDataManager
    @SuppressLint("MissingPermission")
    private val requestLocationPermission = registerForActivityResult( ActivityResultContracts.RequestPermission()) {
        isGranted: Boolean ->
        if(isGranted){
            startSharingLocation()
        }else{
            locationSwitch.isChecked = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userDao = LetsLinkDB.getDatabase(requireContext()).userDao()
        super.onViewCreated(view, savedInstanceState)
        syncManager = SyncDataManager(requireContext())
        //notification switch
        val notificationsSwitch = view.findViewById<SwitchCompat>(R.id.notifications_switch)
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        notificationsSwitch.isChecked = prefs.getBoolean("notifications_enabled", true)

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()

            if (isChecked) {
                enablePushNotifications()
            }else{
                disablePushNotifications()
            }
        }
        //click noise switch
        //live location switch
        locationSwitch = view.findViewById(R.id.share_location_switch)
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        database = FirebaseDatabase.getInstance().getReference("user_locations")

        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                enableLocationSharing()
            }else{
                disableLocationSharing()
            }
        }

        //sync switch
        val syncSwitch = view.findViewById<SwitchCompat>(R.id.sync_database_switch)
        syncSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                //sync the database
                lifecycleScope.launch {
                   val success = syncManager.pushLocalDatabaseToFirebase()
                    if(success){
                        Toast.makeText(requireContext(), "Sync successful!", Toast.LENGTH_SHORT).show()
                        syncSwitch.isChecked = true
                    }else{
                        Toast.makeText(requireContext(), "Sync failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
    private fun enablePushNotifications(){
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU){
            if(requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }private fun disablePushNotifications(){

    }
    private fun enableLocationSharing(){
        if(requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        startSharingLocation()
    }
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startSharingLocation(){
        isSharing = true
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0f,
            this
        )
        scheduleLocationUpdate()
    }
    private fun disableLocationSharing(){
        isSharing = false
        handler.removeCallbacksAndMessages(null)
        locationManager.removeUpdates(this)
    }
    private fun scheduleLocationUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (isSharing) {
                val lastKnown =
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    } else null

                lastKnown?.let { updateLocation(it) }

                delay(60_000) // wait 1 min
            }
        }
    }

    private fun updateLocation(location : Location){
        val user = auth.currentUser
        if(user != null){
            lifecycleScope.launch{
                val currentUser = userDao.getUserByEmail(user.email!!)
                val userId = currentUser?.userId


                val userLocation = mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "timestamp" to System.currentTimeMillis()
                )
                database.child(userId!!).setValue(userLocation)
            }

        }
    }
    //add functionality to trigger the sync of the offline and online database
     override fun onLocationChanged(location: Location) {
        if(isSharing){
            updateLocation(location)
        }
    }
}