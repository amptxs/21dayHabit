package com.cwl.habbitformation.activities

import android.R.attr.button
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.cwl.habbitformation.R
import com.cwl.habbitformation.models.Habit
import kotlinx.android.synthetic.main.activity_view_habit.*
import kotlinx.android.synthetic.main.materialcardview_habit.*
import java.time.Duration
import java.util.*


class ViewHabitActivity : AppCompatActivity() {

    private lateinit var habit: Habit
    private var resultCode = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_habit)

        habit= intent.getSerializableExtra("Object") as Habit
        loadModel(habit)

        buttonEdit.isEnabled = false


        when {
            habit.isDelayed() -> {
                buttonEdit.isEnabled = true
                buttonDoneText.text = baseContext.getString(R.string.startNow)
                initializeDoneButton()
            }
            habit.isActive() && habit.canBeDone() -> {
                initializeDoneButton()
            }
            else -> {
                buttonDoneLayout.isGone = true
            }
        }


    }

    override fun onBackPressed() {
        var index = intent.getSerializableExtra("Index") as Int
        var intentBack = Intent().putExtra("ViewedItem", habit).putExtra("Index", index)
        setResult(resultCode, intentBack)
        finish()
        super.onBackPressed()
    }
    private fun loadModel(habit: Habit){
        viewLabel.text  = habit.Label
        viewDescription.text = habit.Description

        lastUpdateValue.text = habit.getLastUpdate(baseContext)
        statusValue.text = habit.getStatus(baseContext)
        statusValue.setTextColor(habit.getStatusColor(baseContext))
        progressValue.text = habit.getProgressText(baseContext)
        notifyValue.text = habit.millisToNormal(baseContext)
    }

    private fun initializeDoneButton(){
        var animatorIncrease = ValueAnimator.ofInt(buttonDone.progress, 100).setDuration(1000)
        var animatorDecrease= ValueAnimator.ofInt(buttonDone.progress, 0).setDuration(1000)

        buttonDone.setOnTouchListener { v, event ->
            buttonDone.performClick()

            var durationIncreaseValue = 1000 - buttonDone.progress * 10
            var durationDecreaseValue = 1000 - (100 - buttonDone.progress) * 10

            if (event.action == MotionEvent.ACTION_DOWN) {
                animatorIncrease = createAnimator(buttonDone.progress, 100, durationIncreaseValue)
                animatorDecrease.removeAllUpdateListeners()
                animatorDecrease.cancel()
                animatorIncrease.start()

            } else if (event.action == MotionEvent.ACTION_UP) {
                animatorDecrease = createAnimator(buttonDone.progress, 0, durationDecreaseValue)
                animatorIncrease.removeAllUpdateListeners()
                animatorIncrease.cancel()
                animatorDecrease.start()
            }
            true
        }
    }

    private fun createAnimator(from: Int, to: Int, duration: Int): ValueAnimator{
        var animator = ValueAnimator.ofInt(from, to)
            .setDuration(duration.toLong())
        animator.addUpdateListener {
            var value: Int = it.animatedValue as Int
            buttonDone.progress = value
            buttonDone.requestLayout()

            if (buttonDone.progress == 100)
            {
                buttonDone.progress = 0
                markAsDone()
            }
        }
        return animator
    }

    private fun markAsDone(){
        resultCode = 5

        habit.markAsDone()

        loadModel(habit)

        buttonDone.isGone = true
        buttonDoneText.text = baseContext.getString(R.string.todayMarked)
        buttonDoneText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        buttonEdit.isEnabled = true
    }
}