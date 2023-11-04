package com.example.aboutme

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
        prefs.writeSetting("PAGE", "sleep")

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottomNavigationSleep)
        bottomNavigationView.selectedItemId = R.id.sleep
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> {
                    startActivity(
                        Intent(this, MainActivity::class.java)) }
                R.id.symptoms -> {
                    startActivity(
                        Intent(this, Symptoms::class.java)) }
                R.id.diet -> {
                    startActivity(
                        Intent(this, Diet::class.java)) }
                R.id.medication -> {
                    startActivity(
                        Intent(this, Medication::class.java)) }
            }
            overridePendingTransition(0,0)
            true
        }

        val currentDate = prefs.getCurrentDate()

        //Controls the bar for the number of hours slept
        val sleepHoursBar: SeekBar = findViewById(R.id.sleepHoursBar)
        //the hoursSlept variable is initialised and,
        // if applicable, loaded from the app data
        var hoursSlept = 8
        val hoursString = prefs.readData(currentDate,"sleep","hours")
        if (hoursString != "ERROR: DATA NOT FOUND") {
            hoursSlept = hoursString.toInt()
            hoursSleptView.text = "Hours slept last night: $hoursSlept"
            sleepHoursBar.progress = hoursSlept
        }
        //if the user interacts with the seek bar,
        // the displays are updated and data written accordingly
        sleepHoursBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged
                        (seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                hoursSleptView.text = "Hours slept last night: $progress"
                hoursSlept = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar)
            { hoursSleptView.text = "Hours slept last night: $hoursSlept" }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                hoursSleptView.text = "Hours slept last night: $hoursSlept"
                prefs.writeData(
                    currentDate,
                    "sleep",
                    "hours",
                    hoursSlept.toString())
            }
        })

        //Sleep quality radio buttons
        var sleepQuality = ""
        //if applicable, the previously checked radio button
        // is read from the app data and re-checked
        val qualityString = prefs.readData(currentDate,"sleep","quality")
        if (qualityString != "ERROR: DATA NOT FOUND") {
            sleepQuality = qualityString
            when (qualityString) {
                "Fitful" -> qualityGroup.check(R.id.qualityFitful)
                "Poor" -> qualityGroup.check(R.id.qualityPoor)
                "Fair" -> qualityGroup.check(R.id.qualityFair)
                "Restful" -> qualityGroup.check(R.id.qualityRestful)
                "Energising" -> qualityGroup.check(R.id.qualityEnergising)
            }
        }
        //when a radio button is clicked, the app data is updated
        qualityGroup.setOnCheckedChangeListener { _, checkedId ->
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