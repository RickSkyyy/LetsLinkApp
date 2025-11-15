package com.example.letslink.fragments

import android.content.Context
import android.os.Bundle
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
import com.example.letslink.model.Event
import com.example.letslink.model.Group
import com.example.letslink.model.TMEvent
import kotlinx.coroutines.launch
import com.example.letslink.online_database.SyncDataManager
import com.example.letslink.online_database.fb_EventsRepo
import java.util.UUID

class HomeFragment : Fragment() {
    private val selectedGroupIds = mutableListOf<String>()
    private val groupList = mutableListOf<Group>()
    private lateinit var syncManager : SyncDataManager
    private lateinit var recyclerView : RecyclerView
    private lateinit var groupDao : GroupDao
    private lateinit var userId : String
    private lateinit var eventsRepo : fb_EventsRepo
    private lateinit var eventDao : EventDao


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
}