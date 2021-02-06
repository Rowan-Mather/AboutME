package com.example.aboutme

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.sleep.*
import kotlinx.android.synthetic.main.symptoms.*


class Symptoms : AppCompatActivity() {
    private var checkBoxes = linkedMapOf<String, Boolean>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.symptoms)

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationSymptoms)
        bottomNavigationView.selectedItemId = R.id.symptoms
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.sleep -> {
                    startActivity(Intent(this, Sleep::class.java))
                }
                R.id.calendar -> {
                    startActivity(Intent(this, Calendar::class.java))
                }
            }
            overridePendingTransition(0,0)
            true
        }

        var currentDate = prefs.getCurrentDate()

        //Controls the bar for overall health
        val healthBar: SeekBar = findViewById(R.id.healthBar)
        //the overallHealth variable is initialised and, if applicable, loaded from the app data
        var overallHealth = 5
        var healthString = prefs.readData(currentDate,"symptoms","health")
        if (healthString != "ERROR: DATA NOT FOUND") {
            overallHealth = healthString.toInt()
            overallHealthView.text = "How is your health overall? ($healthString)"
            healthBar.progress = overallHealth
        }
        //if the user interacts with the seek bar, the displays are updated and data written accordingly
        healthBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                overallHealthView.text = "How is your health overall? ($progress)"
                overallHealth = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefs.writeData(currentDate,"symptoms", "health", overallHealth.toString())
            }
        })

        //Controls the bar for overall fatigue
        val fatigueBar: SeekBar = findViewById(R.id.fatigueBar)
        //the fatigue variable is initialised and, if applicable, loaded from the app data
        var fatigue = 5
        var fatigueString = prefs.readData(currentDate,"symptoms","fatigue")
        if (fatigueString != "ERROR: DATA NOT FOUND") {
            fatigue = fatigueString.toInt()
            fatigueView.text = "How tired/fatigued are you? ($fatigue)"
            fatigueBar.progress = fatigue
        }
        //if the user interacts with the seek bar, the displays are updated and data written accordingly
        fatigueBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                fatigueView.text = "How tired/fatigued are you? ($progress)"
                fatigue = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefs.writeData(currentDate,"symptoms", "fatigue", fatigue.toString())
            }
        })


        val symptomsLinearLayout: LinearLayout = findViewById(R.id.symptomsScrollViewLinearLayout)
        val loadBoxesString = prefs.readData(currentDate, "symptoms", "symptoms")
        var loadedBoxes: List<String> = listOf()
        if ((loadBoxesString != "ERROR: DATA NOT FOUND") and (loadBoxesString != ""))
        {
            loadedBoxes = loadBoxesString.split("+")
        }
        var checkBoxNames = prefs.readCheckBoxes()
        if (checkBoxNames == null)
        {
            val noSymptomsView = TextView(this)
            noSymptomsView.gravity = Gravity.CENTER
            noSymptomsView.text = "You have not added any potential symptoms to your list yet."
            noSymptomsView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
            symptomsLinearLayout?.addView(noSymptomsView)
        }
        else
        {
            for (i in 0 until checkBoxNames.size)
            {
                val checkBox = CheckBox(ContextThemeWrapper(this, R.style.checkBox))
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
                checkBox.text = checkBoxNames[i]
                checkBoxes[checkBoxNames[i]] = false
                if (loadedBoxes != null){
                    if (checkBoxNames[i] in loadedBoxes)
                    {
                        checkBoxes[checkBoxNames[i]] = true
                        checkBox.isChecked = true
                    }
                }
                symptomsLinearLayout?.addView(checkBox)
            }
        }

        addSymptomButton.setOnClickListener {
            val newName = insertNewSymptom.text.toString()
            if ((newName.matches("^[a-zA-Z0-9]*$".toRegex())) && (newName != ""))
            {
                if (!checkBoxes.keys.contains(newName))
                {
                    if (checkBoxes.isEmpty())
                    {
                        symptomsLinearLayout.removeAllViews()
                    }
                    val checkBox = CheckBox(ContextThemeWrapper(this, R.style.checkBox))
                    checkBox.text = newName
                    checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24F)
                    checkBox.isChecked = true
                    symptomsLinearLayout?.addView(checkBox)
                    checkBoxes[newName] = true
                    dataUpdateSelectedBoxes()
                    prefs.writeCheckBoxes(checkBoxes.keys.toTypedArray())
                    insertNewSymptom.setText("")
                }
                else
                { Toast.makeText(applicationContext,"You have already added this symptom", Toast.LENGTH_SHORT).show() }
            }
            else
            { Toast.makeText(applicationContext,"Symptom name must be alphanumeric", Toast.LENGTH_SHORT).show() }
        }

        deleteSymptomButton.setOnClickListener {
            val symptomName = insertNewSymptom.text.toString()
            if ((symptomName.matches("^[a-zA-Z0-9]*$".toRegex())) && (symptomName != "") && (checkBoxes.keys.contains(symptomName)))
            {
                symptomsLinearLayout.removeViewAt(checkBoxes.keys.indexOf(symptomName))
                checkBoxes.remove(symptomName)
                dataUpdateSelectedBoxes()
                prefs.writeCheckBoxes(checkBoxes.keys.toTypedArray())
                insertNewSymptom.setText("")
            }
            else
            { Toast.makeText(applicationContext,"Cannot find that symptom", Toast.LENGTH_SHORT).show() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onCheckboxClicked(view: View) {
        var name = (view as CheckBox).text.toString()
        checkBoxes[name] = view.isChecked
        dataUpdateSelectedBoxes()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dataUpdateSelectedBoxes()
    {
        var boxListString = ""
        var names = checkBoxes.keys.toTypedArray()
        var toggles = checkBoxes.values.toBooleanArray()
        if (names.isNotEmpty()){
            for (i in 0 until checkBoxes.size)
            {
                if (toggles[i]) { boxListString+=names[i]+"+" }
            }
            if (boxListString.isNotEmpty()){
                if (boxListString[boxListString.lastIndex] == '+')
                { boxListString = boxListString.substring(0,boxListString.lastIndex) }
            }
        }
        prefs.writeData(prefs.getCurrentDate(), "symptoms", "symptoms", boxListString)
    }
}
