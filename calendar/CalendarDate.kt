package com.example.aboutme

import android.R.attr.*
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.calendardate.*


class CalendarDate: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendardate)

        //the selected date from the calendar is received by this activity and displayed at the top
        val intent = intent
        val thisDate = intent.getStringExtra("selectedDate")
        dateView.text = formatDate(thisDate!!)

        //creates a dictionary of the categories and arrays of the attributes in them
        //each button is stored as an instance of the data class EditButtonAttributes
        //which creates the range of possible values that the user can select 
        val buttonAttributes = mapOf(
            "activity" to arrayOf(
                EditButtonAttributes("spoons",0, 15)
            ),
            "sleep" to arrayOf(
                EditButtonAttributes("hours", 0, 10),
                EditButtonAttributes("quality", arrayOf("Fitful","Poor", "Fair", "Restful", "Energising"))
            ),
            "symptoms" to arrayOf(
                EditButtonAttributes("health", 0, 10),
                EditButtonAttributes("fatigue", 0, 10)
                //EditButtonAttributes("symptoms", 0, 10)
            )
        )

        //dictionary of the colour coding of each category
        val categoryColours = mapOf(
            "activity" to ContextCompat.getColor(this, R.color.activity_graph),
            "sleep" to ContextCompat.getColor(this, R.color.sleep_graph),
            "symptoms" to ContextCompat.getColor(this, R.color.symptoms_graph)
        )

        //for each category, the category title is added and formatted
        for (category in buttonAttributes.keys)
        {
            val colour = categoryColours[category]!!
            val title: TextView = TextView(this)
            title.text = category.capitalize()
            title.textSize = 30F
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 30, 0, 3)
            title.layoutParams = params
            title.setTextColor(colour)
            editLinear.addView(title)
            //a custom button layout is generated for each attribute of the category using edit_buttons.xml
            //the button is customised by parsing in the appropriate EditButtonAttributes object, the 
            //selected date, and the category
            //if a value has already been stored for spoons, for example, the button name and category
            //is used within the editButton object to load it from sharedPreferences
            for (button in buttonAttributes[category]!!)
            {
                val editButton: EditButtons = EditButtons(this)
                editButton.setUp(button, categoryColours[category]!!, thisDate, category)
                editLinear.addView(editButton)
            }
        }

        //button to return the user to the calendar activity
        backButton.setOnClickListener {
            startActivity(Intent(this, Calendar::class.java))
        }

    }
    //formats a date of the form yyyy-mm-dd into [day] [month] [year]
    fun formatDate(date: String): String
    {
        val dateSplit = date.split("-")
        val year = dateSplit[0]
        var month = dateSplit[1]
        val day = dateSplit[2].toInt()
        val months = arrayOf("January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December")
        month = months[month.toInt()-1]
        return "$day $month $year"
    }
}
