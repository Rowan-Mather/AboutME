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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.add_list_item_dialog.view.*
import kotlinx.android.synthetic.main.list_edit_button.view.*


//a compound view for the calendar date activity containing a text view and then a customisable list of items
class ListEditButton : ConstraintLayout {
    private var listName = ""
    private var listCategory = ""
    private var thisDate = ""
    private var editList: MutableList<String> = mutableListOf()

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

    //allows the user to set the name of the view and colour of the + button & calls loadList
    fun setUp(name: String, category: String, date: String, colour: Int)
    {
        listName = name
        listCategory = category
        thisDate = date
        editAddList.backgroundTintList = ColorStateList.valueOf(colour)
        listNameView.text = "${listName.capitalize()}:"
        loadList()
    }

    //loads data for the list using the name and category name
    private fun loadList()
    {
        val data = prefs.readData(thisDate, listCategory, listName)
        if (data == "ERROR: DATA NOT FOUND")
        {
            listNameView.text = "${listName.capitalize()}:  —"
        }
        else
        {
            //each item is added in turn
            val editableList = data.split("+").toMutableList()
            listLayout.removeAllViews()
            listNameView.text = "${listName.capitalize()}:"
            for (item in editableList)
            {
                addItem(item)
            }
        }
    }

    //this is called when the + button is clicked
    //it displays a pop-up window for adding an item to the list
    private fun addItemWindow()
    {
        //window design is inflated from add_list_item_dialog.xml
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_list_item_dialog, null)
        //a builder for the window is initialised with a title
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Add a ${listName.dropLast(1)}")
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
            .setPositiveButton("Add",
                DialogInterface.OnClickListener { _, _ ->
                    //when the add button in the dialog window is clicked
                    //the value in the window input box is stored
                    //the value is checked to not already be in the list and that its alphanumeric, then added to the list
                    val name = dialogView.listInputBox.text.toString()
                    if (name in editList)
                    { Toast.makeText(context, "That is already added", Toast.LENGTH_SHORT).show() }
                    else
                    {
                        if (name.matches("^[a-zA-Z0-9 ]*$".toRegex()) && name != "" && name != " ")
                        { addItem(name) }
                        else
                        { Toast.makeText(context, "Item name must be alphanumeric", Toast.LENGTH_SHORT).show() }
                    }
                })
        builder.show()
    }

    //an item is added to the list and displayed
    private fun addItem(name: String)
    {
        //the item is stored as a text view and a delete floating action button which are
        //nested in a horizontal linear layout
        val horizontalLayout: LinearLayout = LinearLayout(context)
        horizontalLayout.orientation = LinearLayout.HORIZONTAL
        //the delete button is configured
        val deleteButton: FloatingActionButton = FloatingActionButton(context)
        configureDeleteButton(deleteButton, name)
        //the text view is configured
        val symptomNameView: TextView = TextView(context)
        symptomNameView.text = name
        symptomNameView.textSize = 22F
        symptomNameView.gravity = Gravity.CENTER_VERTICAL
        symptomNameView.setTextColor(Color.parseColor("#505050"))
        //the views are added, the editList is updated, and shared preferences are updated
        horizontalLayout.addView(deleteButton)
        horizontalLayout.addView(symptomNameView)
        listLayout.addView(horizontalLayout)
        editList.add(name)
        writeList()
    }

    //this formats the delete buttons and sets up the on click listener to call deleteItem()
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
    //this removes an item from the list
    //it is deleted from editList and the linear layout, then shared preferences are updated
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

    //updates shared preferences with the current list
    private fun writeList()
    {
        prefs.writeData(thisDate, listCategory, listName, editList.joinToString("+"))
    }
}
