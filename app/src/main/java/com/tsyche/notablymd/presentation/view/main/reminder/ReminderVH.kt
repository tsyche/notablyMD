package com.tsyche.notablymd.presentation.view.main.reminder

import androidx.recyclerview.widget.RecyclerView
import com.tsyche.notablymd.R
import com.tsyche.notablymd.data.model.Reminder
import com.tsyche.notablymd.data.model.toText
import com.tsyche.notablymd.databinding.RecyclerReminderBinding

class ReminderVH(
    private val binding: RecyclerReminderBinding,
    private val listener: ReminderListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(value: Reminder) {
        binding.apply {
            DateTime.text = value.dateTime.toText()
            Repetition.text =
                value.repetition?.toText(itemView.context)
                    ?: itemView.context.getText(R.string.reminder_no_repetition)
            EditButton.setOnClickListener { listener.edit(value) }
            DeleteButton.setOnClickListener { listener.delete(value) }
        }
    }
}
