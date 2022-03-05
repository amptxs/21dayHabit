package com.cwl.habbitformation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwl.habbitformation.adapters.RecyclerViewHabbitAdapter
import com.cwl.habbitformation.adapters.RecyclerViewTouchHelper
import com.cwl.habbitformation.models.Habbit
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val recyclerAdapter by lazy{
        RecyclerViewHabbitAdapter()
    }
    private val recyclerTouchHelper by lazy {
        RecyclerViewTouchHelper(recyclerAdapter, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        habbitContainer.apply {
            adapter = recyclerAdapter
            layoutManager= LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
        habbitContainer.setItemViewCacheSize(100)
        ItemTouchHelper(recyclerTouchHelper.ItemTouchCallback).attachToRecyclerView(habbitContainer)


        val habit1 = Habbit("Заняться спортом", "тест", Calendar.getInstance().time,
            Calendar.getInstance().time, 21)
        habit1.Progress = 10

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val d = sdf.parse("28/02/2022")
        val habit2 = Habbit("Здорово питаться", "тест", d, d, 21)
        habit2.Progress = 7

        val habit3 = Habbit("Читать каждый день", "тест", Calendar.getInstance().time,
            Calendar.getInstance().time, 21)
        habit3.Progress = 21

        addItemTop(habit1)
        addItemTop(habit2)
        addItemTop(habit3)

        AddButton.setOnClickListener(){
            addItemTop(habit1)
        }


    }

    fun addItemTop(model: Habbit){
        //dbList.add(model) TODO
        recyclerAdapter.addItemTop(model)
        refreshIntroduction()
    }

    fun removeAndNotify(viewHolder: RecyclerViewHabbitAdapter.ViewHolder){
        //dbList.removeAt(model) TODO

        var removedItemPosition = viewHolder.adapterPosition
        var item = recyclerAdapter.getItem(removedItemPosition)
        recyclerAdapter.removeItemAt(removedItemPosition)

        val undoString = applicationContext.getString(R.string.undo)
        val deletedString = applicationContext.getString(R.string.deleted)

        Snackbar.make(viewHolder.itemView, "$deletedString  \"${item.Label}\"", Snackbar.LENGTH_LONG).setAction(undoString){
            recyclerAdapter.addItemAt(item,removedItemPosition)
            recyclerAdapter.notifyDataSetChanged()
            refreshIntroduction()
        }.show()

        refreshIntroduction()
    }

    fun refreshIntroduction(){

        introductionFirst.isGone = !(recyclerAdapter.itemCount == 0)
        introductionSecond.isGone = !(recyclerAdapter.itemCount == 0)

        if (recyclerAdapter.itemCount == 0)
            AddButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = MainFrame.height - (AddButton.height * 1.2).toInt()
            }
        else
            AddButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topMargin = 0
        }


    }
}