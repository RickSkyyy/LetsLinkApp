package com.example.letslink.tickets

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.R
import com.example.letslink.databinding.ItemTicketBinding

class TicketAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketVH>() {

    private val tickets = mutableListOf<Ticket>()

    fun submitList(list: List<Ticket>) {
        tickets.clear()
        tickets.addAll(list)
        notifyDataSetChanged()
    }

    inner class TicketVH(val binding: ItemTicketBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketVH {
        val binding = ItemTicketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TicketVH(binding)
    }

    override fun getItemCount(): Int = tickets.size

    override fun onBindViewHolder(holder: TicketVH, position: Int) {
        val ticket = tickets[position]

        // Left icon
        holder.binding.iconTicket.setImageResource(R.drawable.ic_ticket)

        // Ticket name
        holder.binding.ticketTitle.text = ticket.fileName

        // Right icon: PDF or image
        holder.binding.iconFileType.setImageResource(
            if (ticket.fileName.endsWith(".pdf")) R.drawable.ic_pdf else R.drawable.ic_image
        )

        // Click to view ticket
        holder.binding.root.setOnClickListener {
            onClick(ticket.uri)
        }

        // Click edit button
        holder.binding.btnEditTicket.setOnClickListener {
            // Open a simple dialog to rename
            val context = holder.itemView.context
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            val input = android.widget.EditText(context).apply {
                setText(ticket.fileName)
            }

            builder.setTitle("Rename Ticket")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newName = input.text.toString()
                    TicketStorage.renameTicket(context, ticket.uri, newName)
                    ticket.fileName = newName
                    notifyItemChanged(position)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

}
