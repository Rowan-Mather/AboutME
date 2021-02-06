package com.example.aboutme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.calendar.*
import java.text.SimpleDateFormat

class Calendar: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationCalendar)
        bottomNavigationView.selectedItemId = R.id.calendar

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.sleep -> {
                    startActivity(Intent(this, Sleep::class.java))
                }
                R.id.symptoms -> {
                    startActivity(Intent(this, Symptoms::class.java))
                }
            }
            overridePendingTransition(0,0)
            true
        }

        //initialises the variable selectedDate as the date that is highlighted on the calendar and checks for data
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var selectedDate = sdf.format(calendarView.date).toString()
        if (selectedDate in prefs.dateList)
        {
            var dateInfo = prefs.readAll(selectedDate)
            dataTextView.text = formatData(dateInfo.toString())
        }
        else
        {
            dataTextView.text = ""
        }

        //when the user highlights a different date, selected date is updated and the relevant data displayed
        calendarView.setOnDateChangeListener(object : CalendarView.OnDateChangeListener {
            override fun onSelectedDayChange(c: CalendarView, thisyear: Int, thismonth: Int, thisday: Int) {
                //formatting the date to be in the form yyyy-mm-dd
                var thismonthplus = thismonth + 1
                var thisday2 = thisday.toString()
                var thismonth2 = thismonthplus.toString()
                var thisyear2 = thisyear.toString()
                if (thisday < 10){ thisday2 = "0"+thisday.toString() }
                if (thismonthplus < 10)
                { thismonth2 = "0"+thismonthplus.toString() }
                while (thisyear2.length < 4)
                { thisyear2 = "0"+thisyear2 }
                //selected date is updated
                selectedDate = "$thisyear2-$thismonth2-$thisday2"
                //the data for the selected day is displayed in the scroll bar
                if (selectedDate in prefs.dateList)
                {
                    var dateInfo = prefs.readAll(selectedDate)
                    dataTextView.text = formatData(dateInfo.toString())
                }
                else { dataTextView.text = "" }
            }
        })

        //if the edit button is clicked, the calendarDate activity is started
        editButton.setOnClickListener {
            val dateIntent = Intent(this, CalendarDate::class.java)
            //the selected date is passed into the new activity
            dateIntent.putExtra("selectedDate",selectedDate)
            startActivity(dateIntent)
        }
    }

    fun formatData(data: String): SpannableString
    {
        var newString = ""
        var bulletString = ""
        var categories = data.split(";")
        for (category in categories)
        {
            var attributes = category.split(",")
            var categoryMarker = ""
            when (attributes[0]) {
                "ACTIVITY" -> categoryMarker = "⓪ "
                "SLEEP" -> categoryMarker = "① "
                "SYMPTOMS" -> categoryMarker = "② "
            }
            for (i in 1..attributes.lastIndex)
            {
                var attributeSpaced = attributes[i].replace(":", ": ").capitalize()
                if (attributeSpaced.split(": ")[0] == "Symptoms")
                { attributeSpaced = attributeSpaced.replace("+", ", ") }
                newString += categoryMarker + attributeSpaced + "\n"
                bulletString += "￭ $attributeSpaced\n"
            }
        }
        var newSpannableString = SpannableString(bulletString)
        for (i in 0 until newString.lastIndex)
        {
            var bulletColour = ForegroundColorSpan(Color.BLACK)
            when (newString[i]) {
                '⓪' -> bulletColour = ForegroundColorSpan(Color.BLUE) //Activity
                '①' -> bulletColour = ForegroundColorSpan(Color.MAGENTA) //Sleep
                '②' -> bulletColour = ForegroundColorSpan(Color.GREEN) //Symptoms
            }
            newSpannableString.setSpan(bulletColour, i,i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return newSpannableString
    }
}
