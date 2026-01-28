package com.tsyche.notablymd.presentation.view.note

import androidx.recyclerview.widget.RecyclerView
import com.tsyche.notablymd.databinding.ErrorBinding
import com.tsyche.notablymd.utils.FileError

class ErrorVH(private val binding: ErrorBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(error: FileError) {
        binding.Name.text = error.name
        binding.Description.text = error.description
    }
}
