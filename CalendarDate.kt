package com.example.aboutme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginLeft
import kotlinx.android.synthetic.main.calendar.*
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

        val allAttributes = mapOf(
            "Activity" to arrayOf("spoons"),
            "Sleep" to arrayOf("hours", "quality"),
            "Symptoms" to arrayOf("health", "fatigue", "symptoms")
        )
        val categoryColours = mapOf(
            "Activity" to resources.getColor(R.color.activity_title),
            "Sleep" to resources.getColor(R.color.sleep_title),
            "Symptoms" to resources.getColor(R.color.symptoms_title)
        )

        for (category in allAttributes.keys)
        {
            val title: TextView = TextView(this)
            title.text = category
            title.textSize = 25F
            title.setTextColor(categoryColours[category]!!)
            editLinear.addView(title)

        }
        val test: EditButtons = EditButtons(this)
        editLinear.addView(test)
        /*

        if (thisDate in prefs.dateList)
        {
            val dateInfo = prefs.readAll(thisDate.toString())
            dataTextView.text = dateInfo
        }
        else { dataTextView.text = "NO DATA" }

        //button to return the user to the calendar activity
        backButton.setOnClickListener {
            startActivity(Intent(this, Calendar::class.java))
        }

         */
    }
    fun formatDate(date: String): String
    {
        val dateSplit = date.split("-")
        val year = dateSplit[0]
        var month = dateSplit[1]
        val day = dateSplit[2]
        val months = arrayOf("January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December")
        month = months[month.toInt()-1]
        return "$day $month $year"
    }
}
