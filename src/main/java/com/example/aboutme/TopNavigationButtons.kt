package com.example.aboutme

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.top_navigation.view.*

//this is the compound view for the calendar and graphs buttons at the top of the activities
class TopNavigationButtons: ConstraintLayout {
    //initialises class as the compound view handler
    @JvmOverloads
    constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context!!, attrs, defStyleAttr) {
        init(context)
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context!!, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }
    private fun init(context: Context?) {
        View.inflate(context, R.layout.top_navigation, this)

        //starts the appropriate activity when a button is clicked
        calendarNavButton.setOnClickListener()
        {
            getContext().startActivity(Intent(context, Calendar::class.java))
        }
        graphsNavButton.setOnClickListener()
        {
            getContext().startActivity(Intent(context, Graphs::class.java))
        }
    }
}