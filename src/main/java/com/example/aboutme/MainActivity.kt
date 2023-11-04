package com.example.aboutme

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    //initialises the variable spoons and the maximum number of spoons
    private var spoons = 12
    private var maxSpoons = 12
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the dates on which information is stored
        prefs.readDates()

        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs.writeSetting("PAGE", "activity")

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottomNavigationActivity)
        bottomNavigationView.selectedItemId = R.id.mainActivity
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.sleep -> {
                    startActivity(Intent(this, Sleep::class.java)) }
                R.id.symptoms -> {
                    startActivity(Intent(this, Symptoms::class.java)) }
                R.id.diet -> {
                    startActivity(Intent(this, Diet::class.java)) }
                R.id.medication -> {
                    startActivity(Intent(this, Medication::class.java)) }
            }
            overridePendingTransition(0,0)
            true
        }

        //if the number of spoons for that day &
        // the maximum number of spoons are stored, they are loaded
        val maxSpoonString = prefs.readSetting("MAXSPOONS")
        if (maxSpoonString != "ERROR: DATA NOT FOUND") { maxSpoons = maxSpoonString.toInt() }
        val spoonsString = readSpoons()
        if (spoonsString != "ERROR: DATA NOT FOUND")
        {
            spoons = spoonsString.toInt()
        }
        else
        {
            spoons = maxSpoons
            //if the user exceeded their spoon allowance yesterday,
            // the negative spoons are subtracted from this value
            val yesterdaySpoons = prefs.readData(yesterday(),"activity", "spoons")
            if (yesterdaySpoons != "ERROR: DATA NOT FOUND")
            {
                if (yesterdaySpoons.toInt() < 0)
                { spoons += yesterdaySpoons.toInt() }
            }
        }
        updateTextViews()

        //when the user taps the spoon button,
        // the spoon count is decremented and the stored data is updated
        spoonButton.setOnClickListener {
            if (spoons > -10){
                spoons -= 1
                writeSpoons()
                updateTextViews()
            }
            else
            { Toast.makeText(applicationContext,
                "The spoon count cannot go any lower than this",
                Toast.LENGTH_SHORT).show()
            }
        }

        //controls the +/- buttons for changing the maximum number of spoons the user has
        maxSpoonsMinus.setOnClickListener{
            changeMax(-1)
        }
        maxSpoonsPlus.setOnClickListener{
            changeMax(1)
        }

        //help button triggers help screen
        helpButton.setOnClickListener {
            startActivity(Intent(this, ActivityHelp::class.java))
        }
    }

    //returns the date of the previous day in the form yyyy-mm-dd
    private fun yesterday(): String
    {
        val currentDateTime: Long = prefs.convertDate(prefs.getCurrentDate()).time
        val yesterdayTime = currentDateTime - 86400000
        val yesterday: Date = Date(yesterdayTime)
        var day = yesterday.date.toString()
        if (yesterday.date < 10) { day = "0" + day}
        var month = (yesterday.month + 1).toString()
        if (yesterday.month + 1 < 10) { month = "0" + month}
        val year = (yesterday.year +1900).toString()
        return "$year-$month-$day"
    }

    //reads the value of spoons from shared preferences
    private fun readSpoons() : String
    {
        return prefs.readData(prefs.getCurrentDate(),"activity", "spoons")
    }

    //writes the value of spoons to shared preferences
    private fun writeSpoons()
    {
        prefs.writeData(prefs.getCurrentDate(), "activity",
            "spoons", spoons.toString())
    }

    //when one of the +/- buttons for the maximum spoons is clicked, this is called
    //it checks that the change would be in the maximum bounds if so it updates the variable,
    // and the number of spoons, and writes the info to shared prefs
    private fun changeMax(change: Int)
    {
        if ((change < 0 && maxSpoons == 2) || (change > 0 && maxSpoons == 15 ))
        {
            Toast.makeText(applicationContext,
                "Your maximum number of spoons can only be from 2-15",
                Toast.LENGTH_SHORT).show()
        }
        else
        {
            maxSpoons += change
            prefs.writeSetting("MAXSPOONS", maxSpoons.toString())
            if (!(spoons == -10 && change < 0))
            {
                spoons += change
                if (readSpoons() != "ERROR: DATA NOT FOUND") { writeSpoons() }
            }
            updateTextViews()
        }
    }

    //sets the displays for spoon count, spoon maximum and the recommendation
    // message to their values
    private fun updateTextViews()
    {
        var message = ""
        when (spoons) {
            0 -> message = "You have no spoons remaining for today. You should rest."
            1 -> message = "Your spoons are low, consider resting."
            2 -> message = "Your spoons are low, consider resting."
            maxSpoons -> message = "Your spoons are full!\n" +
                    "After you have been active, tap the spoon one or more times."
        }
        if (spoons < 0)
        {
            message = "You have exceeded your spoons for today.\n" +
                    "The negative spoons will be taken off your starting amount tomorrow."
        }
        spoonRecommendations.text = message
        spoonsDefaultView.text = "Maximum spoons: $maxSpoons"
        spoonCount.text = spoons.toString()
    }
}