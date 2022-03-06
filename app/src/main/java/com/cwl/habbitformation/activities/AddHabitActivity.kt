package com.cwl.habbitformation.activities

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationSet
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.cwl.habbitformation.R
import com.cwl.habbitformation.adapters.RecyclerViewPickerAdapter
import kotlinx.android.synthetic.main.activity_add_habit.*
import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager
import java.time.Duration
import java.util.*


class AddHabitActivity : AppCompatActivity() {

    private var days = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        initializeSwitch()
        initializePicker(23,59)
        initializeCheckBox()
    }

    private fun initializeSwitch(){
        onSwitchClick(button21day)
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

        verticalSliderMinutes.apply {
            adapter = adapterMinutes
            layoutManager = pickerLayoutManagerMinutes
        }

        for (n in 0..hours)
            adapterHours.addItem(n)

        for (n in 0..minutes)
            adapterMinutes.addItem(n)

        heightAnimation(SliderLayout, SliderLayout.height, 1, 1)


        pickerLayoutManagerHours.setOnScrollStopListener { view ->
            Log.d("Hours", (view as TextView).text.toString())
        }
        pickerLayoutManagerMinutes.setOnScrollStopListener { view ->
            Log.d("Minutes", (view as TextView).text.toString())
        }
    }

    private fun initializeCheckBox(){
        checkBoxRemind.setOnCheckedChangeListener { compoundButton, b ->
            verticalSliderHours.smoothScrollToPosition(Calendar.getInstance().time.hours)
            verticalSliderMinutes.smoothScrollToPosition(Calendar.getInstance().time.minutes)


            if (checkBoxRemind.isChecked){
                heightAnimation(SliderLayout, SliderLayout.height, verticalSliderHours.height*2, 500)
            }
            else{
                heightAnimation(SliderLayout, SliderLayout.height, 1, 500)
            }

        }
    }

    private fun heightAnimation(view: View, currentHeight: Int, newHeight:Int, duration: Long){
        val animator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(duration)
        animator.addUpdateListener {
            var value: Int = it.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
            Log.d("test", view.height.toString())
        }
        val animationSet = AnimatorSet()
        animationSet.setInterpolator(AccelerateDecelerateInterpolator())
        animationSet.play(animator)
        animationSet.start()
    }
}