package com.example.letslink.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.R
import com.example.letslink.SessionManager
import com.example.letslink.activities.EventDetails
import com.example.letslink.local_database.EventDao
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.model.Event
import com.example.letslink.online_database.SyncDataManager
import com.example.letslink.online_database.fb_EventsRepo
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

/**
 * Fragment responsible for displaying the list of events.
 */
class EventsFragment : Fragment() {

    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var fb_EventsRepo: fb_EventsRepo
    private var events: List<Event> = emptyList()
    private lateinit var eventDao: EventDao
    private lateinit var syncManager: SyncDataManager
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment (R.layout.fragment_events)
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fb_EventsRepo = fb_EventsRepo(requireContext())
        // Note: The provided XML uses R.id.groups_recycler_view, so we must use that ID here.
        eventsRecyclerView = view.findViewById(R.id.groups_recycler_view)
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        syncManager = SyncDataManager(requireContext())
        sessionManager = SessionManager(requireContext())
        eventAdapter = EventAdapter(emptyList()) {}
        eventsRecyclerView.adapter = eventAdapter
        eventDao = LetsLinkDB.getDatabase(requireContext()).eventDao()
        val userId = sessionManager.getUserId()
        if (syncManager.isInternetAvailable(requireContext())) {
            //check for internet connection and if there is no connection call the local events and if there is internet connection use the online events
            lifecycleScope.launch {
                events = fb_EventsRepo.getEventsThatBelongToUser()
                eventAdapter = EventAdapter(events) { event ->
                    val eventDetailsFragments = EventDetails.newInstace(event)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, eventDetailsFragments)
                        .addToBackStack(null)
                        .commit()
                }
                eventsRecyclerView.adapter = eventAdapter
                Log.d("EventsFragment", "Events: ${events.size}")
            }
        } else {
            //call local events
            lifecycleScope.launch {
                events = eventDao.getEventsForCurrentUser(userId.toString())
                eventAdapter = EventAdapter(events) { event ->
                    val eventDetailsFragments = EventDetails.newInstace(event)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, eventDetailsFragments)
                        .addToBackStack(null)
                        .commit()
                }
                eventsRecyclerView.adapter = eventAdapter
                Log.d("EventsFragment", "Events: ${events.size}")
            }
        }
    }
}


// RecyclerView Adapter for Events
class EventAdapter(
    private val events: List<Event>,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        // Assumes R.layout.item_events_card exists and is correctly structured
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_events_card, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
        holder.itemView.setOnClickListener { onItemClick(event) }
    }

    override fun getItemCount(): Int = events.size

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ⭐️ UPDATED IDs to match the provided XML card structure ⭐️
        // Note: The card XML uses group-related names for Event elements.
       // val eventImage: CircleImageView = itemView.findViewById(R.id.group_profile_image) // Card XML ID
        val eventTitleText: TextView = itemView.findViewById(R.id.event_name_text)      // Card XML ID
        val groupAssociatedText: TextView = itemView.findViewById(R.id.group_assotiated) // Card XML ID
        val groupEventsText: TextView = itemView.findViewById(R.id.group_events_text)    // Card XML ID (now shows tasks)

        fun bind(event: Event) {
         //   eventImage.setImageResource(R.drawable.ic_launcher_foreground)
            eventTitleText.text = event.title
            groupAssociatedText.text = event.description
            groupEventsText.text = 0.toString() // Placeholder for tasks, updated later
        }
    }
}

