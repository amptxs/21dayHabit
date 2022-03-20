package com.cwl.habbitformation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.cwl.habbitformation.activities.AddHabitActivity
import com.cwl.habbitformation.adapters.RecyclerViewHabitAdapter
import com.cwl.habbitformation.adapters.RecyclerViewTouchHelper
import com.cwl.habbitformation.models.Codes
import com.cwl.habbitformation.models.Habit
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.room.*
import com.cwl.habbitformation.controllers.HabitDao
import com.cwl.habbitformation.controllers.HabitDaoEntity
import com.google.android.material.snackbar.BaseTransientBottomBar

class MainActivity : AppCompatActivity() {

    private val recyclerAdapter by lazy{
        RecyclerViewHabitAdapter()
    }
    private val recyclerTouchHelper by lazy {
        RecyclerViewTouchHelper(recyclerAdapter, this)
    }

    @Database(entities = [HabitDaoEntity::class], version = 2)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun habitDAO(): HabitDao
    }

    private val dataBase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()
        //fallbackToDestructiveMigration()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        habitContainer.apply {
            adapter = recyclerAdapter
            layoutManager= LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
        habitContainer.setItemViewCacheSize(100)
        ItemTouchHelper(recyclerTouchHelper.ItemTouchCallback).attachToRecyclerView(habitContainer)



        val collection = dataBase.habitDAO().getAll()

        for (habit in collection)
            addItemTop(habit.castToNormal())
        AddButton.setOnClickListener(){
            openAddHabitActivity()
        }
    }

    fun moveItems(items: MutableList<Habit>){
//        var rawIndexes = mutableListOf<Int>()
//        var habitIndexes = mutableListOf<Int>()
//        var habits = dataBase.habitDAO().getAll()
//
//        for (item in items) {
//            rawIndexes.add(item.EntityId)
//            habitIndexes.add(item.EntityId)
//            Log.d("Raw and Name", item.EntityId.toString() + " " + item.Label)
//        }
//        habitIndexes.sort()
//
//        var habitsTemp = mutableListOf<HabitDaoEntity>()
//
//        for (i in habitIndexes.indices) {
//            var habit = dataBase.habitDAO().getById(rawIndexes[i])
//            habit.id = habitIndexes[i]
//            habitsTemp.add(habit)
//
//            Log.d("habitindex - > rawIndex", habitIndexes[i].toString() + "->" + rawIndexes[i].toString())
//        }
//
//        for (item in habitsTemp.indices) {
//            Log.d("finalIndex and finalName", habitsTemp[item].id.toString() + " " + habitsTemp[item].label)
//            dataBase.habitDAO().update(habitsTemp[item])
//        }
    }

    fun addItemTop(model: Habit){
        recyclerAdapter.addItemTop(model)
        refreshIntroduction()
    }

    fun removeAndNotify(viewHolder: RecyclerViewHabitAdapter.ViewHolder){

        var removedItemPosition = viewHolder.adapterPosition
        var item = recyclerAdapter.getItem(removedItemPosition)

        recyclerAdapter.removeItemAt(removedItemPosition)

        val undoString = applicationContext.getString(R.string.undo)
        val deletedString = applicationContext.getString(R.string.deleted)

        Snackbar.make(viewHolder.itemView, "$deletedString \n\"${item.Label}\"", Snackbar.LENGTH_LONG).setAction(undoString){
            recyclerAdapter.notifyDataSetChanged()
            refreshIntroduction()
        }.apply {
            this.view.background = resources.getDrawable(R.drawable.rectangle_round_corners, null)
            val tv = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            val btn = view.findViewById(com.google.android.material.R.id.snackbar_action) as Button
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            btn.setTextColor(resources.getColor(R.color.white, null))
            tv.setTextColor(resources.getColor(R.color.white, null))
        }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(
                transientBottomBar: Snackbar?,
                event: Int
            ) {
                super.onDismissed(transientBottomBar, event)
                dataBase.habitDAO().removeAt(item.EntityId)
            }
        }).show()


        refreshIntroduction()
    }

    private fun refreshIntroduction(){
        introductionFirst.isGone = recyclerAdapter.itemCount != 0
        introductionSecond.isGone = recyclerAdapter.itemCount != 0

        if (recyclerAdapter.itemCount == 0)
            AddButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = MainFrame.height - (AddButton.height * 1.2).toInt()
            }
        else
            AddButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topMargin = 0
            }
    }

    var resultActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Codes().ADD){
            val data: Intent? = it.data
            val newHabit = data?.getSerializableExtra("Object") as Habit
            newHabit.EntityId = dataBase.habitDAO().add(newHabit.castToEntity()).toInt()
            addItemTop(newHabit)
            Log.d("test", newHabit.EntityId.toString())

        }
        if (it.resultCode == Codes().EDIT){
            val data: Intent? = it.data
            val updatedHabit = data?.getSerializableExtra("ViewedItem") as Habit
            val index = data?.getSerializableExtra("Index") as Int
            recyclerAdapter.updateItemAt(updatedHabit, index)

            updateDao(updatedHabit)
        }
    }

    private fun openAddHabitActivity(){
        var intent = Intent(this, AddHabitActivity::class.java).putExtra("RequestCode",Codes().ADD)
        resultActivity.launch(intent)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = Codes().CHANNEL_ID
            val description: String = "21habitApp"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Codes().CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager: NotificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            )!!
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateDao(updatedHabit: Habit){
        var timeLastUpdate = if (updatedHabit.LastUpdate == null) null else updatedHabit.LastUpdate!!.time
        dataBase.habitDAO().update(updatedHabit.EntityId, updatedHabit.Label, updatedHabit.Description,
            updatedHabit.Created.time, timeLastUpdate, updatedHabit.Duration, updatedHabit.NotifyAt, updatedHabit.Progress)
    }
}