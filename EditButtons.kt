package com.example.aboutme

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class EditButtons : ConstraintLayout {
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
        View.inflate(context, R.layout.edit_buttons, this)
    }
}
