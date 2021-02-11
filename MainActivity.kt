package com.example.aboutme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the dates on which information is stored
        prefs.readDates()

        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationActivity)
        bottomNavigationView.selectedItemId = R.id.mainActivity
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.sleep -> {
                    startActivity(Intent(this, Sleep::class.java))
                }
                R.id.symptoms -> {
                    startActivity(Intent(this, Symptoms::class.java))
                }
                R.id.calendar -> {
                    startActivity(Intent(this, Calendar::class.java))
                }
                R.id.graphs -> {
                    startActivity(Intent(this, Graphs::class.java))
                }
            }
            overridePendingTransition(0,0)
            true
        }

        var currentDate = prefs.getCurrentDate()

        //initialises the variable spoons and the maximum number of spoons
        //if the number of spoons for that day is stored, it is loaded
        var spoons: Int
        var maxSpoons = 10
        var maxSpoonString = prefs.readSetting("MAXSPOONS")
        if (maxSpoonString != "ERROR: DATA NOT FOUND") { maxSpoons = maxSpoonString.toInt() }
        var spoonsString = prefs.readData(currentDate,"activity","spoons")
        if (spoonsString != "ERROR: DATA NOT FOUND") { spoons = spoonsString.toInt() }
        else { spoons = maxSpoons }
        updateTextViews(spoons,maxSpoons)

        //when the user taps the spoon button, the spoon count is decremented and the stored data is updated
        spoonButton.setOnClickListener {
            if (spoons > 0){
                spoons -= 1
                prefs.writeData(currentDate,"activity", "spoons", spoons.toString())
                updateTextViews(spoons,maxSpoons)
            }
            else
            { Toast.makeText(applicationContext,"You have run out of spoons", Toast.LENGTH_SHORT).show() }
        }

        //controls the +/- buttons for changing the maximum number of spoons the user has
        maxSpoonsMinus.setOnClickListener{
            if (maxSpoons > 1)
            {
                maxSpoons -= 1
                prefs.writeSetting("MAXSPOONS", maxSpoons.toString())
                if (spoons > 0)
                {
                    spoons -= 1
                    if (prefs.readData(currentDate,"activity", "spoons") != "ERROR: DATA NOT FOUND")
                    { prefs.writeData(currentDate,"activity", "spoons", spoons.toString()) }
                }
                updateTextViews(spoons,maxSpoons)
            }
            else
            { Toast.makeText(applicationContext,"Your maximum number of spoons can only be from 1-99", Toast.LENGTH_SHORT).show()}
        }
        maxSpoonsPlus.setOnClickListener{
            if (maxSpoons < 99)
            {
                maxSpoons += 1
                prefs.writeSetting("MAXSPOONS", maxSpoons.toString())
                spoons += 1
                if (prefs.readData(currentDate,"activity", "spoons") != "ERROR: DATA NOT FOUND")
                { prefs.writeData(currentDate,"activity", "spoons", spoons.toString()) }
                updateTextViews(spoons,maxSpoons)
            }
            else
            { Toast.makeText(applicationContext,"Your maximum number of spoons can only be from 1-99", Toast.LENGTH_SHORT).show()}
        }

        //help button triggers help screen
        helpButton.setOnClickListener {
            startActivity(Intent(this, ActivityHelp::class.java))
        }
    }

    //sets the displayed spoon count, spoon maximum and recommendation message to their live values
    fun updateTextViews(spoons: Int, max: Int)
    {
        var message = ""
        when (spoons) {
            0 -> message = "You have no spoons remaining for today. You should rest."
            1 -> message = "You're spoons are low, consider resting."
            2 -> message = "You're spoons are low, consider resting."
            max -> message = "You're spoons are full!\nAfter you have been active, tap the spoon one or more times."
        }
        spoonRecommendations.text = message
        spoonsDefaultView.text = "Maximum spoons: $max"
        spoonCount.text = spoons.toString()
    }
}
