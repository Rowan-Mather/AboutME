package com.example.aboutme

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.sleep.*

class Sleep : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sleep)

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationSleep)
        bottomNavigationView.selectedItemId = R.id.sleep
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> {
                    startActivity(Intent(this, MainActivity::class.java))
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

        var currentDate = prefs.getCurrentDate()

        //Controls the bar for the number of hours slept
        val sleepHoursBar: SeekBar = findViewById(R.id.sleepHoursBar)
        //the hoursSlept variable is initialised and, if applicable, loaded from the app data
        var hoursSlept = 8
        var hoursString = prefs.readData(currentDate,"sleep","hours")
        if (hoursString != "ERROR: DATA NOT FOUND") {
            hoursSlept = hoursString.toInt()
            hoursSleptView.text = "Hours slept last night: $hoursSlept"
            sleepHoursBar.progress = hoursSlept
        }
        //if the user interacts with the seek bar, the displays are updated and data written accordingly
        sleepHoursBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                hoursSleptView.text = "Hours slept last night: $progress"
                hoursSlept = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefs.writeData(currentDate,"sleep", "hours", hoursSlept.toString())
            }
        })

        //Sleep quality radio buttons
        val sleepQualityRadio :RadioGroup = findViewById(R.id.qualityGroup)
        var sleepQuality = ""
        //if applicable, the previously checked radio button is read from the app data and re-checked
        var qualityString = prefs.readData(currentDate,"sleep","quality")
        if (qualityString != "ERROR: DATA NOT FOUND") {
            sleepQuality = qualityString
            when (qualityString) {
                "Fitful" -> sleepQualityRadio.check(R.id.qualityFitful)
                "Poor" -> sleepQualityRadio.check(R.id.qualityPoor)
                "Fair" -> sleepQualityRadio.check(R.id.qualityFair)
                "Restful" -> sleepQualityRadio.check(R.id.qualityRestful)
                "Energising" -> sleepQualityRadio.check(R.id.qualityEnergising)
            }
        }
        //when a radio button is clicked, the app data is updated
        sleepQualityRadio.setOnCheckedChangeListener {group, checkedId ->
            when (checkedId) {
                R.id.qualityFitful -> sleepQuality = "Fitful"
                R.id.qualityPoor -> sleepQuality = "Poor"
                R.id.qualityFair -> sleepQuality = "Fair"
                R.id.qualityRestful-> sleepQuality = "Restful"
                R.id.qualityEnergising -> sleepQuality = "Energising"
            }
            prefs.writeData(currentDate,"sleep", "quality", sleepQuality)
        }
    }
}