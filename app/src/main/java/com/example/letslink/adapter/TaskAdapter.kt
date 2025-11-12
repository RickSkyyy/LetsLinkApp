package com.example.letslink.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.R
import com.example.letslink.model.Task

class TaskAdapter(
    private var tasks: List<Task>,
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.task_header)   // "Snacks:"
        private val dueDateText: TextView = itemView.findViewById(R.id.task_due_date) // "Due: ..."
        private val taskTitleText: TextView = itemView.findViewById(R.id.task_title)  // "Get Snacks..."
        private val taskForText: TextView = itemView.findViewById(R.id.task_status)      // "Anyone"
        private val taskDurationText: TextView = itemView.findViewById(R.id.task_duration) // "4 hours"

        fun bind(task: Task) {
            // Header: you can decide how to show it (using name or description)
            headerText.text = "${task.taskName}:"

            // Due Date
            dueDateText.text = "Due: ${task.dueDate}"

            // Task Title
            taskTitleText.text = task.taskDescription

            // For: (hard-coded "Anyone" unless you add field in Task model)
            taskForText.text = task.taskStatus

            // Duration
            taskDurationText.text = task.taskDuration

            // Click listener
            itemView.setOnClickListener {
                onItemClick(task)
                Toast.makeText(itemView.context, "Task clicked: ${task.taskName}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false) // <-- use your xml file name
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
