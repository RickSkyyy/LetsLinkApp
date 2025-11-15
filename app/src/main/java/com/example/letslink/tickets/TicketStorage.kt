package com.example.letslink.tickets

import android.content.Context
import android.net.Uri
import java.io.File

object TicketStorage {

    // Save a new ticket
    fun saveTicket(context: Context, sourceUri: Uri): String {
        val dir = File(context.filesDir, "tickets")
        if (!dir.exists()) dir.mkdirs()

        val fileName = "ticket_${System.currentTimeMillis()}"
        val extension = getExtension(context, sourceUri)
        val file = File(dir, "$fileName.$extension")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }

    // Get all saved tickets
    fun getSavedTickets(context: Context): List<Ticket> {
        val dir = File(context.filesDir, "tickets")
        if (!dir.exists()) return emptyList()

        return dir.listFiles()?.map { file ->
            Ticket(
                uri = file.absolutePath,
                fileName = file.name
            )
        } ?: emptyList()
    }

    // Rename a ticket file
    fun renameTicket(context: Context, uriString: String, newName: String) {
        val oldFile = File(uriString)
        if (!oldFile.exists()) return

        val extension = oldFile.extension
        val dir = oldFile.parentFile
        val sanitizedNewName = newName.replace(Regex("[^a-zA-Z0-9._-]"), "_") // remove illegal chars
        val newFile = File(dir, "$sanitizedNewName.$extension")

        if (!newFile.exists()) {
            oldFile.renameTo(newFile)
        }
    }

    // Helper: get file extension
    private fun getExtension(context: Context, uri: Uri): String {
        val type = context.contentResolver.getType(uri) ?: return "bin"
        return when {
            type.contains("pdf") -> "pdf"
            type.contains("image") -> "jpg"
            else -> "bin"
        }
    }
}
