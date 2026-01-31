package com.tsyche.notablymd.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tsyche.notablymd.data.NotallyDatabase
import com.tsyche.notablymd.data.migration.MigrationConfig
import com.tsyche.notablymd.data.migration.MigrationEngine
import com.tsyche.notablymd.data.migration.MigrationProgress
import com.tsyche.notablymd.data.migration.MigrationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** ViewModel for handling migration operations */
class MigrationViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NotallyDatabase.getDatabase(getApplication(), false).value
    private val migrationEngine = MigrationEngine(application, database)

    private val _progress = MutableLiveData<MigrationProgress?>()
    val progress: LiveData<MigrationProgress?> = _progress

    private val _migrationResult = MutableLiveData<MigrationResult?>()
    val migrationResult: LiveData<MigrationResult?> = _migrationResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var migrationJob: Job? = null

    /** Start migration process */
    fun startMigration(config: MigrationConfig) {
        if (migrationJob?.isActive == true) {
            _errorMessage.value = "Migration is already in progress"
            return
        }

        migrationJob =
            viewModelScope.launch {
                try {
                    // Collect progress updates
                    migrationEngine.progress.collect { progress -> _progress.value = progress }

                    // Start migration
                    val result = migrationEngine.startMigration(config)
                    _migrationResult.value = result
                } catch (e: Exception) {
                    _errorMessage.value = "Migration failed: ${e.message}"
                    _migrationResult.value =
                        MigrationResult(
                            success = false,
                            totalNotes = 0,
                            successfulMigrations = 0,
                            failedMigrations = 0,
                            skippedMigrations = 0,
                            duration = 0,
                            errors = emptyList(),
                        )
                }
            }
    }

    /** Cancel migration */
    fun cancelMigration() {
        migrationJob?.cancel()
        viewModelScope.launch { migrationEngine.cancelMigration() }
    }

    /** Pause migration */
    fun pauseMigration() {
        viewModelScope.launch { migrationEngine.pauseMigration() }
    }

    /** Resume migration */
    fun resumeMigration() {
        viewModelScope.launch { migrationEngine.resumeMigration() }
    }

    /** Check if migration is active */
    fun isMigrationActive(): Boolean {
        return _progress.value?.isActive() == true
    }

    /** Clear results */
    fun clearResults() {
        _migrationResult.value = null
        _errorMessage.value = null
        _progress.value = null
    }

    override fun onCleared() {
        super.onCleared()
        migrationJob?.cancel()
    }
}
