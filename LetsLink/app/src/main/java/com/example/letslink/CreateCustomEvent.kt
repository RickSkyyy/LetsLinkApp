package com.example.letslink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment


class CreateCustomEventFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_create_custom_event, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back Button Logic
        val backArrow: ImageView = view.findViewById(R.id.backArrow)
        backArrow.setOnClickListener {

            parentFragmentManager.popBackStack()
        }

        val createGroupButton = view.findViewById<View>(R.id.btnCreateEvent)
        createGroupButton.setOnClickListener {
            Toast.makeText(context, "Event creation initiated!", Toast.LENGTH_SHORT).show()
        }


    }
}