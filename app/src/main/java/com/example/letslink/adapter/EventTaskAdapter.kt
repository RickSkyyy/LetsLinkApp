package com.example.letslink.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.R
import com.example.letslink.model.Event
import com.example.letslink.model.Task

class EventTaskAdapter(
    private val context: Context,
    private val taskList: List<Task>,
    private val onStatusUpdated: (Task) -> Unit // callback for saving to DB or Firebase
) : RecyclerView.Adapter<EventTaskAdapter.EventViewHolder>() {


    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskHeader: TextView = itemView.findViewById(R.id.task_header)
        val taskDueDate: TextView = itemView.findViewById(R.id.task_due_date)
        val taskTitle: TextView = itemView.findViewById(R.id.task_title)
        val taskStatus: TextView = itemView.findViewById(R.id.task_status)

        val taskDuration: TextView = itemView.findViewById(R.id.task_duration)
        val btnInProgress: Button = itemView.findViewById(R.id.btn_In_progress)
        val btnComplete: Button = itemView.findViewById(R.id.btn_complete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_collab_task, parent, false) // replace with your actual XML name
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val task = taskList[position]

        // Display details
        holder.taskTitle.text = task.taskName
        holder.taskDueDate.text = "Due: ${task.dueDate}"
        if(task.taskDuration == null){
            holder.taskDuration.visibility = View.GONE
        }else {
            holder.taskDuration.text = "Time: ${task.taskDuration}hrs"
        }
            holder.taskStatus.text = task.taskStatus
        // Handle status and button state
        when (task.taskStatus.lowercase()) {
            "completed" -> {
                holder.btnComplete.isEnabled = false
                holder.btnInProgress.isEnabled = false
                holder.btnComplete.setBackgroundColor(Color.GRAY)
                holder.btnInProgress.setBackgroundColor(Color.GRAY)
            }
            "in-progress" -> {
                holder.btnInProgress.isEnabled = false
                holder.btnInProgress.setBackgroundColor(Color.DKGRAY)
                holder.btnComplete.isEnabled = true
                holder.btnComplete.setBackgroundColor(context.getColor(R.color.black))
            }
            else -> {
                holder.btnInProgress.isEnabled = true
                holder.btnComplete.isEnabled = true
                holder.btnInProgress.setBackgroundColor(context.getColor(R.color.midnight))
                holder.btnComplete.setBackgroundColor(context.getColor(R.color.black))
            }
        }

        // Handle clicks
        holder.btnInProgress.setOnClickListener {
            task.taskStatus = "in-progress"
            notifyItemChanged(position) // Refresh this item only
            onStatusUpdated(task)
        }

        holder.btnComplete.setOnClickListener {
            task.taskStatus = "completed"
            notifyItemChanged(position) // Refresh this item only
            onStatusUpdated(task)
        }


    }

    override fun getItemCount(): Int = taskList.size
}
