package com.example.mrdetective.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/** Планирует разовую и периодическую синхронизацию через WorkManager. */
object SyncScheduler {
    private fun connected() = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    /** Запрашивает разовую синхронизацию при выполнении сетевых условий. */
    fun enqueue(context: Context) {
        val request =
            OneTimeWorkRequestBuilder<OfflineSyncWorker>()
                .setConstraints(connected())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
        WorkManager.getInstance(context).enqueueUniqueWork("coldtrail_sync_once", ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    /** Планирует периодическую проверку очереди синхронизации. */
    fun schedulePeriodic(context: Context) {
        val request =
            PeriodicWorkRequestBuilder<ScheduleOfflineSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(connected()).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "coldtrail_sync_periodic",
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}

/** Ставит попытку синхронизации в очередь при запуске периодической задачи. */
class ScheduleOfflineSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    /** Запрашивает синхронизацию и завершает периодическую задачу. */
    override suspend fun doWork(): Result {
        SyncScheduler.enqueue(applicationContext)
        return Result.success()
    }
}
