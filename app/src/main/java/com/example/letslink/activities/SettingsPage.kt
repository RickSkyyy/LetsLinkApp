package com.example.letslink.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.letslink.R
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.local_database.UserDao
import com.example.letslink.online_database.SyncDataManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.to

class SettingsFragment : Fragment(R.layout.fragment_settings), android.location.LocationListener {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notifications_enabled", isGranted).apply()
        }

    private lateinit var locationSwitch: SwitchCompat
    private lateinit var database: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userDao: UserDao
    private lateinit var locationManager: LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var isSharing = false
    private lateinit var syncManager: SyncDataManager

    @SuppressLint("MissingPermission")
    private val requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startSharingLocation() else locationSwitch.isChecked = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userDao = LetsLinkDB.getDatabase(requireContext()).userDao()
        syncManager = SyncDataManager(requireContext())

        // Map switches
        val notificationsSwitch = view.findViewById<SwitchCompat>(R.id.notifications_switch)
        val clickNoiseSwitch = view.findViewById<SwitchCompat>(R.id.click_noise_switch)
        locationSwitch = view.findViewById(R.id.share_location_switch)
        val syncDatabaseSwitch = view.findViewById<SwitchCompat>(R.id.sync_database_switch)

        val languageHeader = view.findViewById<LinearLayout>(R.id.language_card_header)
        val languageDropdown = view.findViewById<LinearLayout>(R.id.language_dropdown_container)
        val languageArrow = view.findViewById<ImageView>(R.id.language_arrow)

        languageHeader.setOnClickListener {
            if (languageDropdown.visibility == View.GONE) {
                languageDropdown.visibility = View.VISIBLE
                languageArrow.rotation = 180f // arrow points up
            } else {
                languageDropdown.visibility = View.GONE
                languageArrow.rotation = 0f // arrow points down
            }
        }


// Language cards
        val langCards: Map<String, LinearLayout> = mapOf(
            "en" to view.findViewById<LinearLayout>(R.id.lang_row_english),
            "zu" to view.findViewById<LinearLayout>(R.id.lang_row_zulu),
            "af" to view.findViewById<LinearLayout>(R.id.lang_row_afrikaans),
            "sn" to view.findViewById<LinearLayout>(R.id.lang_row_shona),
            "zh" to view.findViewById<LinearLayout>(R.id.lang_row_chinese)
        )

        // Load saved language and set initial checkmark
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currentLang = prefs.getString("app_language", "en") ?: "en"
        updateCheckmarks(currentLang)

        // Language switching
        langCards.forEach { (lang, card) ->
            card.setOnClickListener { switchLanguage(lang) }
        }

        // Notifications switch
        notificationsSwitch.isChecked = prefs.getBoolean("notifications_enabled", true)
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            if (isChecked) enablePushNotifications() else disablePushNotifications()
        }

        // Share location switch
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        database = FirebaseDatabase.getInstance().getReference("user_locations")
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) enableLocationSharing() else disableLocationSharing()
        }

        // Sync database switch
        syncDatabaseSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lifecycleScope.launch {
                    syncManager.pushLocalDatabaseToFirebase() { tasksB, eventsB ->
                        val msg = when {
                            tasksB && eventsB -> getString(R.string.sp9_sync_successful)
                            !tasksB && !eventsB -> getString(R.string.sp9_sync_failed)
                            !tasksB -> getString(R.string.sp9_sync_events_tasks_failed)
                            else -> getString(R.string.sp9_sync_tasks_events_failed)
                        }
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun switchLanguage(languageCode: String) {
        persistLanguage(languageCode)
        // The toast will show in the old language, then the activity will be recreated.
        Toast.makeText(requireContext(), getString(R.string.language_changed), Toast.LENGTH_SHORT).show()
        activity?.recreate()
    }

    private fun persistLanguage(languageCode: String) {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", languageCode).apply()
    }

    private fun updateCheckmarks(languageCode: String) {
        // Update checkmarks
        val langChecks = mapOf(
            "en" to view?.findViewById<View>(R.id.lang_check_english),
            "zu" to view?.findViewById<View>(R.id.lang_check_zulu),
            "af" to view?.findViewById<View>(R.id.lang_check_afrikaans),
            "sn" to view?.findViewById<View>(R.id.lang_check_shona),
            "zh" to view?.findViewById<View>(R.id.lang_check_chinese)
        )
        langChecks.forEach { (lang, viewCheck) ->
            viewCheck?.visibility = if (lang == languageCode) View.VISIBLE else View.GONE
        }
    }

    private fun enablePushNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun disablePushNotifications() {}

    private fun enableLocationSharing() {
        if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        startSharingLocation()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startSharingLocation() {
        isSharing = true
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
        scheduleLocationUpdate()
    }

    private fun disableLocationSharing() {
        isSharing = false
        handler.removeCallbacksAndMessages(null)
        locationManager.removeUpdates(this)
    }

    private fun scheduleLocationUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (isSharing) {
                val lastKnown = if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
                lastKnown?.let { updateLocation(it) }
                delay(30000)
            }
        }
    }

    private fun updateLocation(location: Location) {
        val user = auth.currentUser ?: return
        lifecycleScope.launch {
            val currentUser = userDao.getUserByEmail(user.email!!)
            val userId = currentUser?.userId ?: return@launch
            val userLocation = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "timestamp" to System.currentTimeMillis()
            )
            database.child(userId).setValue(userLocation)
        }
    }

    override fun onLocationChanged(location: Location) {
        if (isSharing) updateLocation(location)
    }
}
