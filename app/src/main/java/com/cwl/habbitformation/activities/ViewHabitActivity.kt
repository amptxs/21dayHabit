package com.cwl.habbitformation.activities

import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.*
import com.cwl.habbitformation.R
import com.cwl.habbitformation.controllers.HabitDao
import com.cwl.habbitformation.controllers.HabitDaoEntity
import com.cwl.habbitformation.controllers.OneTimeScheduleWorker
import com.cwl.habbitformation.models.Codes
import com.cwl.habbitformation.models.Habit
import kotlinx.android.synthetic.main.activity_add_habit.*
import kotlinx.android.synthetic.main.activity_view_habit.*
import kotlinx.android.synthetic.main.materialcardview_habit.*
import nl.dionsegijn.konfetti.core.*
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


class ViewHabitActivity : AppCompatActivity() {

    private lateinit var habit: Habit
    private var resultCode = Codes().BACK

    @Database(entities = [HabitDaoEntity::class], version = 3)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun habitDAO(): HabitDao
    }

    private val dataBase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_habit)

        var habitId= intent.getSerializableExtra("EntityId") as Int
        Log.d("habitId", habitId.toString())
        habit = dataBase.habitDAO().getById(habitId).castToNormal()
        loadModel(habit)
        checkHabitStatus()
        initializeEditButton()
    }

    override fun onBackPressed() {
        if (intent.getSerializableExtra("fromNotify") == true)
            exitProcess(0)

        var index = intent.getSerializableExtra("Index") as Int?
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

    private fun checkHabitStatus(){
        buttonEdit.isEnabled = false

        when {
            habit.isDelayed() -> {
                buttonEdit.isEnabled = true
                buttonDoneText.text = baseContext.getString(R.string.startNow)
                initializeDoneButton()
            }
            habit.isActive() && habit.canBeDone() ->
                initializeDoneButton()
            habit.isActive() && !habit.canBeDone() ->
            {
                doneToday()
            }
            else -> {
                buttonDoneLayout.isGone = true
            }
        }
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

    var resultActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Codes().EDIT){
            val data: Intent? = it.data
            val updatedHabit = data?.getSerializableExtra("EditedItem") as Habit
            habit = updatedHabit
            resultCode = Codes().EDIT
            loadModel(habit)

            updateDao(habit)
        }
    }

    private fun initializeEditButton(){
        buttonEdit.setOnClickListener {
            var intent = Intent(this, AddHabitActivity::class.java)
                .putExtra("RequestCode", Codes().EDIT)
                .putExtra("ObjectToEdit", habit)
            resultActivity.launch(intent)
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
        konfettiView.start(parade())

        resultCode = Codes().EDIT

        habit.markAsDone()

        updateDao(habit)

        loadModel(habit)
        doneToday()

        WorkManager.getInstance(baseContext).cancelAllWorkByTag(habit.getHabitAsWorkTag())
        if (habit.hadNotify())
            scheduleOneTimeNotification(habit)

    }

    fun scheduleOneTimeNotification(habit: Habit) {
        val myData = Data.Builder().putString("Label",habit.Label).putString("Description",habit.Description)
            .putInt("Id",habit.EntityId).build()
        val work =
            OneTimeWorkRequestBuilder<OneTimeScheduleWorker>()
                .setInitialDelay(habit.getTimeToNotify(), TimeUnit.MILLISECONDS)
                .addTag(habit.getHabitAsWorkTag())
                .setInputData(myData)
                .build()
        WorkManager.getInstance(baseContext).enqueue(work)
    }


    private fun doneToday(){
        buttonDone.isGone = true
        buttonDoneText.text = baseContext.getString(R.string.todayMarked)
        buttonDoneText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        buttonEdit.isEnabled = true

        if (habit.isDone())
            buttonEdit.isEnabled = false
    }

    private fun parade(): List<Party> {
        val density = applicationContext.getResources().getDisplayMetrics().density

        val party = Party(
            speed = 10f,
            maxSpeed = density * 10,
            damping = 0.95f,
            angle = Angle.RIGHT - 45,
            spread = Spread.SMALL,
            colors = listOf(0x52E3A6, 0xEE6A6A, 0xFFB800, 0x008EFB),
            emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(15),
            position = Position.Relative(0.0, 0.5)
        )

        return listOf(
            party,
            party.copy(
                angle = party.angle - 90,
                position = Position.Relative(1.0, 0.5)
            ),
        )
    }

    private fun updateDao(updatedHabit: Habit){
        var timeLastUpdate = if (updatedHabit.LastUpdate == null) null else updatedHabit.LastUpdate!!.time
        dataBase.habitDAO().update(updatedHabit.EntityId, updatedHabit.Label, updatedHabit.Description,
            updatedHabit.Created.time, timeLastUpdate, updatedHabit.Duration, updatedHabit.NotifyAt, updatedHabit.Progress)
    }
}
