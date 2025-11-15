package com.example.letslink.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.R

class MembersAdapter : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    private var members: List<String> = emptyList()
    private var isAdmin: Boolean = false

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberName: TextView = itemView.findViewById(R.id.txt_member_name)
        val memberRole: TextView = itemView.findViewById(R.id.txt_member_role)
        val memberIcon: TextView = itemView.findViewById(R.id.txt_member_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.member_item, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val memberName = members[position]

        holder.memberName.text = memberName
        holder.memberIcon.text = "👤"


        val role = when {
            position == 0 -> "Host"
            else -> "Member"
        }
        holder.memberRole.text = role


        if (position == 0) {
            holder.memberIcon.text = "👑"
        }
    }

    override fun getItemCount(): Int = members.size

    fun submitList(newMembers: List<String>) {
        members = newMembers
        notifyDataSetChanged()
    }
}