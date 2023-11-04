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
        val bottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottomNavigationSymptoms)
        bottomNavigationView.selectedItemId = R.id.symptoms
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> {
                    startActivity(
                        Intent(this, MainActivity::class.java)) }
                R.id.sleep -> {
                    startActivity(
                        Intent(this, Sleep::class.java)) }
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

        //Controls the seek bar for overall health
        seekBar("health", "How is your health overall?",
            healthBar, overallHealthView)
        val healthBar: SeekBar = findViewById(R.id.healthBar)

        //Controls the seek bar for overall fatigue
        seekBar("fatigue", "How tired/fatigued are you?",
            fatigueBar, fatigueView)

        //this is specifically if the app is being used for the first time
        //it sets 4 example/default symptoms that are commonly experienced by ME patients:
        //sore throat, headache, joint pain, post-exertional malaise
        if (prefs.readSetting("SYMPTOMDEFAULTS") == "ERROR: DATA NOT FOUND")
        {
            prefs.writeSetting("SYMPTOMDEFAULTS", "APPLIED")
            checkBoxes["Sore Throat"] = false
            checkBoxes["Headache"] = false
            checkBoxes["Joint Pain"] = false
            checkBoxes["Post Exertional Malaise"] = false
            writeCheckBoxes()
        }

        displayCheckBoxes()

        //this button adds a new symptom checkbox
        addSymptomButton.setOnClickListener {
            //it takes the name from the user entry text box
            //it checks the input is alphanumeric and not blank,
            // and that that symptom hasn't already been added
            val newName = insertNewSymptom.text.toString()
            if (!(newName.matches("^[a-zA-Z0-9 ]*$".toRegex())
                        && newName != "" && newName != " "))
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
                displayCheckBoxes()
                insertNewSymptom.setText("")
            }
        }

        //this button deletes a symptom checkbox
        deleteSymptomButton.setOnClickListener {
            //gets the name of the symptom from the
            // input text box & checks that it is a valid name
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
    private fun seekBar(
        name: String, question: String, seekbar: SeekBar, textView: TextView)
    {
        //the bar variable is initialised and, if applicable, loaded from the app data
        var progress = 5
        val dataString = prefs.readData(date,"symptoms", name)
        if (dataString != "ERROR: DATA NOT FOUND") {
            progress = dataString.toInt()
            textView.text = "$question ($progress)"
            seekbar.progress = progress
        }
        //if the user interacts with the seek bar,
        // the displays are updated and data written accordingly
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged
                        (seekBar: SeekBar, barProgress: Int, fromUser: Boolean) {
                progress = barProgress
                textView.text = "$question ($progress)"
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

    //writes the names of the checkboxes to shared prefs
    private fun writeCheckBoxes()
    {
        prefs.writeSetting(
            "SYMPTOMSCHECKBOXES",
            checkBoxes.keys.joinToString(","))
    }

    //reads the names of the checkboxes from shared prefs
    private fun readCheckBoxes() : Array<String>?
    {
        val checkBoxData = prefs.readSetting("SYMPTOMSCHECKBOXES")
        if (checkBoxData == "" || checkBoxData == "ERROR: DATA NOT FOUND") { return null }
        return checkBoxData.split(",").toTypedArray()
    }

    //formats checkbox data to be saved into shared preferences
    //saves all currently selected boxes as a string separated by +s
    // e.g. "cough+headache+sneeze"
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

    //populates the checkBoxes dictionary with the stored data
    private fun loadCheckBoxList()
    {
        val existingBoxes = readCheckBoxes()
        if (existingBoxes == null)
        {
            //if there are no checkboxes, a message is displayed
            checkBoxes = linkedMapOf<String, Boolean>()
            checkSymptomsMessage()
        }
        else
        {
            //each checkbox is added in turn
            val selectedBoxesString = prefs.readData(
                date,
                "symptoms",
                "symptoms")
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
    private fun displayCheckBoxes()
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
            noSymptomsView.text =
                "You have not added any potential symptoms to your list yet."
            noSymptomsView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
            symptomsLinear.addView(noSymptomsView)
            return true
        }
        else
        { return false }
    }

    //sorts the list of symptoms in alphabetical order using a quick sort
    private fun quickSortCheckBoxes()
    {
        if (checkBoxes.size > 1) {
            val checkBoxNames: Array<String> = checkBoxes.keys.toTypedArray()
            quickSort(checkBoxNames, 0, checkBoxNames.lastIndex)
            val newCheckBoxes = linkedMapOf<String, Boolean>()
            for (checkBox in checkBoxNames)
            {
                newCheckBoxes[checkBox] = checkBoxes[checkBox]!!
            }
            checkBoxes = newCheckBoxes
            writeCheckBoxes()
            displayCheckBoxes()
        }
    }

    //quick sorts an array of strings
    private fun quickSort(arr: Array<String>, low: Int, high: Int) {
        //if the array is too small to need sorting, it's returned
        if (arr.size <= 1) {
            return
        }
        //if the array range is 2, the elements are sorted with just one comparison
        if (high-low == 1)
        {
            if (compare(arr[low], arr[high]) > 0) { swap(arr, low, high) }
            return
        }
        //quick sort
        if (low < high) {
            //places pivot
            val pivotPos = placePivot(arr, low, high)
            //sorts left
            quickSort(arr, low, pivotPos)
            //sorts right
            quickSort(arr, pivotPos, high)
        }
    }

    //reorder the string array so that all elements with values less than
    // the pivot come before the pivot
    //returns index of pivot
    private fun placePivot(arr: Array<String>, low: Int, high: Int): Int {
        //picks a pivot
        val pivot = medianOfThree(arr, low, high)
        //puts the pivot at the end of the range
        swap(arr, (low+high)/2, high)
        while (true) {
            //finds the first element from the left that is larger than the pivot
            var i = low
            while (compare(arr[i], pivot) <= 0) { i++ }
            //finds the first element from the right that is smaller than the pivot
            var j = high
            while (compare(arr[j], pivot) >= 0) { j -= 1 }
            //if the index of the larger element is greater than the other,
            //the reordering is complete
            if (i > j)
            {
                //puts the pivot back in the correct place & returns its location
                swap(arr, i, high)
                return i
            }
            //otherwise, the two elements are swapped
            else { swap(arr, i, j) }
        }
    }

    //allocates a pivot based on the first, middle and last elements
    // of the range, the median is the pivot
    private fun medianOfThree(arr: Array<String>, low: Int, high: Int): String {
        val mid: Int = (low+high)/2
        //the ifs sort the three elements
        if (compare(arr[low], arr[mid]) > 0) { swap(arr, low, mid) }
        if (compare(arr[mid], arr[high]) > 0) { swap(arr, mid, high) }
        if (compare(arr[low], arr[mid]) > 0) { swap(arr, low, mid) }
        return arr[mid]
    }

    //swaps two array elements
    private fun swap(arr: Array<String>, p1: Int, p2: Int)
    {
        val swap = arr[p1]
        arr[p1] = arr[p2]
        arr[p2] = swap
    }

    //compares two strings alphabetically
    private fun compare(s1: String, s2: String): Int {
        return s1.toLowerCase().compareTo(s2.toLowerCase())
    }
}