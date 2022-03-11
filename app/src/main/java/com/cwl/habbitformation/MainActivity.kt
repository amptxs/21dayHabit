package com.cwl.habbitformation

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwl.habbitformation.activities.AddHabitActivity
import com.cwl.habbitformation.adapters.RecyclerViewHabitAdapter
import com.cwl.habbitformation.adapters.RecyclerViewTouchHelper
import com.cwl.habbitformation.models.Habit
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val recyclerAdapter by lazy{
        RecyclerViewHabitAdapter()
    }
    private val recyclerTouchHelper by lazy {
        RecyclerViewTouchHelper(recyclerAdapter, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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


        val habit1 = Habit("Заняться спортом", "тест", Calendar.getInstance().time,
            Calendar.getInstance().time, 21, null)
        habit1.Progress = 10

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val d = sdf.parse("28/02/2022")
        val habit2 = Habit("Здорово питаться", "тест", d, d, 21, null)
        habit2.Progress = 7

        val habit3 = Habit("Начать бегать", "тест", Calendar.getInstance().time,
            Calendar.getInstance().time, 21, null)
        habit3.Progress = 21

        val habit4 = Habit("ЗАГОЛОВОК Щ В ДВЕ СТРОКИ", "тест", Calendar.getInstance().time,
            Calendar.getInstance().time, 21, null)
        habit4.Progress = 21

        //addItemTop(habit4)
        addItemTop(habit3)
        addItemTop(habit2)
        addItemTop(habit1)

        AddButton.setOnClickListener(){
            openAddHabitActivity()
        }


    }

    //move to data controller TODO
    fun addItemTop(model: Habit){
        //dbList.add(model) TODO
        recyclerAdapter.addItemTop(model)
        refreshIntroduction()
    }

    //move to data controller TODO
    fun removeAndNotify(viewHolder: RecyclerViewHabitAdapter.ViewHolder){
        //dbList.removeAt(model) TODO

        var removedItemPosition = viewHolder.adapterPosition
        var item = recyclerAdapter.getItem(removedItemPosition)
        recyclerAdapter.removeItemAt(removedItemPosition)

        val undoString = applicationContext.getString(R.string.undo)
        val deletedString = applicationContext.getString(R.string.deleted)

        Snackbar.make(viewHolder.itemView, "$deletedString \n\"${item.Label}\"", Snackbar.LENGTH_LONG).setAction(undoString){
            recyclerAdapter.addItemAt(item,removedItemPosition)
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
        }.show()

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

    var resultAddHabitActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == 1){
            val data: Intent? = it.data
            val newHabit = data?.getSerializableExtra("Object") as Habit
            addItemTop(newHabit)
        }
    }
    private fun openAddHabitActivity(){
        var intent = Intent(this, AddHabitActivity::class.java)
        resultAddHabitActivity.launch(intent)
    }

}