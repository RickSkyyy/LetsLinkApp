// MessagesAdapter.kt
package com.example.letslink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.R
import com.example.letslink.model.Message


class MessagesAdapter(
    private val messages: MutableList<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderID == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) holder.bind(message)
        else if (holder is ReceivedMessageViewHolder) holder.bind(message)
    }

    override fun getItemCount() = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // ---- Sent Message ViewHolder ----
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageBody: TextView = itemView.findViewById(R.id.tvMessage)
        private val messageTime: TextView = itemView.findViewById(R.id.tvTime)
        private val messageStatus: TextView = itemView.findViewById(R.id.tvSent)

        fun bind(message: Message) {
            messageBody.text = message.message
            messageTime.text = message.time
            messageStatus.text = "Sent"
        }
    }

    // ---- Received Message ViewHolder ----
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageBody: TextView = itemView.findViewById(R.id.tvMessage)
        private val messageTime: TextView = itemView.findViewById(R.id.tvTime)
        private val senderName: TextView? = itemView.findViewById(R.id.tvSenderName)

        fun bind(message: Message) {
            messageBody.text = message.message
            messageTime.text = message.time
            senderName?.text = message.messageSenderName
        }
    }
}
