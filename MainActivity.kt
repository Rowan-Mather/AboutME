package com.example.aboutme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
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
            }
            overridePendingTransition(0,0)
            true
        }

        //initialises the variable spoons and default sets it to 10
        //  if the number of spoons for that day is stored, it is loaded
        var spoons = 10
        var currentDate = prefs.getCurrentDate()
        var spoonsString = prefs.readData(currentDate,"activity","spoons")
        if (spoonsString != "ERROR: DATA NOT FOUND") { spoons = spoonsString.toInt() }
        //the text view that displays the number of spoons is updated
        val spoonCountView = findViewById(R.id.spoonCount) as TextView
        spoonCountView.text = spoons.toString()

        //when the user taps the spoon button, the spoon count is decremented and the stored data is updated
        spoonButton.setOnClickListener {
            spoons -= 1
            spoonCountView.text = spoons.toString()
            prefs.writeData(currentDate,"activity", "spoons", spoons.toString())
        }
    }
}
