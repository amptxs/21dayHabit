package com.cwl.habbitformation.models

import android.content.Context
import android.graphics.Color
import android.provider.Settings.Global.getString
import android.text.format.DateFormat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cwl.habbitformation.R
import java.io.Console
import java.io.Serializable
import java.security.AccessController.getContext
import java.sql.Time
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

class Habit(label: String, description: String, created: Date, lastUpdate: Date?,
            duration: Int, notifyAt: Long?) : Serializable {
    var Label = label
    var Description = description
    var Created = created
    var LastUpdate = lastUpdate
    var Duration = duration
    var NotifyAt = notifyAt
    var Progress: Int = 0



    fun getStatus(context: Context): String{
        return when {
            delayDateDifference() > 0 -> (context.getString(R.string.delayed) + " " +
                    DateFormat.format("dd.MM", Created))
            Progress == Duration -> context.getString(R.string.statusDone)
            dateDifference() <= 1 -> context.getString(R.string.statusActive)
            else -> context.getString(R.string.statusInactive)
        }

        return dateDifference().toString()
    }

    fun isActive(): Boolean {
        return dateDifference() <= 1 || Progress == Duration
    }

    fun getStatusColor(context: Context): Int {
        return when {
            delayDateDifference() > 0 -> ContextCompat.getColor(context, R.color.delayed)
            dateDifference() <= 1 || Progress == Duration -> ContextCompat.getColor(context, R.color.active)
            else -> ContextCompat.getColor(context, R.color.inactive)
        }
    }

    fun getLastUpdateFormatted(context: Context): CharSequence{
        return if (LastUpdate == null)
            context.getString(R.string.notMarked)
        else
            "${context.getString(R.string.marked)} " + DateFormat.format("dd.MM", LastUpdate)
    }

    fun getProgressBarValue(): Int {
        return (Progress.toDouble() / Duration * 100).toInt()
    }

    fun getProgressText(context: Context): String {
        return "${Progress.toString()} ${context.getString(R.string.outOf)} ${Duration.toString()}"
    }

    fun getTransparency(): Float {
        return if (Progress == Duration || dateDifference() > 1 ) 0.4f
        else 1.0f
    }

    private fun dateDifference(): Long{
        if (LastUpdate == null)
            return (Calendar.getInstance().time.time - Created.time) / (1000 * 60 * 60 * 24)
        return (Calendar.getInstance().time.time - LastUpdate!!.time) / (1000 * 60 * 60 * 24)
    }

    private fun delayDateDifference(): Double{
        return (Created.time - Calendar.getInstance().time.time).toDouble() / (1000 * 60 * 60 * 24)
    }

    fun millisToNormal(mill: Long)
    {
        Log.d("test", java.lang.String.format("%d hours, %d minutes",
            TimeUnit.MILLISECONDS.toHours(mill),
            TimeUnit.MILLISECONDS.toMinutes(mill) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mill))))
    }
}