package com.tsyche.notablymd.presentation.activity.performance

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tsyche.notablymd.R
import com.tsyche.notablymd.databinding.ActivityPerformanceMonitorBinding
import com.tsyche.notablymd.presentation.adapter.PerformanceStatsAdapter
import com.tsyche.notablymd.utils.performance.GlobalPerformanceManager
import com.tsyche.notablymd.utils.performance.PerformanceStats
import kotlinx.coroutines.launch

/** Activity to monitor and display performance statistics */
class PerformanceMonitorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerformanceMonitorBinding
    private lateinit var statsAdapter: PerformanceStatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerformanceMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observePerformanceStats()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Performance Monitor"
        }
    }

    private fun setupRecyclerView() {
        statsAdapter = PerformanceStatsAdapter()
        binding.recyclerViewStats.apply {
            layoutManager = LinearLayoutManager(this@PerformanceMonitorActivity)
            adapter = statsAdapter
        }
    }

    private fun observePerformanceStats() {
        lifecycleScope.launch {
            try {
                val performanceManager =
                    GlobalPerformanceManager.getInstance(
                        // Get database instance - this would need to be passed or injected
                        throw NotImplementedError("Database access needed for performance manager")
                    )

                // Collect performance stats
                performanceManager.performanceStats.collect { stats -> stats?.let { updateUI(it) } }
            } catch (e: Exception) {
                // Fallback to global instances for demo
                updateDemoStats()
            }
        }
    }

    private fun updateUI(stats: PerformanceStats) {
        // Update cache stats
        binding.textViewCacheHitRate.text =
            "Cache Hit Rate: %.1f%%".format(stats.cacheStats.hitRate)
        binding.textViewCacheSize.text = "Cache Size: ${stats.cacheStats.cacheSize} notes"
        binding.textViewCacheRequests.text = "Total Requests: ${stats.cacheStats.totalRequests}"

        // Update index stats
        binding.textViewIndexedNotes.text = "Indexed Notes: ${stats.indexStats.totalIndexedNotes}"
        binding.textViewIndexReady.text =
            "Index Ready: ${if (stats.indexStats.isReady) "Yes" else "No"}"
        binding.textViewLastIndexTime.text =
            "Last Index: ${formatTime(stats.indexStats.lastIndexTime)}"

        // Update preloader stats
        binding.textViewPreloaderSuccessRate.text =
            "Preloader Success: %.1f%%".format(stats.preloaderStats.successRate)
        binding.textViewTrackedNotes.text = "Tracked Notes: ${stats.preloaderStats.trackedNotes}"
        binding.textViewActivePatterns.text =
            "Active Patterns: ${stats.preloaderStats.activePatterns}"

        // Update overall status
        binding.textViewOverallStatus.text =
            "Performance Status: ${if (stats.isReady) "Ready" else "Initializing"}"
        binding.textViewLastUpdate.text = "Last Update: ${formatTime(stats.lastUpdateTime)}"

        // Update detailed stats list
        val detailedStats =
            listOf(
                "Cache Statistics",
                "  Hit Rate: %.1f%%".format(stats.cacheStats.hitRate),
                "  Cache Size: ${stats.cacheStats.cacheSize} notes",
                "  Total Requests: ${stats.cacheStats.totalRequests}",
                "  Preloaded Items: ${stats.cacheStats.preloadedItems}",
                "",
                "Index Statistics",
                "  Total Indexed: ${stats.indexStats.totalIndexedNotes}",
                "  Title Index: ${stats.indexStats.titleIndexSize} entries",
                "  Body Index: ${stats.indexStats.bodyIndexSize} entries",
                "  Label Index: ${stats.indexStats.labelIndexSize} entries",
                "  Folder Index: ${stats.indexStats.folderIndexSize} entries",
                "  Full Text Index: ${stats.indexStats.fullTextIndexSize} entries",
                "",
                "Preloader Statistics",
                "  Total Preloads: ${stats.preloaderStats.totalPreloads}",
                "  Successful Preloads: ${stats.preloaderStats.successfulPreloads}",
                "  Preload Hits: ${stats.preloaderStats.preloadHits}",
                "  Success Rate: %.1f%%".format(stats.preloaderStats.successRate),
                "  Tracked Notes: ${stats.preloaderStats.trackedNotes}",
                "  Active Patterns: ${stats.preloaderStats.activePatterns}",
            )

        statsAdapter.updateStats(detailedStats)
    }

    private fun updateDemoStats() {
        // Demo stats for when database is not available
        binding.textViewCacheHitRate.text = "Cache Hit Rate: 85.2%"
        binding.textViewCacheSize.text = "Cache Size: 150 notes"
        binding.textViewCacheRequests.text = "Total Requests: 1,234"

        binding.textViewIndexedNotes.text = "Indexed Notes: 500"
        binding.textViewIndexReady.text = "Index Ready: Yes"
        binding.textViewLastIndexTime.text = "Last Index: 2 minutes ago"

        binding.textViewPreloaderSuccessRate.text = "Preloader Success: 92.1%"
        binding.textViewTrackedNotes.text = "Tracked Notes: 75"
        binding.textViewActivePatterns.text = "Active Patterns: 8"

        binding.textViewOverallStatus.text = "Performance Status: Ready (Demo Mode)"
        binding.textViewLastUpdate.text = "Last Update: Just now"

        val demoStats =
            listOf(
                "Demo Mode - Database Not Available",
                "",
                "Cache Statistics (Demo)",
                "  Hit Rate: 85.2%",
                "  Cache Size: 150 notes",
                "  Total Requests: 1,234",
                "  Preloaded Items: 50",
                "",
                "Index Statistics (Demo)",
                "  Total Indexed: 500",
                "  Title Index: 1,250 entries",
                "  Body Index: 3,750 entries",
                "  Label Index: 125 entries",
                "  Folder Index: 5 entries",
                "  Full Text Index: 5,000 entries",
                "",
                "Preloader Statistics (Demo)",
                "  Total Preloads: 45",
                "  Successful Preloads: 42",
                "  Preload Hits: 38",
                "  Success Rate: 92.1%",
                "  Tracked Notes: 75",
                "  Active Patterns: 8",
            )

        statsAdapter.updateStats(demoStats)
    }

    private fun formatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86_400_000 -> "${diff / 3600_000} hours ago"
            else -> "${diff / 86_400_000} days ago"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.performance_monitor_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_refresh -> {
                // Refresh stats
                true
            }
            R.id.action_clear_cache -> {
                // Clear cache
                true
            }
            R.id.action_rebuild_index -> {
                // Rebuild index
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
