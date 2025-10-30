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

class GroupsFragment : Fragment() {

    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupsRecyclerView = view.findViewById(R.id.groups_recycler_view)
        groupsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Dummy data for now (replace with actual data from a database/API later)
        val dummyGroups = listOf(
            Group("Team Awesome Devs", 5, 2, R.drawable.ic_group_coding),
            Group("Weekend Adventure Squad", 8, 3, R.drawable.ic_group_hiking),
            Group("Foodie Explorers Club", 12, 1, R.drawable.ic_group_food),
            Group("Travel Buddies", 6, 2, R.drawable.ic_group_travel)
        )

        groupAdapter = GroupAdapter(dummyGroups) { group ->
            // --- Navigation Logic Added Here ---

            // 1. Create a Bundle to pass data (like the group name) to the new fragment
            val bundle = Bundle().apply {
                putString("GROUP_NAME", group.name)
            }

            // 2. Create the destination fragment and attach the bundle
            val groupDetailsFragment = GroupDetailsF().apply {
                arguments = bundle
            }

            // 3. Perform the Fragment Transaction
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, groupDetailsFragment)
                .addToBackStack(null) // Allows the user to press the back button to return to the Groups list
                .commit()

            // Optional Toast removed to prevent double-tap issues when navigating
            // Toast.makeText(context, "Clicked on group: ${group.name}", Toast.LENGTH_SHORT).show()
        }
        groupsRecyclerView.adapter = groupAdapter
    }
}

// Data class to represent a group
data class Group(
    val name: String,
    val members: Int,
    val events: Int,
    val profilePicResId: Int // Drawable resource ID for profile picture
)

// RecyclerView Adapter
class GroupAdapter(
    private val groups: List<Group>,
    private val onItemClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_card, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group)
        holder.itemView.setOnClickListener { onItemClick(group) }
    }

    override fun getItemCount(): Int = groups.size

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupProfileImage: CircleImageView = itemView.findViewById(R.id.group_profile_image)
        val groupNameText: TextView = itemView.findViewById(R.id.group_name_text)
        val groupMembersText: TextView = itemView.findViewById(R.id.group_members_text)
        val groupEventsText: TextView = itemView.findViewById(R.id.group_events_text)

        fun bind(group: Group) {
            groupProfileImage.setImageResource(group.profilePicResId)
            groupNameText.text = group.name
            groupMembersText.text = "${group.members} Members"
            groupEventsText.text = "${group.events} Events"
        }
    }
}