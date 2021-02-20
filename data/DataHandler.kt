package com.example.aboutme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DataHandler (context: Context) {
    //initialises the file/location in which I store all the app data - sharedPrefs
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPrefs.edit()

    //creates a list of all the dates on which information from the user is stored
    var dateList:  MutableList<String> = mutableListOf<String>()
    //fills the list with dates from sharedPrefs
    fun readDates()
    {
        val stringDates = sharedPrefs.getString("DATES", "")
        if (stringDates != null)
        {
            if (stringDates != ""){
                dateList = stringDates.split(",").toMutableList()
            }
            if ("" in dateList)
            {
                dateList.remove("")
            }
        }
    }

    //updates sharedPrefs with the dates in dateList
    private fun writeDates() {
        var stringDates = ""
        for (date in dateList) {
            stringDates += date + ","
        }
        stringDates.dropLast(1)
        editor.putString("DATES", stringDates)
        editor.apply()
    }

    //this adds a new date to the dateList
    private fun addDate(dateString: String)
    {
        //if the date is the most recent is is appended
        val date = convertDate(dateString)
        if ((dateList.isEmpty()) || (convertDate(dateList[dateList.lastIndex]) < date))
        {
            dateList.add(dateString)
        }
        //otherwise a simple binary search is performed to find the appropriate location for the new date
        else
        {
            var l: Int = 0
            var r: Int = dateList.lastIndex
            while (l <= r){
                val m: Int = ((l+r) / 2)
                if (convertDate(dateList[m]) < date) { l = m+1 }
                else if (convertDate(dateList[m]) > date) { r = m - 1 }
                else
                { return }
            }
            dateList.add(l, dateString)
        }
        writeDates()
    }

    //any data updates from the app are written to sharedPrefs through the below function
    fun writeData(date : String, category: String, infoName : String, infoValue: String)
    {
        /*
        this function takes
          - the date that the information is relevant to
          - the category (e.g. activity, sleep)
          - the attribute name of the information (e.g. steps, spoons)
          - the value for the information (e.g. 1033, 4)
        info is stored by key-value pair of date and all that dates data (both strings)
           e.g. Key: 07/01/2020
                value: ACTIVITY,spoons:3,steps:3021;SLEEP,hours:8,quality:good;SYMPTOMS,headache,fever
        (semicolon between categories which are specified in caps, comma between attributes, colon between attribute key-value pair)
        */

        readDates()
        if (dateList.contains(date))
        {
            //this runs if the date already has information stored on it
            //firstly, the category for the data is assigned an arbitrary integer
            //this corresponds to the order that the categories are stored in, namely: activity, sleep, symptoms
            var categoryNum = -1
            when (category) {
                "activity" -> categoryNum = 0
                "sleep" -> categoryNum = 1
                "symptoms" -> categoryNum = 2
            }
            //the existing data is read and formatted into arrays
            val dateInfo = sharedPrefs.getString(date, "")
            val dateSplit = dateInfo!!.split(";").toTypedArray()
            var attributesString = dateSplit[categoryNum]
            val attributes = attributesString.split(",").toMutableList()
            //if the attribute being written already exists, its position in the data is found
            var dataLocation = -1
            var i = 0
            for (attribute in attributes) {
                    if (attribute.contains("$infoName:")) {
                        dataLocation = i
                    }
                    i+=1
                }
            //if the attribute being written does not exist, it is added to the end of the category data
            if (dataLocation == -1)
            {
                if (infoValue != "") //checks data being written has actually been defined
                { attributesString = "$attributesString,$infoName:$infoValue" }
                else { return }
            }
            //otherwise the attribute is updated
            else
            {
                if (infoValue == "") //if the user has not defined the data, it is removed from the attribute list
                { attributes.removeAt(dataLocation) }
                else
                { attributes[dataLocation] = "$infoName:$infoValue" } //otherwise it is changed
                attributesString = attributes.joinToString(",")
            }
            dateSplit[categoryNum] = attributesString
            //the new information is written to sharedPrefs
            editor.putString(date,dateSplit.joinToString(";"))
            editor.apply()
        }
        else
        {
            //if the date doesn't already have information stored on it, it is added to sharedPrefs
            //the piece of information is then written to the new date in sharedPrefs
            if (infoValue == "")
            {
                //checks that the data being written has actually been defined
                return
            }
            addDate(date)
            var toWrite = ""
            when (category) {
                "activity" -> toWrite = "ACTIVITY,$infoName:$infoValue;SLEEP;SYMPTOMS"
                "sleep" -> toWrite = "ACTIVITY;SLEEP,$infoName:$infoValue;SYMPTOMS"
                "symptoms" -> toWrite = "ACTIVITY;SLEEP;SYMPTOMS,$infoName:$infoValue"
            }
            if (toWrite != "") {
                editor.putString(date, toWrite)
                editor.apply()
            }
        }
    }

    //any data displayed in the app is read from the below function
    fun readData(date : String, category: String, infoName : String): String {
        //as in the write function, the category for the data is assigned a corresponding integer
        var categoryNum = -1
        when (category) {
            "activity" -> categoryNum = 0
            "sleep" -> categoryNum = 1
            "symptoms" -> categoryNum = 2
        }
        //the data is split into categories & the appropriate category is selected
        //the category data is split into attributes
        val dateInfo = sharedPrefs.getString(date, "")
        if (dateInfo == "") { return "ERROR: DATA NOT FOUND" }
        val dateSplit = dateInfo!!.split(";").toTypedArray()
        val attributesString = dateSplit[categoryNum]
        val attributes = attributesString.split(",").toTypedArray()
        //the attributes are searched through for the desired one and if found, the corresponding value is returned as a string
        for (attribute in attributes) {
            if (attribute.contains("$infoName:")) {
                val foundInfo = attribute.split(":")?.toTypedArray()
                return foundInfo[1]
            }
        }
        //if the data is not found, this is returned
        return "ERROR: DATA NOT FOUND"
    }

    //removes the data from a given date
    fun clearDate(date: String)
    {
        editor.remove(date)
        dateList.remove(date)
        writeDates()
    }

    //this is specifically for writing the list of checkboxes that the user has added (i.e. symptoms)
    fun writeCheckBoxes(boxNames : Array<String>?)
    {
        var writeBoxNames = ""
        if ((boxNames != null) && (boxNames.isNotEmpty())) {
            writeBoxNames = boxNames.joinToString(",")
        }
        editor.putString("CHECKBOXES", writeBoxNames)
        editor.apply()
    }

    //reads the list of checkboxes that the user has added and returns it as an array
    fun readCheckBoxes() : Array<String>?
    {
        val checkBoxData = sharedPrefs.getString("CHECKBOXES", "")
        if (checkBoxData == "") { return null }
        return checkBoxData?.split(",")?.toTypedArray()
    }

    //writes a non date specific piece of information e.g. the maximum number of spoons
    fun writeSetting(prefName: String, prefInfo: String)
    {
        editor.putString(prefName, prefInfo)
        editor.apply()
    }

    //reads a non date specific piece of information
    fun readSetting(prefName: String) :String
    {
        return sharedPrefs.getString(prefName, "ERROR: DATA NOT FOUND")!!
    }

    //returns all the data for a given day
    fun readAll(date : String): String? {
        val dateInfo = sharedPrefs.getString(date, "")
        return dateInfo
    }

    fun convertDate(date: String) : Date
    {
        val dateSplit = date.split("-")
        return Date(
            dateSplit[0].toInt() - 1900,
            dateSplit[1].toInt() - 1,
            dateSplit[2].toInt()
        )
    }

    //a function to return the actual current date
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate() :String
    {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        return formatted
    }

}
