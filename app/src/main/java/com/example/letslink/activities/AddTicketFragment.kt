package com.example.letslink.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.letslink.databinding.FragmentAddTicketBinding
import com.example.letslink.tickets.TicketStorage

class AddTicketFragment : Fragment() {

    private lateinit var binding: FragmentAddTicketBinding
    private var pickedUri: Uri? = null

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            pickedUri = it.data?.data
            binding.textFileName.text = pickedUri?.lastPathSegment ?: "Selected file"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTicketBinding.inflate(inflater, container, false)

        // Pick a file
        binding.btnPickFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))
            }
            filePicker.launch(intent)
        }

        // Save the picked ticket
        binding.btnSaveTicket.setOnClickListener {
            pickedUri?.let { uri ->
                // ✅ call the correct method
                TicketStorage.saveTicket(requireContext(), uri)
                parentFragmentManager.popBackStack() // go back to MyTicketsFragment
            }
        }

        return binding.root
    }
}
