package com.cwl.habbitformation.adapters

import android.graphics.Canvas
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.cwl.habbitformation.MainActivity
import com.cwl.habbitformation.R

class RecyclerViewTouchHelper(recyclerViewHabitAdapter: RecyclerViewHabitAdapter,
                              mainActivity: MainActivity) {
    var adapter = recyclerViewHabitAdapter
    var parent = mainActivity

    val ItemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(UP or DOWN, LEFT) {

            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {

                val adapter = recyclerView.adapter as RecyclerViewHabitAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                adapter.moveItem(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                  direction: Int) {

                parent.removeAndNotify(viewHolder as RecyclerViewHabitAdapter.ViewHolder)

            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                var iconDrawable = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_baseline_delete_24)
                val itemViem = viewHolder.itemView

                val iconMargin = (itemViem.height - iconDrawable!!.intrinsicHeight) / 4

                iconDrawable?.setBounds(
                    itemViem.right  - iconDrawable.intrinsicWidth * 2 - iconMargin,
                    itemViem.top + iconMargin,
                    itemViem.right ,
                    itemViem.bottom - iconMargin)


                c.clipRect(itemViem.right + dX.toInt(), itemViem.top, itemViem.right, itemViem.bottom)
                iconDrawable?.draw(c)

                itemViem.width
                iconDrawable.alpha = (((itemViem.width - (dX  * -1)) / itemViem.width) * 100).toInt()

                c.restore()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ACTION_STATE_DRAG) {
                    animateViewHolder(viewHolder, 0.95f, 100)
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                animateViewHolder(viewHolder, 1.0f, 100)
            }

            fun animateViewHolder(viewHolder: RecyclerView.ViewHolder?, scale: Float, duration: Long){
                viewHolder?.itemView?.animate()?.scaleX(scale)?.scaleY(scale)?.setInterpolator(
                    AccelerateDecelerateInterpolator()
                )?.duration = duration;
            }

        }
}