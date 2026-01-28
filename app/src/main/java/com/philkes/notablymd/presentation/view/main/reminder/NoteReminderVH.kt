package com.philkes.notablymd.presentation.view.main.reminder

import androidx.recyclerview.widget.RecyclerView
import com.philkes.notablymd.R
import com.philkes.notablymd.data.dao.NoteReminder
import com.philkes.notablymd.data.model.findNextNotificationDate
import com.philkes.notablymd.data.model.toText
import com.philkes.notablymd.databinding.RecyclerNoteReminderBinding

class NoteReminderVH(
    private val binding: RecyclerNoteReminderBinding,
    private val listener: NoteReminderListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(value: NoteReminder) {
        binding.apply {
            Layout.setOnClickListener { listener.openReminder(value) }
            val context = itemView.context
            NoteTitle.text = value.title.ifEmpty { context.getText(R.string.empty_note) }
            val nextNotificationDate = value.reminders.findNextNotificationDate()
            NextNotification.text =
                nextNotificationDate?.let {
                    "${context.getText(R.string.next)}: ${nextNotificationDate.toText()}"
                } ?: context.getString(R.string.elapsed)
            Reminders.text = value.reminders.size.toString()
            OpenNote.setOnClickListener { listener.openNote(value) }
        }
    }
}
