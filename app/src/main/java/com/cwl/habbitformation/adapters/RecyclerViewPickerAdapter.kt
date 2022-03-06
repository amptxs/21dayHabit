package com.cwl.habbitformation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cwl.habbitformation.R
import kotlinx.android.synthetic.main.materialcardview_habbit.view.*
import kotlinx.android.synthetic.main.picker_item_layout.view.*

class RecyclerViewPickerAdapter: RecyclerView.Adapter<RecyclerViewPickerAdapter.ViewHolder>() {
    private var items: MutableList<Int> = mutableListOf()


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(model: Int) {
            itemView.apply{
                picker_item.text = model.toString()
            }
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.picker_item_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = items[position]
        viewHolder.bind(item)
    }

    override fun getItemCount() = this.items.size

    fun addItem(model: Int){
        this.items.add(model)
        notifyItemInserted(itemCount - 1)
    }

}
