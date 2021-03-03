package com.example.aboutme

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.add_medication_dialog.view.*
import kotlinx.android.synthetic.main.medication.*


@RequiresApi(Build.VERSION_CODES.O)
class Medication: AppCompatActivity()  {
    private var medication: MutableMap<String, Boolean> = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.medication)
        prefs.writeSetting("PAGE", "medication")

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationMedication)
        bottomNavigationView.selectedItemId = R.id.medication
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> { startActivity(Intent(this, MainActivity::class.java)) }
                R.id.sleep -> { startActivity(Intent(this, Sleep::class.java)) }
                R.id.symptoms -> { startActivity(Intent(this, Symptoms::class.java)) }
                R.id.diet -> { startActivity(Intent(this, Diet::class.java)) }
            }
            overridePendingTransition(0, 0)
            true
        }

        loadCheckboxes()

        addMedicationButton.setOnClickListener()
        {
            addMedicationWindow()
        }
    }

    private fun addMedicationWindow()
    {
        //the dialog box is built
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.add_medication_dialog, null)
        //a builder for the window is initialised with a title
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add medication")
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
            .setPositiveButton("Add",
                //when the add button is clicked, the entered values are retrieved
                DialogInterface.OnClickListener { _, _ ->
                    val name = dialogView.medicationName.medicationNameInput.text.toString()
                    val dose = dialogView.medicationDose.medicationDoseInput.text.toString()
                    val frequency = dialogView.medicationFrequency.medicationFrequencyInput.text.toString()
                    if (name == "" || name == " " || !(name.matches("^[a-zA-Z0-9 ]*$".toRegex())))
                    {
                        Toast.makeText(this,"Name must be alphanumeric", Toast.LENGTH_SHORT).show()
                        return@OnClickListener
                    }
                    if (dose != "")
                    {
                        val doseInt = try {
                            dose.toInt()
                        } catch (e: Exception) {
                            -1
                        }
                        if (doseInt < 0)
                        {
                            Toast.makeText(this,"You can leave the dose blank, or it must be a whole number", Toast.LENGTH_SHORT).show()
                            return@OnClickListener
                        }
                    }
                    if (frequency != "")
                    {
                        if (frequency == " " || !(frequency.matches("^[a-zA-Z0-9 ]*$".toRegex())))
                        {
                            Toast.makeText(this,"You can leave the frequency blank, or it must be alphanumeric", Toast.LENGTH_SHORT).show()
                            return@OnClickListener
                        }
                    }
                    medication["$name#$dose#${frequency.toLowerCase()}"] = true
                    writeCheckboxes()
                    writeSelectedBoxes()
                    loadCheckboxes()
                })
        builder.show()
    }

    private fun writeCheckboxes()
    {
        prefs.writeSetting("MEDICATION", medication.keys.joinToString(","))
    }

    private fun writeSelectedBoxes()
    {
        val selectedBoxes = mutableListOf<String>()
        for (med in medication.keys)
        {
            if (medication[med] == true)
            { selectedBoxes.add(med) }
        }
        val date = prefs.getCurrentDate()
        if (selectedBoxes.isEmpty())
        {
            prefs.writeData(date, "medication", "medication", "")
        }
        else
        {
            prefs.writeData(date, "medication", "medication", selectedBoxes.joinToString("+"))
        }
    }

    private fun loadCheckboxes()
    {
        medication = mutableMapOf()
        medicationLinear.removeAllViews()
        val selectedBoxesString = prefs.readData(prefs.getCurrentDate(),"medication", "medication")
        val existingBoxes = prefs.readSetting("MEDICATION")
        if (existingBoxes == "ERROR: DATA NOT FOUND" || existingBoxes == "")
        {
            val emptyText: TextView = TextView(this)
            emptyText.text = "You have not yet added any medication."
            emptyText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            emptyText.textSize = 25F
            medicationLinear.addView(emptyText)
        }
        else
        {
            val selectedBoxes = if (selectedBoxesString != "ERROR: DATA NOT FOUND") {
                selectedBoxesString.split("+") }
            else { listOf<String>() }
            for (med in existingBoxes.split(","))
            {
                val boxInfo = med.split("#")
                val name = boxInfo[0]
                val dose = boxInfo[1]
                val frequency = boxInfo[2]
                var checkboxText = name
                if (dose != "") checkboxText += " (${dose}mcg)"
                if (frequency != "") checkboxText += ", take $frequency"
                val checkBox = CheckBox(ContextThemeWrapper(this, R.style.medicationCheckBox))
                checkBox.text = checkboxText
                checkBox.textSize = 25F
                if (med in selectedBoxes) {
                    checkBox.isChecked = true
                    medication[med] = true
                }
                else
                {
                    medication[med] = false
                }
                val relativeLayout = RelativeLayout(this)
                val params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                val deleteButton = ImageButton(ContextThemeWrapper(this, R.style.crossButton))
                deleteButton.setColorFilter(Color.parseColor("#FF8080"))
                deleteButton.setBackgroundDrawable(null)
                deleteButton.layoutParams = params
                deleteButton.setOnClickListener()
                {
                    medication.remove(med)
                    writeSelectedBoxes()
                    writeCheckboxes()
                    loadCheckboxes()
                }

                relativeLayout.addView(checkBox)
                relativeLayout.addView(deleteButton)

                medicationLinear.addView(relativeLayout)
            }
        }
    }

    fun onCheckboxClicked(view: View) {
        val box = (view as CheckBox).text.toString()
        var name = ""
        var nameEndIndex = box.length
        var dose = ""
        var frequency = ""
        if ("," in box)
        {
            nameEndIndex = box.indexOf(", take")
            frequency = box.substring(nameEndIndex+7).toString()
        }
        if (")" in box)
        {
            nameEndIndex = box.indexOf(" (")
            dose = box.subSequence(nameEndIndex+2, box.indexOf("mcg)")).toString()
        }
        name = box.subSequence(0, nameEndIndex).toString()
        medication["$name#$dose#$frequency"] = view.isChecked
        writeSelectedBoxes()
    }
}
