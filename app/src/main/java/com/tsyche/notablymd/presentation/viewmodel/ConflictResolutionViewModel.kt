package com.tsyche.notablymd.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsyche.notablymd.data.sync.ConflictResolution
import com.tsyche.notablymd.data.sync.ConflictResolutionResult
import com.tsyche.notablymd.data.sync.ConflictResolver
import com.tsyche.notablymd.data.sync.SyncConflict
import kotlinx.coroutines.launch

/** ViewModel for handling conflict resolution logic */
class ConflictResolutionViewModel : ViewModel() {

    private val conflictResolver = ConflictResolver()

    private val _resolutionResult = MutableLiveData<ConflictResolutionResult?>()
    val resolutionResult: LiveData<ConflictResolutionResult?> = _resolutionResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /** Resolve a conflict using the specified resolution action */
    fun resolveConflict(conflict: SyncConflict, resolution: ConflictResolution) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = conflictResolver.applyResolution(conflict, resolution)
                _resolutionResult.value = result

                if (!result.success) {
                    _errorMessage.value = result.errorMessage ?: "Unknown error occurred"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Resolution failed: ${e.message}"
                _resolutionResult.value =
                    ConflictResolutionResult(
                        conflictId = conflict.noteId,
                        success = false,
                        action = resolution.action,
                        errorMessage = e.message,
                    )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Attempt auto-resolution for a conflict */
    fun autoResolveConflict(conflict: SyncConflict) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = conflictResolver.autoResolveConflict(conflict)
                _resolutionResult.value = result

                if (!result.success) {
                    _errorMessage.value = result.errorMessage ?: "Auto-resolution failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Auto-resolution failed: ${e.message}"
                _resolutionResult.value =
                    ConflictResolutionResult(
                        conflictId = conflict.noteId,
                        success = false,
                        action = ConflictResolution.ResolutionAction.MERGE_MANUAL,
                        errorMessage = e.message,
                    )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Clear the current resolution result */
    fun clearResult() {
        _resolutionResult.value = null
        _errorMessage.value = null
    }
}
