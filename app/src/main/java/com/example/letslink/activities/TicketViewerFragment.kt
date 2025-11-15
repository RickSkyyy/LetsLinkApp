package com.example.letslink.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.letslink.databinding.FragmentTicketViewerBinding
import java.io.File

class TicketViewerFragment : Fragment() {

    private lateinit var binding: FragmentTicketViewerBinding
    private var filePath: String? = null

    companion object {
        fun newInstance(uri: String): TicketViewerFragment {
            val f = TicketViewerFragment()
            val b = Bundle()
            b.putString("uri", uri)
            f.arguments = b
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTicketViewerBinding.inflate(inflater, container, false)

        filePath = arguments?.getString("uri")

        if (filePath?.endsWith(".pdf", ignoreCase = true) == true) {
            showPdf()
        } else {
            showImage()
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    private fun showPdf() {
        binding.imageTicket.visibility = View.GONE
        binding.pdfView.visibility = View.VISIBLE

        try {
            val path = filePath ?: run {
                showError("File path is null")
                return
            }

            val file = File(path)

            if (!file.exists()) {
                showError("PDF file not found at: ${file.absolutePath}")
                return
            }

            binding.pdfView.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true)
                .spacing(0)
                .onError { throwable ->
                    showError("Error loading PDF: ${throwable.message}")
                    throwable.printStackTrace()
                }
                .onLoad { nbPages ->
                    // PDF loaded successfully
                }
                .load()

        } catch (e: Exception) {
            showError("Failed to open PDF: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showImage() {
        binding.pdfView.visibility = View.GONE
        binding.imageTicket.visibility = View.VISIBLE

        val path = filePath ?: return
        val file = File(path)

        if (file.exists()) {
            binding.imageTicket.setImageURI(Uri.fromFile(file))
        } else {
            showError("Image file not found")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}