package com.philkes.notablymd.presentation.view.note

import androidx.recyclerview.widget.RecyclerView
import com.philkes.notablymd.databinding.ErrorBinding
import com.philkes.notablymd.utils.FileError

class ErrorVH(private val binding: ErrorBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(error: FileError) {
        binding.Name.text = error.name
        binding.Description.text = error.description
    }
}
