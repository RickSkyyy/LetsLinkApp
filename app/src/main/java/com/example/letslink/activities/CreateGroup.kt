package com.example.letslink.fragments

import android.accessibilityservice.GestureDescription

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.letslink.API_related.MyApp
import com.example.letslink.R
import com.example.letslink.viewmodels.GroupViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.letslink.local_database.GroupEvent


/**
 * Fragment responsible for creating new groups.
 */

class CreateGroupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    private var inviteLinkDialog: Dialog? = null
    private lateinit var viewModel: GroupViewModel
    private lateinit var addButton: Button
    private lateinit var etGroupName: EditText
    private lateinit var edDesc: EditText
    private lateinit var context: Context

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back Button Logic
        val backArrow: ImageView = view.findViewById(R.id.backArrow)
        backArrow.setOnClickListener {

            parentFragmentManager.popBackStack()
        }

        //require context is needed to view toast for fragmenets (Ariel ,202
        context = requireContext()
        //  Setup the Create Group Button Logic

        addButton = view.findViewById(R.id.btnCreateGroup)
        etGroupName = view.findViewById(R.id.etGroupName)
        edDesc = view.findViewById(R.id.etGroupDescription)
        val application = requireActivity().application
        val appContainer = (application as MyApp).container
        viewModel = ViewModelProvider(
            this,
            GroupViewModel.provideFactory(appContainer.groupRepository, appContainer.sessionManager)
        )[GroupViewModel::class.java]
        setupClickListeners()

    }

    override fun onDestroy() {
        super.onDestroy()
        inviteLinkDialog?.dismiss()
    }

    private fun setupClickListeners() {
        addButton.setOnClickListener{
            addNote(etGroupName,edDesc,context,viewModel)
        }
    }
    private fun addNote(edTitle: EditText, edDesc: EditText, context: Context, viewModel: GroupViewModel) {
        val title = edTitle.text.toString()
        val description = edDesc.text.toString()

        if (title.isBlank() || description.isBlank()) {
            Toast.makeText(context, "Title or description must not be empty", Toast.LENGTH_LONG).show()
            return
        }
        viewModel.onEvent(GroupEvent.setTitle(title))
        viewModel.onEvent(GroupEvent.setDecription(description))
        viewModel.onEvent(GroupEvent.createNote)

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noteState.collect { state ->
                    Log.d("group-creation-check", state.isSuccess.toString())
                    if (state.isSuccess) {

                        Toast.makeText(context, "Group created successfully!", Toast.LENGTH_LONG).show()

                        val inviteLink = state.inviteLink
                        if (!inviteLink.isNullOrEmpty()) {
                            showInviteLinkDialog(inviteLink)
                        }

                        parentFragmentManager.popBackStack()
                    } else if(state.errorMessage != null){
                        Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    /**
     * Displays a dialog with the invitation link and a copy button.
     */
    private fun showInviteLinkDialog(link: String) {
        // Prevent showing the dialog multiple times
        if (inviteLinkDialog != null && inviteLinkDialog!!.isShowing) {
            return
        }

//dialog

        //require context is needed to view toast for fragmenets (Ariel ,2020)
        //(Lindevs, 2020)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Group Created!")
        builder.setMessage("Share this unique invite link:\n\n$link")
            .setPositiveButton("Copy Link") { dialog, _ ->
                copyToClipboard(link)
                Toast.makeText(requireContext(), "Invite link copied to clipboard.", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Done") { dialog, _ ->
                dialog.dismiss()
            }

        inviteLinkDialog = builder.create()
        inviteLinkDialog!!.show()
    }

    /**
     * metjod to copy text to the system clipboard.
     */
    private fun copyToClipboard(text: String) {

        // Call getSystemService() on the Fragment's associated Activity

        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Group Invite Link", text)
        clipboard.setPrimaryClip(clip)
    }

}


