package com.example.aboutme

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.calendar.*
import java.text.SimpleDateFormat
import java.util.*

class Calendar: AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

        //sets the size and position of the calendar
        // relative to the size of the screen & its orientation
        val metrics = DisplayMetrics()
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics)
        val screenWidth = metrics.widthPixels.toFloat()
        val screenHeight = metrics.heightPixels.toFloat()
        var scaleFactor: Float
        val absoluteViewHeight: Float =
            (screenHeight - ((137 * getResources().getDisplayMetrics().density + 0.5f) +
                    0.22*screenHeight).toFloat())
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

        //initialises the variable selectedDate as the date
        // that is highlighted on the calendar and checks for data
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var selectedDate = sdf.format(calendarView.date).toString()
        showData(selectedDate)

        //when the user highlights a different date,
        // selected date is updated and the relevant data displayed
        calendarView.setOnDateChangeListener { c, thisyear, thismonth, thisday ->
            //formatting the date to be in the form yyyy-mm-dd
            var thismonthplus = thismonth + 1
            var thisday2 = thisday.toString()
            var thismonth2 = thismonthplus.toString()
            var thisyear2 = thisyear.toString()
            if (thisday < 10){ thisday2 = "0$thisday" }
            if (thismonthplus < 10) { thismonth2 = "0$thismonthplus" }
            while (thisyear2.length < 4) { thisyear2 = "0$thisyear2" }
            //selected date is updated
            selectedDate = "$thisyear2-$thismonth2-$thisday2"
            //the data for the selected day is displayed in the scroll bar
            showData(selectedDate)
        }

        //the edit button is clicked
        editButton.setOnClickListener {
            //gets the selected date and the current date in unix time
            prefs.convertDate(prefs.getCurrentDate())
            val currentDate: Date = prefs.convertDate(prefs.getCurrentDate())
            val thisSelectedDate: Date = prefs.convertDate(selectedDate)
            //if the selected date is equal to or
            // after the current date, you can't edit the data
            if (thisSelectedDate > currentDate)
            {
                Toast.makeText(applicationContext,
                    "You cannot edit a future date",
                    Toast.LENGTH_SHORT).show()
            }
            else if (thisSelectedDate == currentDate)
            {
                Toast.makeText(applicationContext,
                    "You cannot edit today's data in the calendar",
                    Toast.LENGTH_SHORT).show()
            }
            //otherwise the calendarDate activity is started
            else {
                val dateIntent = Intent(this, CalendarDate::class.java)
                //the selected date is passed into the new activity
                dateIntent.putExtra("selectedDate", selectedDate)
                startActivity(dateIntent)
            }
        }

        //controls the back button, returns to a different page depending on
        //the last visited recorded in the PAGE setting
        calendarBackButton.setOnClickListener()
        {
            when (prefs.readSetting("PAGE"))
            {
                "activity" ->
                    startActivity(Intent(this, MainActivity::class.java))
                "sleep" ->
                    startActivity(Intent(this, Sleep::class.java))
                "symptoms" ->
                    startActivity(Intent(this, Symptoms::class.java))
                "diet" ->
                    startActivity(Intent(this, Diet::class.java))
                "medication" ->
                    startActivity(Intent(this, Medication::class.java))
                else ->
                    startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    //displays the data for the selected day below the calendar
    private fun showData(selectedDate: String)
    {
        if (selectedDate in prefs.dateList)
        {
            var dateInfo = prefs.readAll(selectedDate)
            dataTextView.text = formatData(dateInfo.toString())
        }
        else { dataTextView.text = "" }
    }

    //this function formats the shared preferences
    // into the user-friendly calendar display
    private fun formatData(data: String): SpannableString
    {
        /*
        the final product is a bullet pointed list of every attribute (labelled)
        the bullet points are colour coded based on the attributes category
        e.g. sleep attributes have purple bullet points
        in order to achieve this there is one string that is formatted as such
        but the bullet points are replaced with arbitrary
        category markers ⓪, ①, ② etc (newString)
        another string is the same but has the bullet points ￭ uncoloured (bulletString)
        a spannable string is then created from bulletString
        (that has the potential to be coloured) (newSpannableString)
        newString is iterated through and when a marker is found,
        newSpannableString is changed at that location
            to be the correct colour
        newSpannable string is returned
        */
        var newString = ""
        var bulletString = ""
        val categories = data.split(";")

        //for each of the categories, the attributes are placed on a new line and
        for (category in categories)
        {
            val attributes = category.split(",")
            var categoryMarker: String
            categoryMarker = when (attributes[0]) {
                "ACTIVITY" -> "⓪ "
                "SLEEP" -> "① "
                "SYMPTOMS" -> "② "
                "DIET" -> "③ "
                "MEDICATION" -> "④ "
                else -> ""
            }
            for (i in 1..attributes.lastIndex)
            {
                var attributeSpaced =
                    attributes[i].replace(":", ": ").capitalize()
                //if the attribute is symptoms, it is formatted nicely into a list
                val name = attributeSpaced.split(": ")[0]
                if (name == "Symptoms")
                //similar for foods/medication
                { attributeSpaced = attributeSpaced.replace("+", ", ") }
                if (name == "Foods" || name == "Medication")
                {
                    val itemList = mutableListOf<String>()
                    for (item in attributeSpaced.split("+"))
                    {
                        itemList.add(item.split("#")[0])
                    }
                    attributeSpaced = itemList.joinToString(", ")
                }
                newString += categoryMarker + attributeSpaced + "\n"
                bulletString += "￭ $attributeSpaced\n"
            }
        }
        var newSpannableString = SpannableString(bulletString)
        for (i in 0 until newString.lastIndex)
        {
            var bulletColour = ForegroundColorSpan(Color.BLACK)
            when (newString[i]) {
                '⓪' -> bulletColour = ForegroundColorSpan(
                    ContextCompat.getColor(this, R.color.activity_graph))
                '①' -> bulletColour = ForegroundColorSpan( //Sleep
                    ContextCompat.getColor(this, R.color.sleep_graph))
                '②' -> bulletColour = ForegroundColorSpan( //Symptoms
                    ContextCompat.getColor(this, R.color.symptoms_graph))
                '③' -> bulletColour = ForegroundColorSpan( //diet
                    ContextCompat.getColor(this, R.color.diet_secondary))
                '④' -> bulletColour = ForegroundColorSpan( //medication
                    ContextCompat.getColor(this, R.color.medication_secondary))
            }
            newSpannableString.setSpan(
                bulletColour, i,i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return newSpannableString
    }
}
