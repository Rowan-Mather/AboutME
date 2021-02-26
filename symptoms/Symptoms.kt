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

    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.symptoms)

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationSymptoms)
        bottomNavigationView.selectedItemId = R.id.symptoms
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> { startActivity(Intent(this, MainActivity::class.java)) }
                R.id.sleep -> { startActivity(Intent(this, Sleep::class.java)) }
                R.id.calendar -> { startActivity(Intent(this, Calendar::class.java)) }
                R.id.graphs -> { startActivity(Intent(this, Graphs::class.java)) }
            }
            overridePendingTransition(0,0)
            true
        }

        val currentDate = prefs.getCurrentDate()

        //Controls the bar for overall health
        val healthBar: SeekBar = findViewById(R.id.healthBar)
        //the overallHealth variable is initialised and, if applicable, loaded from the app data
        var overallHealth = 5
        val healthString = prefs.readData(currentDate,"symptoms","health")
        if (healthString != "ERROR: DATA NOT FOUND") {
            overallHealth = healthString.toInt()
            overallHealthView.text = "How is your health overall? ($healthString)"
            healthBar.progress = overallHealth
        }
        //if the user interacts with the seek bar, the displays are updated and data written accordingly
        healthBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        val fatigueString = prefs.readData(currentDate,"symptoms","fatigue")
        if (fatigueString != "ERROR: DATA NOT FOUND") {
            fatigue = fatigueString.toInt()
            fatigueView.text = "How tired/fatigued are you? ($fatigue)"
            fatigueBar.progress = fatigue
        }
        //if the user interacts with the seek bar, the displays are updated and data written accordingly
        fatigueBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                fatigueView.text = "How tired/fatigued are you? ($progress)"
                fatigue = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefs.writeData(currentDate,"symptoms", "fatigue", fatigue.toString())
            }
        })

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
            prefs.writeCheckBoxes(checkBoxes.keys.toTypedArray())
        }

        //this block loads the checkbox list of symptoms
        //finds the scroll view that contains the symptoms checkboxes
        val symptomsLinearLayout: LinearLayout = findViewById(R.id.symptomsScrollViewLinearLayout)
        //loads the checkboxes that have already been selected on the current date if applicable
        val loadBoxesString = prefs.readData(currentDate, "symptoms", "symptoms")
        var loadedBoxes: Array<String> = arrayOf()
        if ((loadBoxesString != "ERROR: DATA NOT FOUND") and (loadBoxesString != ""))
        { loadedBoxes = loadBoxesString.split("+").toTypedArray() }
        /*loads the saved checkboxes, note the difference between the list of symptoms
          that the user could select which do not get reset with each new day (checkBoxNames)
          and the the list of symptoms that the user has selected that day (loadedBoxes)
         */
        val checkBoxNames = prefs.readCheckBoxes()
        displayCheckboxes(checkBoxNames, loadedBoxes)

        //this button adds a new symptom checkbox
        addSymptomButton.setOnClickListener {
            //it takes the name from the user entry text box
            //it checks the input is alphanumeric and not blank, and that that symptom hasn't already been added
            val newName = insertNewSymptom.text.toString()
            if (newName.matches("^[a-zA-Z0-9 ]*$".toRegex()) && newName != "" && newName != " ")
            {
                if (!checkBoxes.keys.contains(newName))
                {
                    //if there aren't currently any checkboxes, the message is removed
                    if (checkBoxes.isEmpty())
                    { symptomsLinearLayout.removeAllViews() }
                    //a new view is created & formatted, assigned the new name
                    val checkBox = CheckBox(ContextThemeWrapper(this, R.style.checkBox))
                    checkBox.text = newName
                    checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24F)
                    checkBox.isChecked = true
                    symptomsLinearLayout.addView(checkBox)
                    checkBoxes[newName] = true
                    //shared preferences are updated
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

        //this button deletes a symptom checkbox
        deleteSymptomButton.setOnClickListener {
            //gets the name of the symptom from the input text box & checks that it is a valid name
            val symptomName = insertNewSymptom.text.toString()
            if (symptomName.matches("^[a-zA-Z0-9 ]*$".toRegex()) && symptomName != "" && symptomName != " ")
            {
                if (symptomName in checkBoxes.keys){
                    //removes the check box view & the element from the checkboxes dictionary
                    symptomsLinearLayout.removeViewAt(checkBoxes.keys.indexOf(symptomName))
                    checkBoxes.remove(symptomName)
                    //updates shared preferences
                    dataUpdateSelectedBoxes()
                    prefs.writeCheckBoxes(checkBoxes.keys.toTypedArray())
                    insertNewSymptom.setText("")
                    //if there are now no checkboxes, the message is displayed
                    if (checkBoxes.isEmpty())
                    { noSymptomsMessage() }
                }
            }
            else
            { Toast.makeText(applicationContext,"Cannot find that symptom", Toast.LENGTH_SHORT).show() }
        }

        //if the sort a-z button is clicked, the symptoms are sorted in alphabetical order
        sortButton.setOnClickListener()
        {
            quickSortCheckBoxes()
        }
    }

    //creates checkBox objects for every name in existingBoxes and checks them if they are
    //listed in selectedBoxes
    //adds each box to the linear layout
    fun displayCheckboxes(existingBoxes: Array<String>?, selectedBoxes: Array<String>)
    {
        val symptomsLinearLayout: LinearLayout = findViewById(R.id.symptomsScrollViewLinearLayout)
        symptomsLinearLayout.removeAllViews()
        //if there are no checkboxes in the list, the message is displayed
        if (existingBoxes == null)
        { noSymptomsMessage() }
        else
        {
            checkBoxes.clear()
            //each saved checkbox is added to the view in turn
            for (checkBoxName in existingBoxes)
            {
                val checkBox = CheckBox(ContextThemeWrapper(this, R.style.checkBox))
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24F)
                checkBox.text = checkBoxName
                //if they are supposed to be checked from earlier that day, they are checked
                //by default they are added unchecked
                if (checkBoxName in selectedBoxes)
                {
                    checkBoxes[checkBoxName] = true
                    checkBox.isChecked = true
                }
                else
                {
                    checkBoxes[checkBoxName] = false
                    checkBox.isChecked = false
                }
                symptomsLinearLayout.addView(checkBox)
            }
        }
    }

    //this function is called when any symptom checkbox is clicked
    //it updates the checkbox dictionary and shared preferences
    fun onCheckboxClicked(view: View) {
        val name = (view as CheckBox).text.toString()
        checkBoxes[name] = view.isChecked
        dataUpdateSelectedBoxes()
    }

    //returns an array of the names of the checkboxes currently selected
    private fun getSelectedBoxes(): Array<String>
    {
        val selectedBoxes = mutableListOf<String>()
        for (checkBoxName in checkBoxes.keys)
        {
            if (checkBoxes[checkBoxName] == true)
            {
                selectedBoxes.add(checkBoxName)
            }
        }
        return selectedBoxes.toTypedArray()
    }

    //formats checkbox data to be saved into shared preferences
    //saves all currently selected boxes as a string separated by +s e.g. "cough+headache+sneeze"
    private fun dataUpdateSelectedBoxes()
    {
        var boxListString = ""
        val selectedBoxes = getSelectedBoxes()
        if (selectedBoxes.isNotEmpty()) {
            boxListString = selectedBoxes.joinToString("+")
        }
        prefs.writeData(prefs.getCurrentDate(), "symptoms", "symptoms", boxListString)
    }

    //if there are no symptom checkboxes, a message is displayed
    private fun noSymptomsMessage()
    {
        val symptomsLinearLayout: LinearLayout = findViewById(R.id.symptomsScrollViewLinearLayout)
        val noSymptomsView = TextView(this)
        noSymptomsView.gravity = Gravity.CENTER
        noSymptomsView.text = "You have not added any potential symptoms to your list yet."
        noSymptomsView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
        symptomsLinearLayout.addView(noSymptomsView)
    }

    //sorts the list of symptoms in alphabetical order using a quick sort
    private fun quickSortCheckBoxes()
    {
        if (checkBoxes.size > 1) {
            var checkBoxNames: Array<String> = checkBoxes.keys.toTypedArray()
            quickSort(checkBoxNames, 0, checkBoxNames.lastIndex)

            displayCheckboxes(checkBoxNames, getSelectedBoxes())
            prefs.writeCheckBoxes(checkBoxes.keys.toTypedArray())
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
