package com.cwl.habbitformation.models

import android.content.Context
import android.graphics.Color
import android.provider.Settings.Global.getString
import android.text.format.DateFormat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cwl.habbitformation.R
import com.cwl.habbitformation.controllers.HabitDaoEntity
import java.io.Console
import java.io.Serializable
import java.security.AccessController.getContext
import java.sql.Time
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.time.Duration.Companion.hours

class Habit(label: String, description: String, created: Long, lastUpdate: Long?,
            duration: Int, notifyAt: Long?) : Serializable {
    var EntityId = 0
    var Label = label
    var Description = description
    var Created = Date(created)
    var LastUpdate =
        if (lastUpdate == null)
            null
        else
            Date(lastUpdate)
    var Duration = duration
    var NotifyAt = notifyAt
    var Progress: Int = 0
    var Position: Int? = 0

    fun isActive(): Boolean {
        return dateDifference() <= 2 && Progress != Duration
    }

    fun isDone(): Boolean {
        return Progress == Duration
    }

    fun isDelayed(): Boolean{
        return delayDateDifference() > 0
    }

    fun canBeDone(): Boolean{
        return if (LastUpdate == null) true
        else dateDifference() > 1
    }

    fun hadNotify():Boolean{
        return (NotifyAt != null)
    }

    fun markAsDone(){
        Progress +=1

        var date = Calendar.getInstance()
        date[Calendar.HOUR_OF_DAY] = 0
        date[Calendar.MINUTE] = 0
        date[Calendar.SECOND] = 0

        LastUpdate = date.time

        if (isDelayed())
            Created = date.time
    }

    fun getStatus(context: Context): String{
        return when {
            isDelayed() -> (context.getString(R.string.delayed) + " " + DateFormat.format("dd.MM", Created))
            isDone() -> context.getString(R.string.statusDone)
            dateDifference() <= 2 -> context.getString(R.string.statusActive)
            else -> context.getString(R.string.statusInactive)
        }

        return dateDifference().toString()
    }

    fun getStatusColor(context: Context): Int {
        return when {
            isDelayed() -> ContextCompat.getColor(context, R.color.delayed)
            dateDifference() <= 2 || isDone() -> ContextCompat.getColor(context, R.color.active)
            else -> ContextCompat.getColor(context, R.color.inactive)
        }
    }

    fun getLastUpdate(context: Context): CharSequence{
        return if (LastUpdate == null)
            context.getString(R.string.notMarked)
        else
            DateFormat.format("dd.MM.yyyy", LastUpdate)
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
        return if (Progress == Duration || dateDifference() > 2 ) 0.4f
        else 1.0f
    }

    private fun dateDifference(): Double{
        if (LastUpdate == null)
            return (Calendar.getInstance().time.time - Created.time).toDouble() / (1000 * 60 * 60 * 24)
        return (Calendar.getInstance().time.time - LastUpdate!!.time).toDouble() / (1000 * 60 * 60 * 24)
    }

    private fun delayDateDifference(): Double{
        return (Created.time - Calendar.getInstance().time.time).toDouble() / (1000 * 60 * 60 * 24)
    }

    fun millisToNormal(context: Context): String {
        return if (!hadNotify())
            context.getString(R.string.notSet)
        else
            String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(NotifyAt!!),
                TimeUnit.MILLISECONDS.toMinutes(NotifyAt!!) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(NotifyAt!!)))
    }

    fun getNotifyHours(): Long{
        return TimeUnit.MILLISECONDS.toHours(NotifyAt!!)
    }

    fun getNotifyMinutes(): Long{
        return TimeUnit.MILLISECONDS.toMinutes(NotifyAt!!) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(NotifyAt!!))
    }

    fun getTimeToNotify():Long{
        var notifyDate = Calendar.getInstance()
        notifyDate[Calendar.HOUR_OF_DAY] = getNotifyHours().toInt()
        notifyDate[Calendar.MINUTE] = getNotifyMinutes().toInt()
        notifyDate[Calendar.SECOND] = 0

        return (notifyDate.timeInMillis + (1000 * 60 * 60 * 24)) - Calendar.getInstance().timeInMillis
    }

    fun getTimeToNotifyOnCreate():Long{
        var notifyDate = Calendar.getInstance()
        notifyDate[Calendar.HOUR_OF_DAY] = getNotifyHours().toInt()
        notifyDate[Calendar.MINUTE] = getNotifyMinutes().toInt()
        notifyDate[Calendar.SECOND] = 0

        return when {
            isDelayed() -> (notifyDate.timeInMillis + (1000 * 60 * 60 * 24 * delayDateDifference().toInt())) - Calendar.getInstance().timeInMillis
            notifyDate.time > Calendar.getInstance().time -> (notifyDate.timeInMillis - Calendar.getInstance().timeInMillis)
            else -> (notifyDate.timeInMillis + (1000 * 60 * 60 * 24)) - Calendar.getInstance().timeInMillis
        }
    }

    fun getHabitAsWorkTag(): String{
        return (EntityId).hashCode().toString()
    }

    fun castToEntity(): HabitDaoEntity{
        var entity = HabitDaoEntity()
        entity.label = Label
        entity.description = Description
        entity.created = Created.time
        entity.lastUpdate =
        if (LastUpdate == null)
            null
        else
            LastUpdate!!.time
        entity.duration = Duration
        entity.notifyAt = NotifyAt
        entity.progress = Progress

        return entity
    }
}