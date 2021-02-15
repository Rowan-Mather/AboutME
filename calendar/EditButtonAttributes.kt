package com.example.aboutme

import kotlin.properties.Delegates

//this class stores attribute data e.g. spoons have range 0-15
//objects of this class are used to set up the compound view of the text view & +/- buttons in the calendar date activity
data class EditButtonAttributes(var name: String) {
    var minimum = 0
    var maximum = 0
    var range: Array<String>? = null
    //the range of the attributes in calendar date can either be numeric e.g. Spoons: 1-10
    // or strings e.g. Sleep Quality: Poor, Fine, Good
    //the constructors set the objects up accordingly, and the function isIntRange tells you which type the object is
    constructor(name: String, min: Int, max: Int) : this(name) {
        minimum = min
        maximum = max
    }
    constructor(name: String, scrollRange: Array<String>): this(name)
    {
        range = scrollRange
    }
    fun isIntRange(): Boolean
    {
        if (range == null)
        {
            return true
        }
        return false
    }
}
