package com.example.aboutme

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    //initialises the variable spoons and the maximum number of spoons
    private var spoons = 12
    private var maxSpoons = 12
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
                R.id.sleep -> { startActivity(Intent(this, Sleep::class.java)) }
                R.id.symptoms -> { startActivity(Intent(this, Symptoms::class.java)) }
                R.id.calendar -> { startActivity(Intent(this, Calendar::class.java)) }
                R.id.graphs -> { startActivity(Intent(this, Graphs::class.java)) }
            }
            overridePendingTransition(0,0)
            true
        }

        //if the number of spoons for that day & the maximum number of spoons are stored, they are loaded
        val maxSpoonString = prefs.readSetting("MAXSPOONS")
        if (maxSpoonString != "ERROR: DATA NOT FOUND") { maxSpoons = maxSpoonString.toInt() }
        val spoonsString = readSpoons()
        spoons = if (spoonsString != "ERROR: DATA NOT FOUND") { spoonsString.toInt() }
                else { maxSpoons }
        updateTextViews()

        //when the user taps the spoon button, the spoon count is decremented and the stored data is updated
        spoonButton.setOnClickListener {
            if (spoons > 0){
                spoons -= 1
                writeSpoons()
                updateTextViews()
            }
            else
            { Toast.makeText(applicationContext,"You have run out of spoons", Toast.LENGTH_SHORT).show() }
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

    //reads the value of spoons from shared preferences
    @RequiresApi(Build.VERSION_CODES.O)
    fun readSpoons() : String
    {
        return prefs.readData(prefs.getCurrentDate(),"activity", "spoons")
    }

    //writes the value of spoons to shared preferences
    @RequiresApi(Build.VERSION_CODES.O)
    fun writeSpoons()
    {
        prefs.writeData(prefs.getCurrentDate(),"activity", "spoons", spoons.toString())
    }

    //when one of the +/- buttons for the maximum spoons is clicked, this is called
    //it checks that the change would be in the maximum bounds
    //if so it updates the variable, and the number of spoons, and writes the info to shared prefs
    @RequiresApi(Build.VERSION_CODES.O)
    fun changeMax(change: Int)
    {
        if ((change < 0 && maxSpoons == 2) || (change > 0 && maxSpoons == 15 ))
        {
            Toast.makeText(applicationContext,"Your maximum number of spoons can only be from 2-15", Toast.LENGTH_SHORT).show()
        }
        else
        {
            maxSpoons += change
            prefs.writeSetting("MAXSPOONS", maxSpoons.toString())
            if (!(spoons == 0 && change < 0))
            {
                spoons += change
                if (readSpoons() != "ERROR: DATA NOT FOUND") { writeSpoons() }
            }
            updateTextViews()
        }
    }

    //sets the displayed spoon count, spoon maximum and recommendation message to their live values
    fun updateTextViews()
    {
        var message = ""
        when (spoons) {
            0 -> message = "You have no spoons remaining for today. You should rest."
            1 -> message = "You're spoons are low, consider resting."
            2 -> message = "You're spoons are low, consider resting."
            maxSpoons -> message = "You're spoons are full!\nAfter you have been active, tap the spoon one or more times."
        }
        spoonRecommendations.text = message
        spoonsDefaultView.text = "Maximum spoons: $maxSpoons"
        spoonCount.text = spoons.toString()
    }
}
