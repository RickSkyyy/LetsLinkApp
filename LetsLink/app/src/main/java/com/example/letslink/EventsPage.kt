package com.example.letslink.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.R
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Fragment responsible for displaying the list of events.
 */
class EventsFragment : Fragment() {

    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment (R.layout.fragment_events)
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Note: The provided XML uses R.id.groups_recycler_view, so we must use that ID here.
        eventsRecyclerView = view.findViewById(R.id.groups_recycler_view)
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Dummy data for Events, updated to match the new card structure:
        // Title, Associated Group, Task Count, and Image Resource
        val dummyEvents = listOf(
            Event("Braai at Z's House", "Weekend Adventures", 3, R.drawable.braai_dp),
            Event("New Year's Eve Party", "Foodie Explorers Club", 1, R.drawable.new_years_dp),
            Event("Sunday Morning Hike", "Team Awesome Devs", 0, R.drawable.hiking_dp),
            Event("Sandton Hackathon", "Travel Buddies", 5, R.drawable.hackathon_dp)
        )

        eventAdapter = EventAdapter(dummyEvents) { event ->
            // Navigate to EventDetails fragment when an event card is clicked
            val eventDetailsFragment = com.example.letslink.EventDetails.newInstance(
                event.title,
                event.associatedGroupName
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, eventDetailsFragment) // Make sure this is your container id
                .addToBackStack(null)
                .commit()
        }
        eventsRecyclerView.adapter = eventAdapter

    }
}

// Data class to represent an Event, updated to match the new card requirements
data class Event(
    val title: String,
    val associatedGroupName: String,
    val tasksCount: Int,
    val imageResId: Int // Drawable resource ID for event image/icon
)

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
        val eventImage: CircleImageView = itemView.findViewById(R.id.group_profile_image) // Card XML ID
        val eventTitleText: TextView = itemView.findViewById(R.id.event_name_text)      // Card XML ID
        val groupAssociatedText: TextView = itemView.findViewById(R.id.group_assotiated) // Card XML ID
        val groupEventsText: TextView = itemView.findViewById(R.id.group_events_text)    // Card XML ID (now shows tasks)

        fun bind(event: Event) {
            eventImage.setImageResource(event.imageResId)
            eventTitleText.text = event.title
            groupAssociatedText.text = event.associatedGroupName
            groupEventsText.text = "${event.tasksCount} Tasks"
        }
    }
}
