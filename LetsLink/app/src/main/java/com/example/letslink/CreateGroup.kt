package com.example.letslink.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.letslink.R

/**
 * Fragment responsible for creating new groups.
 */
class CreateGroupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment (fragment_create_group.xml)
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back Button Logic
        val backArrow: ImageView = view.findViewById(R.id.backArrow)
        backArrow.setOnClickListener {

            parentFragmentManager.popBackStack()
        }

        // 2. Setup the Create Group Button Logic (Example)
        val createGroupButton = view.findViewById<View>(R.id.btnCreateGroup)
        createGroupButton.setOnClickListener {
            Toast.makeText(context, "Group creation initiated!", Toast.LENGTH_SHORT).show()
        }


    }
}
