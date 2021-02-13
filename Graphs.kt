package com.example.aboutme

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.graphs.*
import java.util.*
import kotlin.collections.HashMap


class Graphs: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graphs)

        //controls the clicks on the bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationGraphs)
        bottomNavigationView.selectedItemId = R.id.graphs
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.sleep -> {
                    startActivity(Intent(this, Sleep::class.java))
                }
                R.id.symptoms -> {
                    startActivity(Intent(this, Symptoms::class.java))
                }
                R.id.calendar -> {
                    startActivity(Intent(this, Calendar::class.java))
                }
            }
            overridePendingTransition(0,0)
            true
        }

        //ensures all the dates on which data is stored is loaded
        prefs.readDates()

        //initialises the graph view and sets the y axis scale and stops the zero lines being bold
        val graph: GraphView = findViewById(R.id.graph)
        var potentialHighest = 12
        var maxSpoonString = prefs.readSetting("MAXSPOONS")
        //the number of spoons is the only available measure that could exceed 10, 
        // hence it determines the scale of the y axis
        if (maxSpoonString != "ERROR: DATA NOT FOUND") 
        {
            if (maxSpoonString.toInt() <= 10)
            {
                graph.viewport.setMinY(0.0)
                graph.viewport.setMaxY(10.0)
                graph.gridLabelRenderer.numVerticalLabels = 11
            }
            else if (maxSpoonString.toInt() <= 12)
            {
                graph.viewport.setMinY(0.0)
                graph.viewport.setMaxY(12.0)
                graph.gridLabelRenderer.numVerticalLabels = 13
            }
            else
            {
                graph.viewport.setMinY(0.0)
                graph.viewport.setMaxY(15.0)
                graph.gridLabelRenderer.numVerticalLabels = 16
            }
        }
        graph.viewport.isYAxisBoundsManual = true
        graph.gridLabelRenderer.isHighlightZeroLines = false
        graph.gridLabelRenderer.setHumanRounding(false)

        //initialises the dictionary selectedGraphs which contains each graph title and true/false
        //the user's previous selection of graphs is loaded if applicable and the switches are toggled accordingly
        var selectedGraphs = HashMap<String, Boolean>()
        var previousSelected = prefs.readSetting("SELECTEDGRAPHS")
        if (previousSelected == "ERROR: DATA NOT FOUND") { previousSelected = "0000" }
        selectedGraphs["spoons"] = previousSelected[0] == '1'
        selectedGraphs["hours"] = previousSelected[1] == '1'
        selectedGraphs["health"] = previousSelected[2] == '1'
        selectedGraphs["fatigue"] = previousSelected[3] == '1'
        spoonsSwitch.isChecked = selectedGraphs["spoons"]!!
        hoursSwitch.isChecked = selectedGraphs["hours"]!!
        healthSwitch.isChecked = selectedGraphs["health"]!!
        fatigueSwitch.isChecked = selectedGraphs["fatigue"]!!

        //the switches are colour coded and the appropriate graphs drawn
        colourSwitches(selectedGraphs)
        drawGraphs(getArrayOfSelectedGraphs(selectedGraphs))

        //these listeners are called when their respective switches are clicked
        //they update the selectedGraphs dictionary and call the update method
        spoonsSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            selectedGraphs["spoons"] = isChecked
            update(selectedGraphs)
        })
        hoursSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            selectedGraphs["hours"] = isChecked
            update(selectedGraphs)
        })
        healthSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            selectedGraphs["health"] = isChecked
            update(selectedGraphs)
        })
        fatigueSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            selectedGraphs["fatigue"] = isChecked
            update(selectedGraphs)
        })
    }

    /*this method calls all the functions to
       - update shared preferences with the switches that are currently selected
       - colour code the switches
       - draw all the selected graphs
       - refresh the graph view
     */
    fun update(selectedGraphs: HashMap<String, Boolean>)
    {
        writeSelectedGraphs(selectedGraphs)
        colourSwitches(selectedGraphs)
        drawGraphs(getArrayOfSelectedGraphs(selectedGraphs))
        reloadGraph()
    }

    //this colour codes the switches so that when they are selected they are highlighted in the
    //same colour as the graph drawn and, when they are not selected they are grey
    fun colourSwitches(selectedGraphs: HashMap<String, Boolean>)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            var colour = Color.BLACK

            colour = if (selectedGraphs["spoons"]!!)
            { Color.rgb(112, 200, 255) } else { Color.GRAY } //blue for spoons
            spoonsSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)

            colour = if (selectedGraphs["hours"]!!)
            { Color.rgb(183, 125, 255) } else { Color.GRAY } //purple for hours of sleep
            hoursSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
            hoursSwitch.trackDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)

            colour = if (selectedGraphs["health"]!!)
            { Color.rgb(90, 176, 124) } else { Color.GRAY } //dark green for health
            healthSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
            healthSwitch.trackDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)

            colour = if (selectedGraphs["fatigue"]!!)
            { Color.rgb(165, 240, 192) } else { Color.GRAY } //light green for fatigue
            fatigueSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
            fatigueSwitch.trackDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
        }
    }

    //this actually reloads the whole activity, but it does so in order to update the graph view
    // when another graph has been selected/deselected
    fun reloadGraph()
    {
        finish()
        startActivity(intent)
        overridePendingTransition(0,0)
    }

    //this updates shared preferences with the switches that are currently selected
    fun writeSelectedGraphs(selectedGraphs: HashMap<String, Boolean>)
    {
        var selectedSummary = ""
        for (graph in arrayOf("spoons", "hours", "health", "fatigue")) {
            selectedSummary += if (selectedGraphs[graph] == true) "1" else "0"
        }
        prefs.writeSetting("SELECTEDGRAPHS", selectedSummary)
    }

    //this takes the dictionary of selectedGraphs and creates an array of just the graphs that are set to true
    fun getArrayOfSelectedGraphs(selectedGraphs: HashMap<String, Boolean>) : Array<String>
    {
        var selected = mutableListOf<String>()
        for (graph in selectedGraphs.keys)
        {
            if (selectedGraphs[graph] == true) { selected.add(graph) }
        }
        return selected.toTypedArray()
    }

    //this controls the graph view, cycling through the array of selected graphs and drawing them
    fun drawGraphs(attributes: Array<String>)
    {
        val graph: GraphView = findViewById(R.id.graph)
        //the graph view is reset
        graph.removeAllSeries()
        var minDate: Date? = null
        var maxDate: Date? = null
        /*Each graph is drawn in turn.
        Firstly the graph name is used to deduce the category and colour that the graph should be drawn.
        The points of the graph are stored in myDataPoints.
        The data points are found by cycling through all the dates where data is stored and checking
        if there is relevant data to the current graph being drawn.
        The min and max date are updated when a date when a date with relevant data is found to be
        greater than or less than the current min and max date.
        The data points are synthesised into a series and this is added to the graph view
         */
        for (attribute in attributes) {
            var myDataPoints: MutableList<DataPoint> = mutableListOf()
            var thisCategory = ""
            var graphColor = Color.BLACK
            when (attribute) {
                "spoons" -> {
                    thisCategory = "activity"
                    graphColor = Color.rgb(112, 200, 255)
                }
                "hours" -> {
                    thisCategory = "sleep"
                    graphColor = Color.rgb(183, 125, 255)
                }
                "health" -> {
                    thisCategory = "symptoms"
                    graphColor = Color.rgb(90, 176, 124)
                }
                "fatigue" -> {
                    thisCategory = "symptoms"
                    graphColor = Color.rgb(165, 240, 192)
                }
            }
            for (stringDate in prefs.dateList) {
                val attributeData = prefs.readData(stringDate, thisCategory, attribute)
                if (attributeData != "ERROR: DATA NOT FOUND") {
                    val dateSplit = stringDate.split("-")
                    val date: Date = Date(
                        dateSplit[0].toInt() - 1900,
                        dateSplit[1].toInt() - 1,
                        dateSplit[2].toInt()
                    )
                    if ((maxDate == null) || (date > maxDate)) {
                        maxDate = date
                    }
                    if ((minDate == null) || (date < minDate)) {
                        minDate = date
                    }
                    myDataPoints.add(DataPoint(date, attributeData.toInt().toDouble()))
                }
            }
            if (myDataPoints.size != 0) {

                val series = LineGraphSeries(myDataPoints.toTypedArray())
                series.color = graphColor
                series.isDrawDataPoints = true;
                series.dataPointsRadius = 7F
                series.thickness = 6
                graph.addSeries(series)
            }
        }
        //this sets the minimum and maximum point of the x axis for the data found
        if ((minDate != null) && (maxDate != null))
        {
            graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(applicationContext)
            graph.viewport.setMinX(minDate.time.toDouble())
            graph.viewport.setMaxX(maxDate.time.toDouble())
            graph.viewport.isXAxisBoundsManual = true
        }
    }
}
