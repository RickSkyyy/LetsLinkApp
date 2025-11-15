package com.example.letslink.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letslink.R
import com.example.letslink.databinding.FragmentMyTicketsBinding
import com.example.letslink.tickets.TicketAdapter
import com.example.letslink.tickets.TicketStorage

class MyTicketsFragment : Fragment() {

    private lateinit var binding: FragmentMyTicketsBinding
    private lateinit var adapter: TicketAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyTicketsBinding.inflate(inflater, container, false)

        adapter = TicketAdapter { uri ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TicketViewerFragment.newInstance(uri))
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTickets.adapter = adapter

        loadTickets()

        binding.fabAddTicket.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddTicketFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    private fun loadTickets() {
        adapter.submitList(TicketStorage.getSavedTickets(requireContext()))
    }
}
