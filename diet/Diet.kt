package com.example.aboutme

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.add_food_dialog.view.*
import kotlinx.android.synthetic.main.diet.*
import kotlinx.android.synthetic.main.food_item.view.*


@RequiresApi(Build.VERSION_CODES.O)
class Diet : AppCompatActivity() {
    //the dictionary of foods that the user can add to their live list
    //the key is the name of the food and the value is a tuple of the calories as an int
    //and the tags as a string
    private var foodDictionary: MutableMap<String, Pair<Int, String>> = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diet)
        prefs.writeSetting("PAGE", "diet")

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationDiet)
        bottomNavigationView.selectedItemId = R.id.diet
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> { startActivity(Intent(this, MainActivity::class.java)) }
                R.id.sleep -> { startActivity(Intent(this, Sleep::class.java)) }
                R.id.symptoms -> { startActivity(Intent(this, Symptoms::class.java)) }
            }
            overridePendingTransition(0, 0)
            true
        }

        loadFoods()

        //sets listener for the add to dictionary button
        addFoodButton.setOnClickListener()
        {
            addFoodWindow()
        }

        //when the input in the search bar changes, the dictionary view is updated
        foodSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable)
            {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int)
            {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {
                updateDictionaryView(searchFoods(s.toString()))
            }
        })
    }

    //loads the user's dictionary of foods from shared prefs
    private fun readDictionary()
    {
        val stringDictionary = prefs.readSetting("FOODDICTIONARY")
        if (stringDictionary != "ERROR: DATA NOT FOUND")
        {
            foodDictionary.clear()
            for (food in stringDictionary.split(";"))
            {
                val foodSplit = food.split("#")
                val name = foodSplit[0]
                val calories = foodSplit[1].toInt()
                val tags = foodSplit[2]
                foodDictionary[name] = Pair(calories, tags)
            }
            //sorts dictionary
            foodDictionary = foodDictionary.toSortedMap().toMutableMap()
        }
    }

    //updates shared prefs with the food dictionary
    private fun writeDictionary()
    {
        val writeList = mutableListOf<String>()
        for (food in foodDictionary.keys)
        {
            val calories = foodDictionary[food]?.first
            val tags = foodDictionary[food]?.second
            writeList.add("$food#$calories#$tags")
        }
        val write = if (writeList.isEmpty()) { "ERROR: DATA NOT FOUND" } else { writeList.joinToString(";") }
        prefs.writeSetting("FOODDICTIONARY", write)
    }

    //sets up the dictionary and list of food views
    private fun loadFoods()
    {
        readDictionary()
        updateDictionaryView(null)
        foodsList.setUp("foods", "diet", prefs.getCurrentDate(), Color.BLACK, true)
        foodsList.hideAddButton()
    }

    //when the + (add food to dictionary) button is clicked, this is called to handle the dialog box
    private fun addFoodWindow()
    {
        //the dialog box is built
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.add_food_dialog, null)
        //a builder for the window is initialised with a title
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add a new food")
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
            .setPositiveButton("Add",
                //when the add button is clicked, the entered values are retrieved
                DialogInterface.OnClickListener { _, _ ->
                    val name = dialogView.foodNameInput.foodNameInputText.text.toString()
                    val calories = dialogView.foodCaloriesInput.foodCaloriesInputText.text.toString()
                    val tags = dialogView.foodTagsInput.foodTagsInputText.text.toString()
                    //checks that the name entered is valid
                    if (name != "" && name != " " && name.matches("^[a-zA-Z0-9 ]*$".toRegex())) {
                        if (name in foodDictionary.keys)
                        {
                            Toast.makeText(this,"You have already added this food to your dictionary", Toast.LENGTH_SHORT).show()
                        }
                        else
                        {
                            //checks that the calories entered is valid, if left empty, value defaults to -1
                            var caloriesInt = -1
                            try {
                                caloriesInt = calories.toInt()
                                if (caloriesInt <= 0){ 0/0 }
                            }
                            catch (e: Exception)
                            {
                                if (calories != "") {
                                    Toast.makeText(this,"You can leave the calories box blank, or it must be a whole number", Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this,"You can leave the tags box blank, or list them separated by commas", Toast.LENGTH_SHORT).show()
                                    return@OnClickListener
                                }
                            }
                            //if there are no errors, the item is added to the live list and the dictionary
                            foodsList.addItem("$name#$caloriesInt#$tagString")
                            foodDictionary[name] = Pair(caloriesInt, tagString)
                            foodDictionary = foodDictionary.toSortedMap().toMutableMap()
                            writeDictionary()
                            updateDictionaryView(null)
                        }
                    }
                    else
                    {
                        Toast.makeText(applicationContext,"Food name should be alphanumeric", Toast.LENGTH_SHORT).show()
                    }
                })
        builder.show()
    }

    //the dictionary is search for all keys beginning with the given value
    //the array of matches is returned
    private fun searchFoods(search: String): Array<String>
    {
        val matches = mutableListOf<String>()
        for (food in foodDictionary.keys)
        {
            if (food.startsWith(search))
            {
                matches.add(food)
            }
        }
        return matches.toTypedArray()
    }

    //this clears and recreates all the views in the dictionary
    //if the user is searching, an array of matches is provided,
    //otherwise it displays the whole dictionary
    private fun updateDictionaryView(foodsArray: Array<String>?)
    {
        foodScrollLinear.removeAllViews()
        val foods = foodsArray ?: foodDictionary.keys.toTypedArray()
        if (foods.isEmpty())
        {
            val foodView = TextView(this)
            foodView.text = "  No foods found"
            foodScrollLinear.addView(foodView)
        }
        else
        {
            for (food in foods)
            {
                //creates the compound view for one item
                val foodView = FoodItem(this)
                val calories = foodDictionary[food]!!.first
                val tags = foodDictionary[food]!!.second
                foodView.setUp(food, calories, tags)

                foodView.addFoodItem.setOnClickListener()
                {
                    //if the add button for an item in the dictionary is clicked,
                    //the item is added to the live list
                    foodsList.addItem("$food#$calories#$tags")
                }
                foodView.deleteFoodButton.setOnClickListener() {
                    //if the delete button for an item in the dictionary is clicked,
                    //the item is removed from the dictionary and any instances of that item in the
                    //live list are removed
                    foodDictionary.remove(food)
                    writeDictionary()
                    foodsList.deleteAllType("$food#$calories#$tags")
                    updateDictionaryView(null)
                }

                foodScrollLinear.addView(foodView)
            }
        }
    }
}
