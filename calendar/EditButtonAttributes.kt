package com.example.aboutme

import kotlin.properties.Delegates

data class EditButtonAttributes(var name: String) {
    var minimum = 0
    var maximum = 0
    var range: Array<String>? = null
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
