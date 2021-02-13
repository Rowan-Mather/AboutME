package com.example.aboutme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
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
                R.id.mainActivity -> { startActivity(Intent(this, MainActivity::class.java)) }
                R.id.sleep -> { startActivity(Intent(this, Sleep::class.java)) }
                R.id.symptoms -> { startActivity(Intent(this, Symptoms::class.java)) }
                R.id.graphs -> { startActivity(Intent(this, Graphs::class.java)) }
            }
            overridePendingTransition(0,0)
            true
        }

        //sets the size and position of the calendar relative to the size of the screen & its orientation
        val metrics = DisplayMetrics()
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics)
        val screenWidth = metrics.widthPixels.toFloat()
        val screenHeight = metrics.heightPixels.toFloat()
        var scaleFactor: Float
        val absoluteViewHeight: Float = (screenHeight - ((137 * getResources().getDisplayMetrics().density + 0.5f) + 0.22*screenHeight).toFloat())
        if (screenWidth <= screenHeight)
        {
            scaleFactor = (screenWidth/520)
            calendarView.x = (screenWidth/3.4).toFloat()
            calendarView.y = (absoluteViewHeight/2.5).toFloat()
        }
        else
        {
            scaleFactor = (screenHeight/720)
            calendarView.x = (screenWidth/2.65).toFloat()
            calendarView.y = (absoluteViewHeight/2.8).toFloat()
        }
        calendarView.scaleX = scaleFactor
        calendarView.scaleY = scaleFactor

        //initialises the variable selectedDate as the date that is highlighted on the calendar and checks for data
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var selectedDate = sdf.format(calendarView.date).toString()
        showData(selectedDate)

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
                showData(selectedDate)

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

    //displays the data for the selected day below the calendar
    fun showData(selectedDate: String)
    {
        if (selectedDate in prefs.dateList)
        {
            var dateInfo = prefs.readAll(selectedDate)
            dataTextView.text = formatData(dateInfo.toString())
        }
        else { dataTextView.text = "" }
    }

    //this function formats the shared preferences into the user-friendly calendar display
    fun formatData(data: String): SpannableString
    {
        /*
        the final product is a bullet pointed list of every attribute (labelled)
        the bullet points are colour coded based on the attributes category e.g. sleep attributes have purple bullet points
        in order to achieve this there is one string that is formatted as such but the bullet points
            are replaced with arbitrary category markers ⓪, ①, ② etc (newString)
        another string is the same but has the bullet points ￭ uncoloured (bulletString)
        a spannable string is then created from bulletString (that has the potential to be coloured) (newSpannableString)
        newString is iterated through and when a marker is found, newSpannableString is changed at that location
            to be the correct colour
        newSpannable string is returned
        */
        var newString = ""
        var bulletString = ""
        var categories = data.split(";")

        //for each of the categories, the attributes are placed on a new line and
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
                //if the attribute is symptoms, it is formatted nicely into a list
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
                '⓪' -> bulletColour = ForegroundColorSpan(Color.rgb(112, 200, 255)) //Activity
                '①' -> bulletColour = ForegroundColorSpan(Color.rgb(183, 125, 255)) //Sleep
                '②' -> bulletColour = ForegroundColorSpan(Color.rgb(165, 240, 192)) //Symptoms
            }
            newSpannableString.setSpan(bulletColour, i,i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return newSpannableString
    }
}
