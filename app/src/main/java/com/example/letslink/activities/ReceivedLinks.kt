package com.example.letslink.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.API_related.AppContainer
import com.example.letslink.API_related.MyApp
import com.example.letslink.R
import com.example.letslink.SessionManager
import com.example.letslink.adapter.JoinAdapter
import com.example.letslink.viewmodels.ReceivedLinksViewModel
import com.example.letslink.model.Invites

class ReceivedLinks : Fragment() {
    // 1. Switched to the dedicated ViewModel
    private lateinit var viewModel: ReceivedLinksViewModel
    private lateinit var appContainer: AppContainer
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: JoinAdapter
    private lateinit var groupsRecyclerView: RecyclerView




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_received_links, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupsRecyclerView = view.findViewById(R.id.groups_recycler_view)

        // Initialize dependencies
        val application = requireActivity().application
        appContainer = (application as MyApp).container
        sessionManager = appContainer.sessionManager

        // Initialize ViewModel
        viewModel = ViewModelProvider(
            this,
            ReceivedLinksViewModel.provideFactory(appContainer.groupRepository, sessionManager)
        )[ReceivedLinksViewModel::class.java]

        setupRecyclerView()
        observeInvites()
    }


    /**
     *  Observes receivedInvites  from the ViewModel
     *(Lackner, 2025b)
     *
     */
    private fun observeInvites() {
        // Fetch the invites  which triggers the Firebase
        val userId = sessionManager.getUserId().toString()
        if (userId.isNotBlank()) {
            viewModel.fetchReceivedInvites(userId)
        } else {
            Toast.makeText(requireContext(), "Error: User ID is missing.", Toast.LENGTH_SHORT).show()
        }

        //  Observe the list of Invites objects/ invites received
        viewModel.receivedInvites.observe(viewLifecycleOwner) { invitesList ->
            // Ensure the list is not null
            adapter.submitList(invitesList)
            println("Loaded ${invitesList?.size ?: 0} received invites.")

            //  Show a message if the list is empty
            if (invitesList.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No invites received.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * handles Invites
    (Lackner, 2025b)
     */
    private fun setupRecyclerView() {
        adapter = JoinAdapter(
            onJoinClick = { invite ->
                // Extract just the group ID from the inviteLink (which is the full URL)
                val groupId = extractGroupIdFromInviteLink(invite.inviteLink)
                val currentUserId = sessionManager.getUserId()

                if (groupId != null && currentUserId.toString().isNotBlank()) {
                    // function to join the group using the extracted groupId
                    viewModel.joinGroup(groupId, currentUserId)
                    Toast.makeText(requireContext(), "Attempting to join ${invite.groupName}...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error: Cannot join group. Invalid invite link.", Toast.LENGTH_SHORT).show()
                }
            }
        )

        groupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReceivedLinks.adapter
        }
    }

    /**
     * Extracts the group ID from the full invite link URL
     */
    private fun extractGroupIdFromInviteLink(inviteLink: String): String? {
        return try {
            // Get the last part of the URL after the last "/"
            inviteLink.substringAfterLast("/").takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}