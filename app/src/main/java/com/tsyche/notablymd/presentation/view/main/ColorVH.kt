package com.tsyche.notablymd.presentation.view.main

import android.content.res.ColorStateList
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.tsyche.notablymd.R
import com.tsyche.notablymd.data.model.BaseNote
import com.tsyche.notablymd.databinding.RecyclerColorBinding
import com.tsyche.notablymd.presentation.dp
import com.tsyche.notablymd.presentation.extractColor
import com.tsyche.notablymd.presentation.getColorFromAttr
import com.tsyche.notablymd.presentation.getContrastFontColor
import com.tsyche.notablymd.presentation.view.misc.ItemListener

class ColorVH(private val binding: RecyclerColorBinding, listener: ItemListener) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.CardView.setOnClickListener { listener.onClick(absoluteAdapterPosition) }
        binding.CardView.setOnLongClickListener {
            listener.onLongClick(absoluteAdapterPosition)
            true
        }
    }

    fun bind(color: String, isSelected: Boolean) {
        val showAddIcon = color == BaseNote.COLOR_NEW
        val context = binding.root.context
        val value =
            if (showAddIcon) context.getColorFromAttr(R.attr.colorOnSurface)
            else context.extractColor(color)
        val controlsColor = context.getContrastFontColor(value)
        binding.apply {
            CardView.apply {
                setCardBackgroundColor(value)
                contentDescription = color
                if (isSelected) {
                    strokeWidth = 4.dp
                    strokeColor = controlsColor
                } else {
                    strokeWidth = 1.dp
                    strokeColor = controlsColor
                }
            }
            CardIcon.apply {
                if (showAddIcon) {
                    setImageResource(R.drawable.add)
                } else if (isSelected) {
                    setImageResource(R.drawable.checked_circle)
                }
                imageTintList = ColorStateList.valueOf(controlsColor)
                isVisible = showAddIcon || isSelected
            }
        }
    }
}
