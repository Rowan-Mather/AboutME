package com.example.aboutme

/*this class stores attribute data e.g. spoons have range 0-15
objects of this class are used to set up the compound views in the calendar date activity
the compound view can be one of three types:
    - a min-max button set with integer values e.g. spoons
    - a min-max button set with string values e.g. quality of sleep
    - a list e.g. symptoms
    depending on the secondary constructor used, this type is determined
 */
data class EditButtonAttributes(var name: String) {
    var minimum = 0
    var maximum = 0
    var range: Array<String>? = null
    var type: String = ""
    init {
        type = "list"
    }
    constructor(name: String, min: Int, max: Int) : this(name) {
        minimum = min
        maximum = max
        type = "intRange"
    }
    constructor(name: String, scrollRange: Array<String>): this(name)
    {
        range = scrollRange
        type = "stringRange"
    }
    fun isIntRange(): Boolean
    {
        if (type == "intRange")
        {
            return true
        }
        return false
    }
}
