package com.example.aboutme

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.diet.view.*
import kotlinx.android.synthetic.main.food_item.view.*
import kotlinx.android.synthetic.main.list_edit_button.view.*

//this is a compound view for items in the foods dictionary in the diet activity
class FoodItem : ConstraintLayout {
    private var foodName: String = ""

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
        //gets the xml design for the compound view
        View.inflate(context, R.layout.food_item, this)
    }

    //this sets up the custom graphical layout for the compound view
    fun setUp(name: String, calories: Int, tags: String)
    {
        //it sets the name of the item
        foodName = name
        //checks if there are calories and displays them
        if (calories > 0)
        { foodItem.text = "$name ($calories cal)" }
        else
        { foodItem.text = name }
        //checks if there are tags and adds them in turn to the layout
        if (tags != ""){
            for (tag in tags.split(","))
            {
                val tagView = TextView(context)
                tagView.text = tag
                tagView.textSize = 16F
                val params = LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                params.setMargins(10, 0, 10, 0)
                tagView.layoutParams = params
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    tagView.background = ContextCompat.getDrawable(context, R.drawable.orange_border)
                }
                foodItemLinear.addView(tagView)
            }
        }
    }
}
