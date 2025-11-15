package com.example.letslink.fragments

import android.view.View
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class CenterSnapHelper : LinearSnapHelper() {

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        if (layoutManager !is CenterSnapLayoutManager) {
            return super.findSnapView(layoutManager)
        }

        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }

        val centerX = layoutManager.width / 2

        var closestChild: View? = null
        var closestDistance = Int.MAX_VALUE

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val childCenterX = (layoutManager.getDecoratedLeft(child) +
                    layoutManager.getDecoratedRight(child)) / 2
            val distance = kotlin.math.abs(centerX - childCenterX)

            if (distance < closestDistance) {
                closestDistance = distance
                closestChild = child
            }
        }

        return closestChild
    }
}