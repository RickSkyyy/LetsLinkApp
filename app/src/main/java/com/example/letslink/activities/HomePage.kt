package com.example.letslink.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.Network.TicketMasterClient
import com.example.letslink.R
import com.example.letslink.adapter.TicketMasterAdapter
import kotlinx.coroutines.launch
import com.example.letslink.online_database.SyncDataManager

class HomeFragment : Fragment() {
    private lateinit var syncManager : SyncDataManager
    private lateinit var recyclerView : RecyclerView

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


        val adapter = TicketMasterAdapter(mutableListOf()) { event ->
            // Handle item click here
        }
        recyclerView.adapter = adapter

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
    }
}