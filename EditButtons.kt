package com.example.aboutme

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.edit_buttons.view.*

class EditButtons : ConstraintLayout {
    private lateinit var range: MutableList<String>
    private lateinit var selected: String
    private var title = ""
    @JvmOverloads
    constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context!!, attrs, defStyleAttr) {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context!!, attrs, defStyleAttr, defStyleRes) {
        init()
    }
    private fun init() {
        View.inflate(context, R.layout.edit_buttons, this)

        editAdd.setOnClickListener{
            editButtonClicked(1)
        }
        editDelete.setOnClickListener{
            editButtonClicked(-1)
        }
    }

    fun setText(text: String)
    {
        editButtonTextView.text = text
    }
    fun setTitle(text: String)
    {
        title = text
    }
    fun setButtonColour(hex: String)
    {
        editAdd.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hex))
        editDelete.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hex))
    }
    fun setRange(min: Int, max: Int)
    {
        range.clear()
        for (i in min ..  max)
        {
            range.add(i.toString())
        }
    }
    fun setRange(completeRange: Array<String>)
    {
        range = completeRange.toMutableList()
    }
    fun editButtonClicked(change: Int)
    {
        if (range != null)
        {
            if (selected == null)
            {
                selected = if (change > 0) { range[0] }
                else { range[range.lastIndex] }
            }
            else
            {
                val newIndex: Int = range.indexOf(selected) + change
                if (newIndex < 0 || newIndex > range.lastIndex)
                {
                    Toast.makeText(context,"Outside the range", Toast.LENGTH_SHORT).show()
                }
                else
                { selected = range[newIndex] }
            }
            setText("$title: $selected")
        }
    }

}
