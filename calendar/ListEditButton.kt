package com.example.aboutme

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isNotEmpty
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.add_list_item_dialog.view.*
import kotlinx.android.synthetic.main.calendardate.view.*
import kotlinx.android.synthetic.main.edit_buttons.view.*
import kotlinx.android.synthetic.main.list_edit_button.view.*


//the compound view for the calendar date activity containing a text view and +/- buttons
class ListEditButton : ConstraintLayout {
    var listName = ""
    private var listCategory = ""
    private var thisDate = ""
    private var editList: MutableList<String> = mutableListOf()
    private lateinit var supportFragmentManager: FragmentManager

    //initialises class as the compound view handler
    @JvmOverloads
    constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context!!, attrs, defStyleAttr) {
        init()
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context!!, attrs, defStyleAttr, defStyleRes) {
        init()
    }
    private fun init() {
        View.inflate(context, R.layout.list_edit_button, this)

        //+ button listener
        editAddList.setOnClickListener{
            addItemWindow()
        }
    }

    fun setUp(name: String, category: String, date: String, colour: Int, fragmentManager: FragmentManager)
    {
        listName = name
        listCategory = category
        thisDate = date
        editAddList.backgroundTintList = ColorStateList.valueOf(colour)
        listNameView.text = "${listName.capitalize()}:"
        loadList()
        supportFragmentManager = fragmentManager
    }

    private fun loadList()
    {
        val data = prefs.readData(thisDate, listCategory, listName)
        if (data == "ERROR: DATA NOT FOUND")
        {
            listNameView.text = "${listName.capitalize()}:  —"
        }
        else
        {
            val editableList = data.split("+").toMutableList()
            listLayout.removeAllViews()
            listNameView.text = "${listName.capitalize()}:"
            for (item in editableList)
            {
                addItem(item)
            }
        }
    }

    private fun addItemWindow()
    {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_list_item_dialog, null)
        val mBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Add a ${listName.dropLast(1)}")
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
            .setPositiveButton("Add",
                DialogInterface.OnClickListener { dialog, id ->
                    val name = dialogView.listInputBox.text.toString()
                    if (name in editList)
                    {
                        Toast.makeText(context, "That is already added", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        if (name.matches("^[a-zA-Z0-9 ]*$".toRegex()) && name != "" && name != " ")
                        {
                            addItem(name)
                        }
                        else
                        {
                            Toast.makeText(context, "Item name must be alphanumeric", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        val mAlertDialog = mBuilder.show()
    }

    private fun addItem(name: String)
    {
        val horizontalLayout: LinearLayout = LinearLayout(context)
        horizontalLayout.orientation = LinearLayout.HORIZONTAL
        val deleteButton: FloatingActionButton = FloatingActionButton(context)
        configureDeleteButton(deleteButton, name)

        val symptomNameView: TextView = TextView(context)
        symptomNameView.text = name
        symptomNameView.textSize = 22F
        symptomNameView.gravity = Gravity.CENTER_VERTICAL
        symptomNameView.setTextColor(Color.parseColor("#505050"))

        horizontalLayout.addView(deleteButton)
        horizontalLayout.addView(symptomNameView)
        listLayout.addView(horizontalLayout)
        editList.add(name)
        writeList()
    }

    private fun configureDeleteButton(deleteButton: FloatingActionButton, name: String)
    {
        deleteButton.setImageResource(R.drawable.ic_baseline_remove_24)
        deleteButton.size = FloatingActionButton.SIZE_MINI
        deleteButton.scaleX = 0.8F
        deleteButton.scaleY = 0.8F
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            deleteButton.outlineSpotShadowColor = Color.WHITE
        }
        val params = LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        deleteButton.layoutParams = params
        params.setMargins(0, 0, 40, 0)
        deleteButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF8080"))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            deleteButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
        }
        deleteButton.setOnClickListener(OnClickListener {
            deleteItem(name)
        })
    }
    private fun deleteItem(name: String)
    {
        val removeIndex = editList.indexOf(name)
        listLayout.removeViewAt(editList.indexOf(name))
        editList.removeAt(removeIndex)
        if (editList.isEmpty())
        {
            listNameView.text = "${listName.capitalize()}:  —"
        }
        writeList()
    }
    private fun writeList()
    {
        prefs.writeData(thisDate, listCategory, listName, editList.joinToString("+"))
    }
}
