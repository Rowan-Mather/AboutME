package com.example.aboutme

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.calendar_date.*

class CalendarDate: AppCompatActivity() {
    private var thisDate = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_date)

        //the selected date from the calendar is received by
        // this activity and displayed at the top
        val intent = intent
        thisDate = intent.getStringExtra("selectedDate").toString()
        dateView.text = formatDate()

        //creates a dictionary of the categories and arrays of the attributes in them
        //each button is stored as an instance of the data class EditButtonAttributes
        //which creates the range of possible values that the user can select
        val buttonAttributes = mapOf(
            "activity" to arrayOf(
                EditButtonAttributes("spoons",-10, 15)
            ),
            "sleep" to arrayOf(
                EditButtonAttributes("hours", 0, 10),
                EditButtonAttributes("quality",
                    arrayOf("Fitful","Poor", "Fair", "Restful", "Energising"))
            ),
            "symptoms" to arrayOf(
                EditButtonAttributes("health", 0, 10),
                EditButtonAttributes("fatigue", 0, 10),
                EditButtonAttributes("symptoms")
            ),
            "diet" to arrayOf(
                EditButtonAttributes("total calories", 0, 0),
                EditButtonAttributes("foods")
            ),
            "medication" to arrayOf(
                EditButtonAttributes("medication")
            )
        )

        //dictionary of the colour coding of each category
        val categoryColours = mapOf(
            "activity" to
                    ContextCompat.getColor(this, R.color.activity_graph),
            "sleep" to
                    ContextCompat.getColor(this, R.color.sleep_graph),
            "symptoms" to
                    ContextCompat.getColor(this, R.color.symptoms_graph),
            "diet" to
                    ContextCompat.getColor(this, R.color.diet_secondary),
            "medication" to
                    ContextCompat.getColor(this, R.color.medication_secondary)
        )

        //for each category, the category title is added and formatted
        for (category in buttonAttributes.keys)
        {
            val colour = categoryColours[category]!!
            val title: TextView = TextView(this)
            title.text = category.capitalize()
            title.textSize = 30F
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 30, 0, 3)
            if (category in "diet medication")
            {
                params.setMargins(0, 5, 0, 3)
            }
            title.layoutParams = params
            title.setTextColor(colour)
            editLinear.addView(title)
            /*a custom button layout is generated for each attribute of the category
            these either use edit_buttons.xml or list_edit_button.xml
            the button is customised by parsing in the appropriate
            EditButtonAttributes object, the selected date, and the category
            if a value has already been stored for spoons, for example, the button
            name and category is used within the editButton object to load it
            from sharedPreferences
             */
            for (button in buttonAttributes[category]!!)
            {
                if (button.type == "list")
                {
                    val listEditButton: ListEditButton = ListEditButton(this)
                    listEditButton.setUp(
                        button.name, category, thisDate,
                        categoryColours[category]!!)
                    listEditButton.hideCalories()
                    editLinear.addView(listEditButton)
                }
                else{
                    val editButton: EditButtons = EditButtons(this)
                    editButton.setUp(button,
                        categoryColours[category]!!, thisDate, category)
                    editLinear.addView(editButton)
                }
            }
        }
        editLinear.addView(TextView(this))

        //button to return the user to the calendar activity
        backButton.setOnClickListener {
            startActivity(Intent(this, Calendar::class.java))
        }

        //button to delete all existing data for this date
        clearDataButton.setOnClickListener {
            clearDate()
        }

    }

    private fun clearDate()
    {
        //creates a dialog box to confirm deletion
        val builder = AlertDialog.Builder(this)
            .setTitle("Delete this date's data?")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Delete"
            ) { _, _ ->
                //deletes data & refreshes page
                if (thisDate in prefs.dateList) {
                    prefs.clearDate(thisDate)
                    finish()
                    startActivity(intent)
                    overridePendingTransition(0,0)
                }
            }
        builder.show()
    }

    //formats a date of the form yyyy-mm-dd into [day] [month] [year]
    private fun formatDate(): String
    {
        val months = arrayOf("January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December")
        val dateSplit = thisDate.split("-")
        val year = dateSplit[0]
        val month = months[dateSplit[1].toInt()-1]
        val day = dateSplit[2].toInt()
        return "$day $month $year"
    }
}