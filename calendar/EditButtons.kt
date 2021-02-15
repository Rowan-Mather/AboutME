package com.example.aboutme

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorSpace
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.edit_buttons.view.*

class EditButtons : ConstraintLayout {
    private var range: Array<String>? = null
    private var selected: String? = null
    private var title: String = ""
    private var category: String = ""
    private var date: String = ""

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

    fun setUp(button: EditButtonAttributes, colour: Int, thisDate: String, thisCategory: String)
    {
        if (button.isIntRange())
        { setRange(button.minimum, button.maximum) }
        else
        { setRange(button.range!!) }
        title = button.name
        date = thisDate
        category = thisCategory
        setButtonColour(colour)
        loadSelected()
        setTextView()
    }

    private fun editButtonClicked(change: Int)
    {
        if (range != null)
        {
            val ranged = range!!
            if (selected == null)
            {
                selected = if (change > 0) { ranged[0] }
                else { ranged[ranged.lastIndex] }
            }
            else
            {
                val newIndex: Int = ranged.indexOf(selected) + change
                if (newIndex < 0 || newIndex > ranged.lastIndex)
                {
                    Toast.makeText(context,"Outside the range", Toast.LENGTH_SHORT).show()
                }
                else
                { selected = ranged[newIndex] }
            }
            setTextView()
            updateSelected()
        }
    }

    private fun loadSelected()
    {
        val data = prefs.readData(date, category, title)
        println("yoyo $date, $category, $title")
        if (data != "ERROR: DATA NOT FOUND")
        {
            selected = data
            setTextView()
        }
    }

    private fun updateSelected()
    {
        if (date != "" && category != "" && title != "" && selected != null)
        {
            prefs.writeData(date, category, title, selected!!)
        }
    }
    private fun setTextView()
    {
        val upperTitle = title.capitalize()
        var data: String = " —"
        if (selected != null)
        {
            data = selected.toString()
        }
        editButtonTextView.text = "$upperTitle: $data"
    }
    /*
   fun setButtonColour(hex: String)
    {
        editAdd.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hex))
        editDelete.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hex))
    }*/
    private fun setButtonColour(colour: Int)
    {
        editAdd.backgroundTintList = ColorStateList.valueOf(colour)
        editDelete.backgroundTintList = ColorStateList.valueOf(colour)

    }
    private fun setRange(min: Int, max: Int)
    {
        val newRange = mutableListOf<String>()
        for (i in min ..  max)
        {
            newRange.add(i.toString())
        }
        range = newRange.toTypedArray()
    }
    private fun setRange(completeRange: Array<String>)
    {
        range = completeRange
    }
}
