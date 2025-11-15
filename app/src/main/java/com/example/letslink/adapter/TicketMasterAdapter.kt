package com.example.letslink.adapter

import com.example.letslink.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letslink.model.TMEvent
import com.example.letslink.utils.TranslationManager
import kotlinx.coroutines.launch

class TicketMasterAdapter(
    private val events: MutableList<TMEvent>,
    private val onItemClick: (TMEvent) -> Unit,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val translationManager: TranslationManager
) : RecyclerView.Adapter<TicketMasterAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventImage: ImageView = itemView.findViewById(R.id.eventImage)
        val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        val eventLocation: TextView = itemView.findViewById(R.id.eventLocation)
        val eventPrice: TextView = itemView.findViewById(R.id.eventPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticketmaster_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        // Load image
        val imageUrl = event.images?.maxByOrNull { it.width ?: 0 }?.url
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.eventImage)

        // Set title with translation
        val originalTitle = event.name ?: "Unknown Event"
        holder.eventTitle.text = originalTitle // Show original first

        // Translate title asynchronously
        lifecycleScope.launch {
            translationManager.translateText(
                originalText = originalTitle,
                onSuccess = { translatedText ->
                    holder.eventTitle.text = translatedText
                },
                onError = { fallbackText ->
                    holder.eventTitle.text = fallbackText
                }
            )
        }

        // Set date
        val date = event.dates?.start?.localDate ?: "TBA"
        val time = event.dates?.start?.localTime ?: ""
        holder.eventDate.text = "$date $time"

        // Set location
        val venue = event._embedded?.venues?.firstOrNull()
        val venueName = venue?.name ?: "Unknown Venue"
        val city = venue?.city?.name ?: ""
        val country = venue?.country?.name ?: ""
        holder.eventLocation.text = "$venueName • $city, $country"

        // Set price
        val priceRange = event.priceRanges?.firstOrNull()
        val min = priceRange?.min
        val max = priceRange?.max
        val currency = priceRange?.currency ?: ""
        holder.eventPrice.text = when {
            min != null && max != null -> "$currency $min - $max"
            min != null -> "From $currency $min"
            else -> "Price not available"
        }

        // Handle click
        holder.itemView.setOnClickListener {
            onItemClick(event)
        }
    }

    fun updateData(newEvents: List<TMEvent>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = events.size
}