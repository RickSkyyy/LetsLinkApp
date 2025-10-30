package com.example.letslink.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.letslink.EventVoting
import com.example.letslink.R


class GroupDetailsF : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_group_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val votingButton: LinearLayout = view.findViewById(R.id.btn_vote_on_events)

        val addTaskButton: LinearLayout = view.findViewById(R.id.btn_add_task)

        votingButton?.setOnClickListener {

            val intent = Intent(requireContext(), EventVoting::class.java)


            requireContext().startActivity(intent)
        }

        addTaskButton?.setOnClickListener {
            val createTaskFragment = CreateTaskFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, createTaskFragment)
                .addToBackStack(null)
                .commit()
        }
    }

}
