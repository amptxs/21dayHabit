package com.cwl.habbitformation.controllers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cwl.habbitformation.MainActivity
import com.cwl.habbitformation.R
import com.cwl.habbitformation.activities.ViewHabitActivity
import com.cwl.habbitformation.models.Codes
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class OneTimeScheduleWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private val CHANNEL_ID = Codes().CHANNEL_ID

    override fun doWork(): Result {
        val resultIntent = Intent(context, ViewHabitActivity::class.java).putExtra("fromNotify", true)
            .putExtra("EntityId",inputData.getInt("Id", 0))
        val pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_date_24)
            .setContentTitle(context.getString(R.string.habitNotification) + " " + inputData.getString("Label"))
            .setContentText(inputData.getString("Description"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(Random.nextInt(), builder.build())
        }

        return Result.success()
    }
}