package com.cwl.habbitformation.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.cwl.habbitformation.R
import kotlinx.android.synthetic.main.activity_add_habit.*
import kotlinx.android.synthetic.main.materialcardview_habit.view.*
import kotlinx.android.synthetic.main.picker_item_layout.view.*

class RecyclerViewPickerAdapter: RecyclerView.Adapter<RecyclerViewPickerAdapter.ViewHolder>() {
    private var items: MutableList<String> = mutableListOf()


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(model: String) {
            itemView.apply{
                picker_item.text = model
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

    fun addItem(model: String){
        this.items.add(model)
        notifyItemInserted(itemCount - 1)
    }
    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is MarginLayoutParams) {
            val p = v.layoutParams as MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }
}
