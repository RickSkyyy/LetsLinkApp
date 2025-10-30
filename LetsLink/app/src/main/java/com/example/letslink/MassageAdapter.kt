package com.example.letslink
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = message.text

        // Correctly casting to LinearLayout.LayoutParams
        val params = holder.tvMessage.layoutParams as LinearLayout.LayoutParams
        if (message.isMine) {
            // Align to the right for sender
            params.gravity = Gravity.END
            holder.tvMessage.setBackgroundResource(R.drawable.bg_message_sent)
        } else {
            // Align to the left for receiver
            params.gravity = Gravity.START
            holder.tvMessage.setBackgroundResource(R.drawable.bg_message_received)
        }
        holder.tvMessage.layoutParams = params
    }
}



