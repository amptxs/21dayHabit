package com.cwl.habbitformation.activities

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cwl.habbitformation.R
import com.cwl.habbitformation.adapters.RecyclerViewPickerAdapter
import com.cwl.habbitformation.controllers.OneTimeScheduleWorker
import com.cwl.habbitformation.models.Codes
import com.cwl.habbitformation.models.Habit
import kotlinx.android.synthetic.main.activity_add_habit.*
import kotlinx.android.synthetic.main.activity_view_habit.*
import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager
import java.lang.String
import java.util.*
import java.util.concurrent.TimeUnit


class AddHabitActivity : AppCompatActivity() {

    private var date = Calendar.getInstance()

    private var hoursNotify = 0
    private var minutesNotify = 0

    private var days = 0;

    private var requestCode = Codes().BACK
    private lateinit var editedHabit: Habit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        requestCode = intent.getSerializableExtra("RequestCode") as Int

        initializePicker(23,59)

        if (requestCode == Codes().ADD)
            requestAdd()
        else if (requestCode == Codes().EDIT)
            requestEdit()

        LabelInput.doOnTextChanged { text, start, before, count ->
            buttonSave.isEnabled = !LabelInput.text.isNullOrEmpty()
        }

        buttonSave.setOnClickListener {
            save()
        }

        buttonDateSelect.setOnClickListener {
            setDate()
        }

    }

    private fun requestAdd(){
        initializeSwitch(button21day)
        initializeCheckBox()

        buttonSave.isEnabled = false
    }

    private fun requestEdit()
    {
        editedHabit = intent.getSerializableExtra("ObjectToEdit") as Habit

        LabelInput.setText(editedHabit.Label)
        DescriptionInput.setText(editedHabit.Description)
        StartFromLayout.isGone = true

        when (editedHabit.Duration) {
            21 -> initializeSwitch(button21day)
            else -> initializeSwitch(button100days)
        }
        if (editedHabit.NotifyAt != null) {
            checkBoxRemind.isChecked = true
            heightAnimation(SliderLayout, SliderLayout.height, verticalSliderHours.height*2, 500)
            initializeCheckBox()
            verticalSliderHours.smoothScrollToPosition(editedHabit.getNotifyHours().toInt())
            verticalSliderMinutes.smoothScrollToPosition(editedHabit.getNotifyMinutes().toInt())
        }
        else
            initializeCheckBox()
        if (editedHabit.Progress >= 21)
        {
            setMargins(checkBoxRemind, 32, 0, 0, 0)
            switchLayout.isGone = true
        }

    }

    private fun initializeSwitch(button: Button){
        onSwitchClick(button)
        button21day.setOnClickListener {
            onSwitchClick(it)
        }
        button100days.setOnClickListener {
            onSwitchClick(it)
        }
    }

    private fun onSwitchClick(view: View){
        if (view.id == button21day.id){
            days = 21
            button21day.backgroundTintList = applicationContext.getResources().getColorStateList(R.color.element, null)
            button100days.backgroundTintList = applicationContext.getResources().getColorStateList(android.R.color.transparent, null)
        }
        else{
            days = 100
            button21day.backgroundTintList = applicationContext.getResources().getColorStateList(android.R.color.transparent, null)
            button100days.backgroundTintList = applicationContext.getResources().getColorStateList(R.color.element, null)
        }
    }

    private fun initializePicker(hours:Int, minutes:Int){
        val pickerLayoutManagerHours = PickerLayoutManager(this, PickerLayoutManager.HORIZONTAL, false)
        pickerLayoutManagerHours.isChangeAlpha = true
        pickerLayoutManagerHours.scaleDownBy = 0.70f
        pickerLayoutManagerHours.scaleDownDistance = 0.99f
        val snapHelperHours: SnapHelper = LinearSnapHelper()
        snapHelperHours.attachToRecyclerView(verticalSliderHours)

        val pickerLayoutManagerMinutes= PickerLayoutManager(this, PickerLayoutManager.HORIZONTAL, false)
        pickerLayoutManagerMinutes.isChangeAlpha = true
        pickerLayoutManagerMinutes.scaleDownBy = 0.70f
        pickerLayoutManagerMinutes.scaleDownDistance = 0.99f
        val snapHelperMinutes: SnapHelper = LinearSnapHelper()
        snapHelperMinutes.attachToRecyclerView(verticalSliderMinutes)


        var adapterHours = RecyclerViewPickerAdapter()
        var adapterMinutes = RecyclerViewPickerAdapter()

        verticalSliderHours.apply {
            adapter = adapterHours
            layoutManager = pickerLayoutManagerHours
        }

        var density = applicationContext.getResources().getDisplayMetrics().density
        var widthPixels = applicationContext.getResources().getDisplayMetrics().widthPixels

        //25 - picker item text size *2 is for two symbols
        //15 - picker left\right margin
        var padding = (widthPixels - addHabitLayout.paddingLeft- 25*density*2 - 15*density*2) / 2
        verticalSliderHours.setPadding(padding.toInt(),0,padding.toInt(),0)
        verticalSliderMinutes.setPadding(padding.toInt(),0,padding.toInt(),0)

        verticalSliderMinutes.apply {
            adapter = adapterMinutes
            layoutManager = pickerLayoutManagerMinutes
        }

        for (n in 0..hours)
            adapterHours.addItem(formattedString(n))

        for (n in 0..minutes)
            adapterMinutes.addItem(formattedString(n))

        heightAnimation(SliderLayout, SliderLayout.height, 1, 1)


        pickerLayoutManagerHours.setOnScrollStopListener { view ->
            hoursNotify = (view as TextView).text.toString().toInt()
        }
        pickerLayoutManagerMinutes.setOnScrollStopListener { view ->
            minutesNotify = (view as TextView).text.toString().toInt()
        }
    }

    private fun initializeCheckBox(){
        checkBoxRemind.setOnCheckedChangeListener { compoundButton, b ->


            if (checkBoxRemind.isChecked){
                heightAnimation(SliderLayout, SliderLayout.height, verticalSliderHours.height*2, 500)

                verticalSliderHours.smoothScrollToPosition(Calendar.getInstance().time.hours)
                verticalSliderMinutes.smoothScrollToPosition(Calendar.getInstance().time.minutes)

                hoursNotify = Calendar.getInstance().time.hours
                minutesNotify = Calendar.getInstance().time.minutes
            }
            else{
                heightAnimation(SliderLayout, SliderLayout.height, 1, 500)
            }

        }
    }

    private fun save(){
        var notifyTimeLong: Long? = timeToMilis(hoursNotify, minutesNotify)
        if (!checkBoxRemind.isChecked)
            notifyTimeLong = null



        when (requestCode) {
            Codes().ADD -> {
                var habit = Habit(LabelInput.text.toString(),DescriptionInput.text.toString().replace("\n", " "),
                    date.time, null, days, notifyTimeLong)
                var intentBack = Intent().putExtra("Object", habit)

                if (habit.hadNotify())
                    scheduleOneTimeNotification(habit.getTimeToNotifyOnCreate(), habit.getHabitAsWorkTag())

                setResult(Codes().ADD, intentBack)
                finish()
            }
            Codes().EDIT -> {
                editedHabit.Label = LabelInput.text.toString().replace("\n", " ")
                editedHabit.Description = DescriptionInput.text.toString()
                editedHabit.NotifyAt = notifyTimeLong
                editedHabit.Duration = days

                WorkManager.getInstance(baseContext).cancelAllWorkByTag(editedHabit.getHabitAsWorkTag())
                if (editedHabit.hadNotify())
                    scheduleOneTimeNotification(editedHabit.getTimeToNotify(),  editedHabit.getHabitAsWorkTag())

                var intentBack = Intent().putExtra("EditedItem", editedHabit)
                setResult(Codes().EDIT, intentBack)
                finish()
            }
        }


    }

    private fun heightAnimation(view: View, currentHeight: Int, newHeight:Int, duration: Long){
        val animator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(duration)
        animator.addUpdateListener {
            var value: Int = it.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }
        val animationSet = AnimatorSet()
        animationSet.setInterpolator(AccelerateDecelerateInterpolator())
        animationSet.play(animator)
        animationSet.start()
    }

    var d =
        OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            date[Calendar.YEAR] = year
            date[Calendar.MONTH] = monthOfYear
            date[Calendar.DAY_OF_MONTH] = dayOfMonth
            date[Calendar.HOUR_OF_DAY] = 0
            date[Calendar.MINUTE] = 0
            date[Calendar.SECOND] = 0

            buttonDateSelect.text = "${formattedString(date[Calendar.DAY_OF_MONTH])}" +
                    ".${formattedString(date[Calendar.MONTH])}"
        }

    private fun setDate() {
        var pickerDialog = DatePickerDialog(
            this,R.style.DialogTheme,d,
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH)
        )
        var picker = pickerDialog.datePicker
        picker.minDate=Calendar.getInstance().timeInMillis
        pickerDialog.show()
    }

    private fun formattedString(int: Int): kotlin.String{
        return String.format("%02d", int)
    }

    private fun timeToMilis(h: Int, m:Int): Long{
        var mill = (h * (1000*60*60) + m * (1000*60)).toLong()

        Log.d("test", String.format("%d hours, %d minutes",
            TimeUnit.MILLISECONDS.toHours(mill),
            TimeUnit.MILLISECONDS.toMinutes(mill) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mill))))

        return mill
    }

    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = v.layoutParams as ViewGroup.MarginLayoutParams
            val left = applicationContext.getResources().getDisplayMetrics().density.toInt() * l
            val right = applicationContext.getResources().getDisplayMetrics().density.toInt() * r
            val top = applicationContext.getResources().getDisplayMetrics().density.toInt() * t
            val bottom = applicationContext.getResources().getDisplayMetrics().density.toInt() * b
            p.setMargins(left, top, right, bottom)
            v.requestLayout()
        }
    }

    fun scheduleOneTimeNotification(initialDelay: Long, WORK_TAG: kotlin.String) {
        val work =
            OneTimeWorkRequestBuilder<OneTimeScheduleWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build()
        WorkManager.getInstance(baseContext).enqueue(work)
    }
}