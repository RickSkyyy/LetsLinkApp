package com.example.letslink.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView

import androidx.core.view.ViewCompat

import androidx.core.view.WindowInsetsCompat

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letslink.R
import com.example.letslink.adapter.TaskAdapter
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.local_database.TaskDao
import com.example.letslink.model.Task
import com.example.letslink.online_database.SyncDataManager
import com.example.letslink.online_database.fb_TaskRepo
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.launch

class ToDoFragment : Fragment() {
    companion object{
        private const val ARG_EVENT_ID = "eventId"
        fun newInstance(eventId : String) : ToDoFragment{
            val fragment = ToDoFragment()
            val bundle = Bundle().apply {
                putString(ARG_EVENT_ID, eventId)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
    private lateinit var taskAdapter : TaskAdapter
    private lateinit var fb_Taskrepo: fb_TaskRepo
    private lateinit var taskDao : TaskDao
    private lateinit var syncManager : SyncDataManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_to_do_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var tasks : List<Task> = emptyList()
        val eventId = arguments?.getString(ARG_EVENT_ID)
        val recyclerView : RecyclerView = view.findViewById(R.id.tasks_recyclerView)
        taskDao = LetsLinkDB.getDatabase(requireContext()).taskDao()
        syncManager = SyncDataManager(requireContext())

        //initialize an empty adapter
        taskAdapter = TaskAdapter(emptyList(),{})
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        if(syncManager.isInternetAvailable(requireContext())) {
            //fetched online tasks for event
            fb_Taskrepo = fb_TaskRepo(requireContext())
            lifecycleScope.launch {
                tasks = fb_Taskrepo.getTasksForEvent(eventId!!)
                taskAdapter.updateTasks(tasks)
                recyclerView.adapter = taskAdapter
            }
        }else{ //fetched local tasks for event
            lifecycleScope.launch {
                tasks = taskDao.getTasksForEvent(eventId!!)
                taskAdapter.updateTasks(tasks)
                recyclerView.adapter = taskAdapter
            }
        }

        // Back Button Logic
        val backArrow: ImageButton = view.findViewById(R.id.back_image_button)
        backArrow.setOnClickListener {

            parentFragmentManager.popBackStack()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

 }
}
