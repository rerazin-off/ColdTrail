package com.example.mrdetective.data.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private fun connected() = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
            .setConstraints(connected())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("coldtrail_sync_once", ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<ScheduleOfflineSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(connected()).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "coldtrail_sync_periodic", ExistingPeriodicWorkPolicy.UPDATE, request,
        )
    }
}

class ScheduleOfflineSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        SyncScheduler.enqueue(applicationContext)
        return Result.success()
    }
}
