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

//the compound view for the calendar date activity containing a text view and +/- buttons
class EditButtons : ConstraintLayout {
    private var range: Array<String>? = null
    private var selected: String? = null
    private var title: String = ""
    private var category: String = ""
    private var date: String = ""

    //initialises class as the compound view handler
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

        //sets listeners for +/- buttons
        editAdd.setOnClickListener{
            editButtonClicked(1)
        }
        editDelete.setOnClickListener{
            editButtonClicked(-1)
        }
    }

    //called once the button has being initialised
    //sets the range of the button, the colours & text & the data saving information
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

    //when one of the +/- buttons are clicked
    private fun editButtonClicked(change: Int)
    {
        if (range != null)
        {
            val ranged = range!!
            //if the attribute doesn't have an assigned value yet, it is set to the first or last
            //item in the range depending on whether it was the + or - button clicked
            if (selected == null)
            {
                selected = if (change > 0) { ranged[0] }
                else { ranged[ranged.lastIndex] }
            }
            //otherwise the current selected item is indexed and changed (if the change is allowed for the range)
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
            //shared preferences and the text view are updated
            setTextView()
            updateSelected()
        }
    }

    //if the attributes already has a stored value, it is loaded
    private fun loadSelected()
    {
        val data = prefs.readData(date, category, title)
        if (data != "ERROR: DATA NOT FOUND")
        {
            selected = data
            setTextView()
        }
    }

    //shared preferences is updated with the new value of the attribute
    private fun updateSelected()
    {
        if (date != "" && category != "" && title != "" && selected != null)
        {
            prefs.writeData(date, category, title, selected!!)
        }

    }
    //the text view is formatted to display the attribute name and its value
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
    //sets the colour of the + & - buttons
    private fun setButtonColour(colour: Int)
    {
        editAdd.backgroundTintList = ColorStateList.valueOf(colour)
        editDelete.backgroundTintList = ColorStateList.valueOf(colour)

    }
    //if the attribute has a numeric value, this is called to define the range
    private fun setRange(min: Int, max: Int)
    {
        val newRange = mutableListOf<String>()
        for (i in min ..  max)
        {
            newRange.add(i.toString())
        }
        range = newRange.toTypedArray()
    }
    //if the attribute has a non-numeric value, this is called to define the range
    private fun setRange(completeRange: Array<String>)
    {
        range = completeRange
    }
}
