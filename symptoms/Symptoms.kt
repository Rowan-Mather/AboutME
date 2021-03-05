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

@RequiresApi(Build.VERSION_CODES.O)
class Symptoms : AppCompatActivity() {
    //this variable is a dictionary storing all the current symptom checkbox details
    // where key=[symptom name], value=[true/false (checked/unchecked)]
    private var checkBoxes = linkedMapOf<String, Boolean>()
    private var date = prefs.getCurrentDate()

    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.symptoms)
        prefs.writeSetting("PAGE", "symptoms")

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationSymptoms)
        bottomNavigationView.selectedItemId = R.id.symptoms
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> {
                    startActivity(Intent(this, MainActivity::class.java)) }
                R.id.sleep -> {
                    startActivity(Intent(this, Sleep::class.java)) }
                R.id.diet -> {
                    startActivity(Intent(this, Diet::class.java)) }
                R.id.medication -> {
                    startActivity(Intent(this, Medication::class.java)) }
            }
            overridePendingTransition(0,0)
            true
        }

        //Controls the seek bar for overall health
        seekBar("health", "How is your health overall?", healthBar, overallHealthView)
        val healthBar: SeekBar = findViewById(R.id.healthBar)

        //Controls the seek bar for overall fatigue
        seekBar("fatigue", "How tired/fatigued are you?", fatigueBar, fatigueView)

        //this is specifically if the app is being used for the first time
        //it sets 4 example/default symptoms that are commonly experienced by ME patients:
        //sore throat, headache, joint pain, post-exertional malaise
        if (prefs.readSetting("SYMPTOMDEFAULTS") == "ERROR: DATA NOT FOUND")
        {
            prefs.writeSetting("SYMPTOMDEFAULTS", "APPLIED")
            checkBoxes["Sore Throat"] = false
            checkBoxes["Headache"] = false
            checkBoxes["Joint Pain"] = false
            checkBoxes["Post-Exertional Malaise"] = false
            writeCheckBoxes()
        }

        displayCheckboxes()

        //this button adds a new symptom checkbox
        addSymptomButton.setOnClickListener {
            //it takes the name from the user entry text box
            //it checks the input is alphanumeric and not blank, and that that symptom hasn't already been added
            val newName = insertNewSymptom.text.toString()
            if (!(newName.matches("^[a-zA-Z0-9 ]*$".toRegex()) && newName != "" && newName != " "))
            {
                Toast.makeText(applicationContext,
                    "Symptom name must be alphanumeric", Toast.LENGTH_SHORT).show()
            }
            else if (checkBoxes.keys.contains(newName))
            {
                Toast.makeText(applicationContext,
                    "You have already added this symptom", Toast.LENGTH_SHORT).show()
            }
            else
            {
                //if there aren't currently any checkboxes, the message is removed
                if (checkBoxes.isEmpty())
                { symptomsLinear.removeAllViews() }
                checkBoxes[newName] = true
                writeCheckBoxes()
                writeSelectedBoxes()
                displayCheckboxes()
                insertNewSymptom.setText("")
            }
        }

        //this button deletes a symptom checkbox
        deleteSymptomButton.setOnClickListener {
            //gets the name of the symptom from the input text box & checks that it is a valid name
            val symptomName = insertNewSymptom.text.toString()
            if (!(symptomName in checkBoxes.keys))
            {
                Toast.makeText(applicationContext,
                    "Cannot find that symptom", Toast.LENGTH_SHORT).show()
            }
            else
            {
                //removes the check box view & the element from the checkboxes dictionary
                symptomsLinear.removeViewAt(checkBoxes.keys.indexOf(symptomName))
                checkBoxes.remove(symptomName)
                //updates shared preferences
                writeSelectedBoxes()
                writeCheckBoxes()
                insertNewSymptom.setText("")
                checkSymptomsMessage()
            }
        }

        //if the sort a-z button is clicked, the symptoms are sorted in alphabetical order
        sortButton.setOnClickListener()
        {
            quickSortCheckBoxes()
        }
    }

    //controls a given seekBar & updates data storage for it
    private fun seekBar(name: String, question: String, seekbar: SeekBar, textView: TextView)
    {
        //the bar variable is initialised and, if applicable, loaded from the app data
        var progress = 5
        val dataString = prefs.readData(date,"symptoms", name)
        if (dataString != "ERROR: DATA NOT FOUND") {
            progress = dataString.toInt()
            textView.text = "$question ($progress)"
            seekbar.progress = progress
        }
        //if the user interacts with the seek bar, the displays are updated and data written accordingly
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, barProgress: Int, fromUser: Boolean) {
                textView.text = "$question ($progress)"
                progress = barProgress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                textView.text = "$question ($progress)"
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                textView.text = "$question ($progress)"
                prefs.writeData(date,"symptoms", name, progress.toString())
            }
        })
    }

    //this function is called when any symptom checkbox is clicked
    //it updates the checkbox dictionary and shared preferences
    fun onCheckboxClicked(view: View) {
        val name = (view as CheckBox).text.toString()
        checkBoxes[name] = view.isChecked
        writeSelectedBoxes()
    }

    private fun writeCheckBoxes()
    {
        prefs.writeSetting("SYMPTOMSCHECKBOXES", checkBoxes.keys.joinToString(","))
    }

    private fun readCheckBoxes() : Array<String>?
    {
        val checkBoxData = prefs.readSetting("SYMPTOMSCHECKBOXES")
        if (checkBoxData == "" || checkBoxData == "ERROR: DATA NOT FOUND") { return null }
        return checkBoxData.split(",").toTypedArray()
    }

    //formats checkbox data to be saved into shared preferences
    //saves all currently selected boxes as a string separated by +s e.g. "cough+headache+sneeze"
    private fun writeSelectedBoxes()
    {
        val selectedBoxes = mutableListOf<String>()
        for (checkBoxName in checkBoxes.keys)
        {
            if (checkBoxes[checkBoxName] == true)
            { selectedBoxes.add(checkBoxName) }
        }
        val selectedString = if (selectedBoxes.isNotEmpty()) {
            selectedBoxes.joinToString("+")
        } else { "" }
        prefs.writeData(date,"symptoms", "symptoms", selectedString)
    }

    private fun loadCheckBoxList()
    {
        val existingBoxes = readCheckBoxes()
        if (existingBoxes == null)
        {
            checkBoxes = linkedMapOf<String, Boolean>()
            checkSymptomsMessage()
        }
        else
        {
            val selectedBoxesString = prefs.readData(date, "symptoms", "symptoms")
            var selectedBoxes = listOf<String>()
            if (selectedBoxesString != "ERROR: DATA NOT FOUND")
            { selectedBoxes = selectedBoxesString.split("+") }
            for (box in existingBoxes)
            {
                checkBoxes[box] = box in selectedBoxes
            }
        }
    }

    //creates a checkBox view for all listed in checkBoxes
    private fun displayCheckboxes()
    {
        loadCheckBoxList()
        symptomsLinear.removeAllViews()
        //if there are no checkboxes in the list, the message is displayed
        if (!checkSymptomsMessage())
        {
            //each saved checkbox is added to the view in turn
            for (checkBoxName in checkBoxes.keys)
            {
                val checkBox = CheckBox(ContextThemeWrapper(this, R.style.symptomCheckBox))
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24F)
                checkBox.text = checkBoxName
                //if they are supposed to be checked from earlier that day, they are checked
                checkBox.isChecked = checkBoxes[checkBoxName]!!
                symptomsLinear.addView(checkBox)
            }
        }
    }

    //if there are no symptom checkboxes, a message is displayed
    private fun checkSymptomsMessage() : Boolean
    {
        if (checkBoxes.isEmpty()) {
            symptomsLinear.removeAllViews()
            val noSymptomsView = TextView(this)
            noSymptomsView.gravity = Gravity.CENTER
            noSymptomsView.text = "You have not added any potential symptoms to your list yet."
            noSymptomsView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
            symptomsLinear.addView(noSymptomsView)
            return true
        }
        else
        {
            return false
        }
    }

    //sorts the list of symptoms in alphabetical order using a quick sort
    private fun quickSortCheckBoxes()
    {
        if (checkBoxes.size > 1) {
            val checkBoxNames: Array<String> = checkBoxes.keys.toTypedArray()
            try {
                quickSort(checkBoxNames, 0, checkBoxNames.lastIndex)
            } catch (e: Exception) {}

            displayCheckboxes()
            writeCheckBoxes()
        }
    }

    //quick sorts an array of strings
    private fun quickSort(array: Array<String>, left: Int, right: Int) {
        val part = partition(array, left, right)
        if (left < part - 1)
        {
            //sort LHS
            quickSort(array, left, part-1)
        }
        if (part < right)
        {
            //sort RHS
            quickSort(array, part, right)
        }
    }

    //reorder the string array so that all elements with values less than the pivot come before the pivot
    private fun partition(array: Array<String>, left: Int, right: Int): Int
    {
        var l = left
        var r = right
        val pivot = array[(l+r)/2].toLowerCase() //pivot in middle of range
        while (l <= r) {
            while (array[l].toLowerCase().compareTo(pivot) <= 0)
            {
                l += 1
            }
            while (array[r].toLowerCase().compareTo(pivot) >= 0)
            {
                r -= 1
            }
            if (l <= r)
            {
                val swap = array[l]
                array[l] = array[r]
                array[r] = swap
                l += 1
                r -= 1
            }
        }
        return l
    }
}
