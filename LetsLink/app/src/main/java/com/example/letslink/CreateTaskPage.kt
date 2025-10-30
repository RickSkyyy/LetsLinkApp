package com.example.letslink.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.letslink.R

class CreateTaskFragment : Fragment() {

    // 1. Inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // We use the layout file originally intended for the Activity
        return inflater.inflate(R.layout.activity_create_task_page, container, false)
    }

    // 2. Set up back navigation logic
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the back arrow button assuming the ID is 'backArrow' in the layout
        val backArrow: ImageView = view.findViewById(R.id.backArrow)

        backArrow.setOnClickListener {
            // Correct way for a Fragment to go back to the previous Fragment
            parentFragmentManager.popBackStack()
        }

        // Note: Additional view initialization (like EditTexts or other buttons)
        // would go here.
    }
}