package com.ssafy.final_pennant_preset

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.final_pennant.R

class CustomItemDecoration(context: Context): RecyclerView.ItemDecoration() {
//    lateinit var context: Context
//    lateinit var mDivider:Drawable
//    fun CustomItemDecoration(context:Context){
//        this.context=context
//        mDivider= context.getDrawable(R.drawable.gradient)!!
//    }
    var context = context
    var mDivider = context.getDrawable(R.drawable.gradient_recycler_divider)!!
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val widthMargin = 5f   // 좌우 Margin
        val height = 5f         // 사각형의 height

        val left = parent.paddingLeft.toFloat()
        val right = parent.width - parent.paddingRight.toFloat()
//        val paint = Paint().apply { color = Color.parseColor("#F4EAE0") }
        for(i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            val top = view.bottom.toFloat() + (view.layoutParams as RecyclerView.LayoutParams).bottomMargin
            val bottom = top + height   // 세로 길이 = 5 (bottom - top = height)

            // 좌표 (left, top) / (right, bottom) 값을 대각선으로 가지는 사각형
            mDivider.setBounds((left + widthMargin).toInt(), (top).toInt(), (right - widthMargin).toInt(), (bottom).toInt())
            mDivider.draw(c)
        }
    }
}
