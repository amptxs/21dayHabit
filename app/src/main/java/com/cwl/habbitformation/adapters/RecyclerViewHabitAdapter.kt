package com.cwl.habbitformation.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cwl.habbitformation.MainActivity
import com.cwl.habbitformation.R
import com.cwl.habbitformation.activities.ViewHabitActivity
import com.cwl.habbitformation.models.Habit
import kotlinx.android.synthetic.main.materialcardview_habit.view.*


class RecyclerViewHabitAdapter: RecyclerView.Adapter<RecyclerViewHabitAdapter.ViewHolder>() {
    private var items: MutableList<Habit> = mutableListOf()
    private lateinit var thisContext: Context

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(model: Habit) {
            itemView.apply{
                thisContext = context
                itemView.alpha = model.getTransparency()

                Label.text = model.Label
                Status.text = model.getStatus(thisContext)
                Status.setTextColor(model.getStatusColor(thisContext))
                LastUpdate.text = model.getLastUpdateFormatted(thisContext)
                ProgressText.text = model.getProgressText(thisContext)
                ProgressBar.progress = model.getProgressBarValue()

                if (model.isActive() || model.isDone())
                    ProgressBar.progressDrawable.setTint(ContextCompat.getColor(context, R.color.active))
                else{
                    ProgressBar.progressDrawable.setTint(ContextCompat.getColor(context, R.color.inactive))
                    ProgressBar.progress = 100
                }

                var resultViewHabit = (context as MainActivity).resultActivity
                itemView.setOnClickListener {
                    val intent = Intent(context, ViewHabitActivity::class.java)
                        .putExtra("Object", items[adapterPosition])
                        .putExtra("Index", adapterPosition)
                    resultViewHabit.launch(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.materialcardview_habit, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = items[position]
        viewHolder.bind(item)
    }

    override fun getItemCount() = this.items.size

    fun addItem(model: Habit){
        this.items.add(model)
        notifyItemInserted(itemCount - 1)
    }
    fun addItemTop(model: Habit){
        this.items.add(0, model)
        notifyItemInserted(0)
    }

    fun addItemAt(model: Habit, position: Int) {
        this.items.add(position, model)
        notifyItemInserted(position)
    }

    fun removeItemAt(position: Int){
        this.items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getItem(position: Int): Habit{
        return this.items[position]
    }

    fun updateItemAt(habit: Habit, index: Int){
        items[index] = habit
        notifyItemChanged(index)
    }

    fun moveItem(from: Int, to: Int){
        val itemToMove = this.items[from]
        val index: Int = from

        this.items.removeAt(index)
        this.items.add(to, itemToMove)

        (thisContext as MainActivity).moveItems(this.items)

        notifyItemMoved(from, to)
    }
}