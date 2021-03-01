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
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.add_food_dialog.view.*
import kotlinx.android.synthetic.main.add_list_item_dialog.view.*
import kotlinx.android.synthetic.main.list_edit_button.view.*


//a compound view for the calendar date activity containing a text view and then a customisable list of items
class ListEditButton : ConstraintLayout {
    private var listName = ""
    private var listCategory = ""
    private var thisDate = ""
    private var editList: MutableList<String> = mutableListOf()
    private var extras = false
    private var total = 0

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

    fun setUp(name: String, category: String, date: String, colour: Int)
    {
        setUp(name, category, date, colour, false)
    }

    //allows the user to set the name of the view and colour of the + button & calls loadList
    //also if the view is the diet food list and has calories and tags, these are added
    fun setUp(name: String, category: String, date: String, colour: Int, hasExtras: Boolean)
    {
        listName = name
        listCategory = category
        thisDate = date
        editAddList.backgroundTintList = ColorStateList.valueOf(colour)
        extras = hasExtras
        loadList()
        if (hasExtras)
        {
            calculateTotalCalories()
        }
    }

    //hides the add item button
    fun hideAddButton()
    {
        editAddList.hide()
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
    fun addItemWindow()
    {
        //if the view is the food list, then the add food dialog is loaded
        if (extras)
        {
            //window design is inflated from add_list_item_dialog.xml
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.add_food_dialog, null)
            //a builder for the window is initialised with a title
            val builder = AlertDialog.Builder(context)
                .setView(dialogView)
                .setTitle("Add a new food")
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
                .setPositiveButton("Add",
                    DialogInterface.OnClickListener { _, _ ->
                        val name = dialogView.foodNameInput.foodNameInputText.text.toString()
                        val calories = dialogView.foodCaloriesInput.foodCaloriesInputText.text.toString()
                        val tags = dialogView.foodTagsInput.foodTagsInputText.text.toString()
                        //checks that the calories entered is valid, if left empty, value defaults to -1
                        var caloriesInt = -1
                        try {
                            caloriesInt = calories.toInt()
                            if (caloriesInt <= 0){ 0/0 }
                        }
                        catch (e: Exception)
                        {
                            if (calories != "") {
                                Toast.makeText(context,"You can leave the calories box blank, or it must be a whole number", Toast.LENGTH_SHORT).show()
                                return@OnClickListener
                            }
                        }
                        //edits the tags entered so they are saved in the form 'tag1!tag2!tag3'
                        var tagString = ""
                        if (tags != "")
                        {
                            if (tags != " " && tags.matches("^[a-zA-Z0-9, ]*$".toRegex()))
                            {
                                val tagSplit = tags.split(",").toMutableList()
                                var i = 0
                                while (i < tagSplit.size)
                                {
                                    tagSplit[i] = tagSplit[i].trim()
                                    if (tagSplit[i] == "" || tagSplit[i] == " ")
                                    {
                                        tagSplit.removeAt(i)
                                    }
                                    i++
                                }
                                tagString = tagSplit.joinToString("!")
                            }
                            else
                            {
                                Toast.makeText(context,"You can leave the tags box blank, or list them separated by commas", Toast.LENGTH_SHORT).show()
                                return@OnClickListener
                            }
                        }
                        //adds the new item to the list
                        addItem("$name#$caloriesInt#$tagString")
                    })
            builder.show()
        }
        else {
            //otherwise window design is inflated from add_list_item_dialog.xml
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.add_list_item_dialog, null)
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
                        if (name in editList) {
                            Toast.makeText(context, "That is already added", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            if (name.matches("^[a-zA-Z0-9 ]*$".toRegex()) && name != "" && name != " ") {
                                addItem(name)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Item name must be alphanumeric",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
            builder.show()
        }
    }

    //an item is added to the list and displayed
    fun addItem(item: String)
    {
        //update the editList & shared prefs
        editList.add(item)
        writeList()
        //the item is stored as a text view and a delete floating action button which are
        //nested in a horizontal linear layout
        val horizontalLayout: LinearLayout = LinearLayout(context)
        horizontalLayout.orientation = LinearLayout.HORIZONTAL
        //the delete button is configured
        val deleteButton: FloatingActionButton = FloatingActionButton(context)
        configureDeleteButton(deleteButton, editList.lastIndex)
        //the text view is configured
        val symptomNameView: TextView = TextView(context)
        symptomNameView.text = item.split("#")[0]
        symptomNameView.textSize = 22F
        symptomNameView.gravity = Gravity.CENTER_VERTICAL
        symptomNameView.setTextColor(Color.parseColor("#505050"))
        //the views are added
        horizontalLayout.addView(deleteButton)
        horizontalLayout.addView(symptomNameView)
        if (extras)
        {
            //if the item has tags etc, these are added
            addExtras(item, horizontalLayout)
            calculateTotalCalories()
        }
        listLayout.addView(horizontalLayout)
        listNameView.text = "${listName.capitalize()}:"
    }
    
    //if its the food list, this adds to a new view the calories and tags
    private fun addExtras(item: String, horizontalLayout: LinearLayout)
    {
        val itemSplit = item.split("#")
        if (itemSplit.size > 1) {
            val name = itemSplit[0]
            val calories = itemSplit[1].toInt()
            val tags = itemSplit[2]
            //if calories is not -1, the view is added
            if (calories > 0) {
                val extraView: TextView = TextView(context)
                extraView.text = " ($calories cal)"
                extraView.textSize = 22F
                extraView.gravity = Gravity.CENTER_VERTICAL
                extraView.setTextColor(Color.parseColor("#505050"))
                horizontalLayout.addView(extraView)
            }
            //if there are tags, they are added in turn
            if (tags != "") {
                for (tag in tags.split("!")) {
                    val tagView = TextView(context)
                    tagView.text = tag
                    tagView.textSize = 16F
                    val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    params.setMargins(20, 0, 2, 0)
                    tagView.layoutParams = params
                    //tags get a box background
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        tagView.background =
                            ContextCompat.getDrawable(context, R.drawable.orange_border)
                    }
                    horizontalLayout.addView(tagView)
                }
            }
        }
    }

    //this formats the delete buttons and sets up the on click listener to call deleteItem()
    private fun configureDeleteButton(deleteButton: FloatingActionButton, index: Int)
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
            deleteItem(index)
        })
    }

    //removes an item from the list
    private fun deleteItem(index: Int)
    {
        listLayout.removeViewAt(index)
        editList.removeAt(index)
        if (editList.isEmpty())
        {
            listNameView.text = "${listName.capitalize()}:  —"
        }
        writeList()
        if (extras) { calculateTotalCalories() }
    }

    //removes all the items that match the given string from the list
    fun deleteAllType(item: String)
    {
        while (item in editList)
        {
            deleteItem(editList.indexOf(item))
        }
        writeList()

    }

    //updates shared preferences with the current list
    private fun writeList()
    {
        prefs.writeData(thisDate, listCategory, listName, editList.joinToString("+"))
    }

    //if the list is the food list, this updates the total calories view & shared prefs
    private fun calculateTotalCalories()
    {
        total = 0
        //it cycles through the list, adding to total if there are calories
        for (food in editList)
        {
            val calorieString = food.split("#")[1]
            if (calorieString != "-1")
            {
                total += calorieString.toInt()
            }
        }
        //updates text view
        if (total != 0)
        {
            prefs.writeData(thisDate, "diet", "total calories", total.toString())
            totalView.text = "Total Calories: $total"
        }
        else
        {
            totalView.text = "Total Calories: -"
        }
    }
}
