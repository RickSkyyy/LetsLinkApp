package com.example.letslink.fragments

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.Network.TicketMasterClient
import com.example.letslink.R
import com.example.letslink.SessionManager
import com.example.letslink.adapter.TicketMasterAdapter
import com.example.letslink.local_database.EventDao
import com.example.letslink.local_database.GroupDao
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.local_database.UserDao
import com.example.letslink.model.Event
import com.example.letslink.model.Group
import com.example.letslink.model.TMEvent
import kotlinx.coroutines.launch
import com.example.letslink.online_database.SyncDataManager
import com.example.letslink.online_database.fb_EventsRepo
import java.util.UUID
import android.view.MotionEvent
import androidx.annotation.RequiresPermission
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.letslink.online_database.fb_userRepo


class HomeFragment : Fragment() {
    private val selectedGroupIds = mutableListOf<String>()
    private val groupList = mutableListOf<Group>()
    private lateinit var syncManager : SyncDataManager
    private lateinit var recyclerView : RecyclerView
    private lateinit var groupDao : GroupDao
    private lateinit var userId : String
    private lateinit var eventsRepo : fb_EventsRepo
    private lateinit var eventDao : EventDao
    private lateinit var userDao : UserDao
    private lateinit var userRepo : fb_userRepo

    private val EMERGENCY_MESSAGE = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate your existing layout
        return inflater.inflate(R.layout.activity_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        syncManager = SyncDataManager(requireContext())
        groupDao = LetsLinkDB.getDatabase(requireContext()).groupDao()
        eventDao = LetsLinkDB.getDatabase(requireContext()).eventDao()
        userDao = LetsLinkDB.getDatabase(requireContext()).userDao()
        userRepo = fb_userRepo()
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getString(SessionManager.KEY_USER_ID, null).toString()
        // Apply edge-to-edge padding
        // Handle system insets for full-screen experience - apply only top padding for status bar
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Only apply top padding for status bar, let content extend to bottom
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
        recyclerView = view.findViewById(R.id.ticketMasterRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        eventsRepo = fb_EventsRepo(requireContext())

        val adapter = TicketMasterAdapter(mutableListOf()) { event ->
            // Handle item click here
            showGroupSelectionDialog(event)
        }
        recyclerView.adapter = adapter
        lifecycleScope.launch{
            groupDao.getNotesByUserId(userId!!).collect{ groups ->
                groupList.clear()
                groupList.addAll(groups)
                Log.d("GroupList", groupList.count().toString())
            }
        }
        if(syncManager.isInternetAvailable(requireContext())) {

            lifecycleScope.launch {
                val response = TicketMasterClient.api.getEvents(
                    apiKey = "P8bWsLtGJIGsdxSCDh3c4z39zZABAKi0"
                )
                if (response.isSuccessful) {
                    val events = response.body()?._embedded?.events
                    val distinctEvents = events?.distinctBy { it.name }

                    Log.d("API call successful", "Events: ${events?.size}")
                    distinctEvents?. let{
                        adapter.updateData(it)
                    }

                    // Handle the events list as needed
                } else {
                    // Handle error
                    Log.d("api call unsuccessful", "Error: ${response.code()}")
                }
            }
        }

        //Navigate to my tickets page
        val myTicketsCard = view.findViewById<CardView>(R.id.cardMyTickets)

        myTicketsCard.setOnClickListener {
            // Navigate to MyTicketsFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyTicketsFragment())
                .addToBackStack(null)
                .commit()
        }

        var phoneNumber = ""
        var message = ""
        var userLocation = ""
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001)
        } else {
            getCurrentLocation(requireContext()) { location ->
                if (location != null) {
                    userLocation = location
                } else {
                    userLocation = "No location found"
                }
            }
        }


        lifecycleScope.launch {
            val user = userDao.getUserById(userId!!)
            if(user != null){

                Log.d("check-user","${user.firstName} = ${user.userId} = ${user.emergencyContact}")
                message = "LetsLink - SOS Message 🆘🆘🆘 \n ${user.firstName} needs your help, please make your way to them by using their location:\n ${userLocation} "
                phoneNumber = user.emergencyContact!!
                if(phoneNumber.isEmpty()){
                    //check online for emergency contact
                    userRepo.getUserEmergencyContact(userId!!){ emergencyContact ->
                        if(emergencyContact.isNotEmpty()){
                            phoneNumber = emergencyContact
                        }else{
                            Toast.makeText(requireContext(),"No emergency contact set",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }


        val sosBtn = view.findViewById<CardView>(R.id.sos_layout_btn)

        val handler = Handler(Looper.getMainLooper())
        var isHolding = false
        val holdRunnable = Runnable {
            Log.d("SOS", "Runnabke Fired")
            isHolding = true

            if (phoneNumber.isNotEmpty()) {
                val url =
                    "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    setPackage("com.whatsapp")
                }

                try {
                    Log.d("check-intent", "intent is being started")
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.d("check-error", e.toString())
                    Toast.makeText(requireContext(), "Error sending message", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "No emergency contact set", Toast.LENGTH_SHORT)
                    .show()
            }
        }





        sosBtn.setOnTouchListener{ _, event ->
            Log.d("SOS-Touch","ACTION ${event.action}")
            //get users emergency contact
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    Log.d("SOS","DOWN")
                    isHolding = false
                    handler.postDelayed(holdRunnable, 3000L)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.d("SOS,","UP or cancel")
                    // Handle touch up event
                    handler.removeCallbacks(holdRunnable)
                    if(!isHolding){
                        Log.d("HOLD","Relwased before 3 seconds")

                    }
                }
            }
            true
        }

        /*
        *
        * val phoneNumber = "27821234567"
            val message = "Your package is ready."

            val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.setPackage("com.whatsapp")

            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                // WhatsApp not installed → handle fallback
            }
        *
        * */


    }


    private fun showGroupSelectionDialog(tmEvent: TMEvent) {
        val groupName = groupList.map { it.groupName }.toTypedArray()
        val checkedItems = BooleanArray(groupList.size) { i ->
            selectedGroupIds.contains(groupList[i].groupId.toString())
        }


        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Add ${tmEvent.name} to group:")
        builder.setMultiChoiceItems(groupName, checkedItems) { _, which, isChecked ->
            val group = groupList[which]
            if (isChecked) {
                if (!selectedGroupIds.contains(group.groupId.toString())) {
                    selectedGroupIds.add(group.groupId.toString())
                }
            } else {
                selectedGroupIds.remove(group.groupId.toString())
            }
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            val selectedName = groupList.filter { selectedGroupIds.contains(it.groupId.toString()) }
                .map { it.groupName }

            val tvSelectedGroups = view?.findViewById<TextView>(R.id.tvSelectedGroups)
            if (selectedName.isEmpty()) {
                tvSelectedGroups?.setText("Select Groups")
            } else {
                tvSelectedGroups?.setText(selectedName.toString())
            }
            //create event object and then pass the relevant information from the TMEvent object that was clicked on to the event object
            val event = Event(
                eventId = UUID.randomUUID().toString(),
                ownerId = userId!!,
                title = tmEvent.name ?: "Unknown Event",
                description = "TicketMaster Event",
                location = tmEvent.name ?: "Unknown Location",
                startTime = tmEvent.dates?.start?.localTime ?: "Unknown Time",
                date = tmEvent.dates?.start?.localDate ?: "Unknown Date",
                groups = selectedGroupIds,
                isSynced = false,
                imageUrl = tmEvent.images?.maxByOrNull{ it.width ?: 0 }?.url ?: ""
            )
            //add the event to the online database
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    eventsRepo.createEvent(
                        event.title,
                        event.description,
                        event.location,
                        event.startTime,
                        event.endTime,
                        event.date,
                        event.ownerId,
                        selectedGroupIds,
                        false,
                        event.imageUrl

                    ) { isComplete, eventID ->
                        if (isComplete) {
                            Toast.makeText(context, getString(R.string.hp9_event_added_successfully), Toast.LENGTH_SHORT).show()
                            //insert it locally now
                            viewLifecycleOwner.lifecycleScope.launch {
                                try {
                                    eventDao.addEventLocally(event)
                                    Toast.makeText(context, getString(R.string.hp9_event_added_online), Toast.LENGTH_SHORT).show()
                                }catch (e: Exception){
                                    Log.d("TM-eventAdded-error",e.toString())
                                }
                            }
                            Log.d("Event created successfully!", "Event ID: $eventID")
                        } else {
                            Log.d("Event creation failed!", "Error: $eventID")
                        }
                    }
                }catch(e: Exception){

                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getCurrentLocation(context: Context, callback: (String?) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (lastLocation != null) {
            val latitude = lastLocation.latitude
            val longitude = lastLocation.longitude
            val mapsUrl = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
            callback(mapsUrl)
        } else {
            // If last location is null, request a single update
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val mapsUrl = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
                    callback(mapsUrl)
                    locationManager.removeUpdates(this)
                }

                override fun onProviderDisabled(provider: String) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            try {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper())
            } catch (e: SecurityException) {
                e.printStackTrace()
                callback(null)
            }
        }
    }

}