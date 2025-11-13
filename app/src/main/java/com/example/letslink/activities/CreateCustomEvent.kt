package com.example.letslink.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.letslink.R
import android.widget.EditText
import com.example.letslink.SessionManager
import android.widget.DatePicker
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.letslink.API_related.GroupRepo
import com.example.letslink.API_related.LetsLinkAPI
import com.example.letslink.local_database.EventDao
import com.example.letslink.local_database.GroupDao
import com.example.letslink.local_database.LetsLinkDB
import com.example.letslink.local_database.UserDao
import com.example.letslink.model.Event
import com.example.letslink.model.Group
import com.example.letslink.online_database.SyncDataManager
import com.example.letslink.online_database.fb_EventsRepo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class CreateCustomEventFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var repo: fb_EventsRepo
    private lateinit var groupRepo : GroupRepo
    private lateinit var db: LetsLinkDB
    private lateinit var groupDao: GroupDao
    private lateinit var eventDao : EventDao
    private lateinit var syncManager : SyncDataManager


    private val selectedGroupIds = mutableListOf<String>()
    private val groupList = mutableListOf<Group>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_create_custom_event, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getString(SessionManager.KEY_USER_ID, null)
        syncManager = SyncDataManager(requireContext())
        groupDao = LetsLinkDB.getDatabase(requireContext()).groupDao()
        sessionManager = SessionManager(requireContext())
        eventDao = LetsLinkDB.getDatabase(requireContext()).eventDao()
        repo = fb_EventsRepo(requireContext())

        lifecycleScope.launch{
            groupDao.getNotesByUserId(userId!!).collect{ groups ->
                groupList.clear()
                groupList.addAll(groups)
                Log.d("GroupList", groupList.count().toString())
            }

        }
        // Back Button Logic

        val backArrow: ImageView = view.findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        val tvSelectedGroups = view.findViewById<TextView>(R.id.tvSelectedGroups)
        tvSelectedGroups.setOnClickListener {
            showGroupSelectionDialog()
        }





        val createEventBtn = view.findViewById<View>(R.id.btnCreateEvent)
        createEventBtn.setOnClickListener {
            val eventTitle = view.findViewById<EditText>(R.id.etEventTitle).text.toString()
            val eventDescription =
                view.findViewById<EditText>(R.id.etEventDescription).text.toString()
            val eventStartTime = view.findViewById<EditText>(R.id.etStartTime).text.toString()
            val eventEndTime = view.findViewById<EditText>(R.id.etEndTime).text.toString()
            val eventLocation = view.findViewById<EditText>(R.id.etLocation).text.toString()
            val datepicker = view.findViewById<DatePicker>(R.id.datePicker)
            val day = datepicker.dayOfMonth
            val month = datepicker.month + 1
            val year = datepicker.year
            val date = "$day/$month/$year"


            if(syncManager.isInternetAvailable(requireContext())){
                //insert the event online then insert the event locally make sure to ensure the isSynced is true
                repo.createEvent(
                    eventTitle,
                    eventDescription,
                    eventLocation,
                    eventStartTime,
                    eventEndTime,
                    date,
                    userId!!,
                    selectedGroupIds,
                    true
                ) { isComplete, eventID ->
                    if (isComplete) {

                        Toast.makeText(context, "Event created successfully!", Toast.LENGTH_SHORT).show()
                        //insert it locally now
                        val localEvent = Event(
                            eventId = eventID,
                            ownerId = userId!!,
                            title = eventTitle,
                            description = eventDescription,
                            location = eventLocation,
                            startTime = eventStartTime,
                            endTime = eventEndTime,
                            date = date,
                            groups = selectedGroupIds,
                            isSynced = true
                        )
                        viewLifecycleOwner.lifecycleScope.launch {
                            try{
                                eventDao.addEventLocally(localEvent)
                                Toast.makeText(context, "Event created successfully!(online)", Toast.LENGTH_SHORT).show()

                            }catch(e: Exception){
                                Toast.makeText(context, "Event creation failed!", Toast.LENGTH_SHORT).show()
                                Log.d("check-error",e.toString())

                            }


                        }
                        view.postDelayed({
                            parentFragmentManager.popBackStack()
                        }, 1000)

                    } else {
                        Toast.makeText(context, "Event creation failed!", Toast.LENGTH_SHORT).show()
                    }
                }
                Toast.makeText(context, "Event creation initiated!", Toast.LENGTH_SHORT).show()
            }else{
                //insert the event locally
                val localEvent = Event(
                    eventId = UUID.randomUUID().toString(),
                      ownerId = userId!!,
                      title = eventTitle,
                      description = eventDescription,
                      location = eventLocation,
                      startTime = eventStartTime,
                      endTime = eventEndTime,
                      date = date,
                      groups = selectedGroupIds,
                    isSynced = false
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        eventDao.addEventLocally(localEvent)
                        Toast.makeText(context, "Event created successfully!(offline)", Toast.LENGTH_SHORT).show()
                        view.postDelayed({
                            parentFragmentManager.popBackStack()
                        }, 1000)
                    }catch (e:Exception){
                        Log.d("check-error",e.toString())
                        Toast.makeText(context, "Event creation failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }


    }

    private fun showGroupSelectionDialog() {
        val groupName = groupList.map { it.groupName }.toTypedArray()
        val checkedItems = BooleanArray(groupList.size) { i ->
            selectedGroupIds.contains(groupList[i].groupId.toString())
        }

        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Groups")
        builder.setMultiChoiceItems(groupName, checkedItems) { _, which, isChecked ->
            val group = groupList[which]
            if (isChecked) {
                if (!selectedGroupIds.contains(group.groupId.toString())) {
                    selectedGroupIds.add(group.groupId.toString())
                }
            } else {
                selectedGroupIds.remove(group.groupId.toString())
            }
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            val selectedName = groupList.filter { selectedGroupIds.contains(it.groupId.toString()) }
                .map { it.groupName }

            val tvSelectedGroups = view?.findViewById<TextView>(R.id.tvSelectedGroups)
            if (selectedName.isEmpty()) {
                tvSelectedGroups?.setText("Select Groups")
            } else {
                tvSelectedGroups?.setText(selectedName.toString())
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}

