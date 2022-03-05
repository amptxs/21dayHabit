package com.cwl.habbitformation.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.cwl.habbitformation.R
import com.cwl.habbitformation.models.Habbit
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.materialcardview_habbit.view.*


class RecyclerViewHabbitAdapter: RecyclerView.Adapter<RecyclerViewHabbitAdapter.ViewHolder>() {
    private var items: MutableList<Habbit> = mutableListOf()
    private lateinit var thisContext: Context

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(model: Habbit) {
            itemView.apply{
                thisContext = context

                itemView.alpha = model.getTransparency()

                Label.text = model.Label
                Status.text = model.getStatus(thisContext)
                Status.setTextColor(model.getStatusColor(thisContext))
                LastUpdate.text = model.getLastUpdateFormatted(thisContext)
                ProgressText.text = model.getProgressText(thisContext)
                ProgressBar.progress = model.getProgressBarValue()

                if (model.isActive())
                    ProgressBar.progressDrawable.setTint(ContextCompat.getColor(context, R.color.active))
                else{
                    ProgressBar.progressDrawable.setTint(ContextCompat.getColor(context, R.color.inactive))
                    ProgressBar.progress = 100
                }

            }
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.materialcardview_habbit, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = items[position]
        viewHolder.bind(item)
    }

    override fun getItemCount() = this.items.size

    fun addItem(model: Habbit){
        this.items.add(model)
        notifyItemInserted(itemCount - 1)
    }
    fun addItemTop(model: Habbit){
        this.items.add(0, model)
        notifyItemInserted(0)
    }

    fun addItemAt(model: Habbit, position: Int) {
        this.items.add(position, model)
        notifyItemInserted(position)
    }

    fun removeItemAt(position: Int){
        this.items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getItem(position: Int): Habbit{
        return this.items[position]
    }

    fun moveItem(from: Int, to: Int){
        val itemToMove = this.items[from]
        val index: Int = from
        this.items.removeAt(index)
        this.items.add(to, itemToMove)
        notifyItemMoved(from, to)
    }
}