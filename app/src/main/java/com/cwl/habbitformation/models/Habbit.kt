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
import java.security.AccessController.getContext
import java.time.Duration
import java.util.*

class Habbit(label: String, description: String, created: Date, lastUpdate: Date,
            duration: Int) {
    var Label = label
    var Description = description
    var Created = created
    var LastUpdate = lastUpdate
    var Duration = duration

    var Progress: Int = 0

    var DateDifference: Long = (Calendar.getInstance().time.time - LastUpdate.time) / (1000 * 60 * 60 * 24)


    fun getStatus(context: Context): String{
        return when {
            Progress == Duration -> context.getString(R.string.statusDone)
            DateDifference <= 1 -> context.getString(R.string.statusActive)
            else -> context.getString(R.string.statusInactive)
        }

        return DateDifference.toString()
    }

    fun isActive(): Boolean {
        return DateDifference <= 1 || Progress == Duration
    }

    fun getStatusColor(context: Context): Int {
        return when {
            DateDifference <= 1 || Progress == Duration -> ContextCompat.getColor(context, R.color.active)
            else -> ContextCompat.getColor(context, R.color.inactive)
        }
    }

    fun getLastUpdateFormatted(context: Context): CharSequence{
        return "${context.getString(R.string.marked)} " + DateFormat.format("dd.MM", LastUpdate)
    }

    fun getProgressBarValue(): Int {
        return (Progress.toDouble() / Duration * 100).toInt()
    }

    fun getProgressText(context: Context): String {
        return "${Progress.toString()} ${context.getString(R.string.outOf)} ${Duration.toString()}"
    }

    fun getTransparency(): Float {
        return if (Progress == Duration || DateDifference > 1 ) 0.4f
        else 1.0f
    }
}