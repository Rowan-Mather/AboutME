package com.example.aboutme

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.calendardate.*

class CalendarDate: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendardate)

        //the selected date from the calendar is received by this activity and displayed at the top
        val intent = intent
        var thisDate = intent.getStringExtra("selectedDate")
        val dateView = findViewById(R.id.dateView) as TextView
        dateView.text = thisDate

        //button to return the user to the calendar activity
        returnToCalendar.setOnClickListener {
            startActivity(Intent(this, Calendar::class.java))
        }
    }
}
