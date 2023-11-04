package com.example.aboutme

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.graphs.*
import java.util.*
import kotlin.collections.HashMap

@RequiresApi(Build.VERSION_CODES.O)
class Graphs: AppCompatActivity() {
    //initialises the dictionary selectedGraphs
    // which contains each graph title and true/false
    private val selectedGraphs = HashMap<String, Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graphs)

        graphsBackButton.setOnClickListener()
        {
            when (prefs.readSetting("PAGE"))
            {
                "activity" ->
                    startActivity(Intent(this, MainActivity::class.java))
                "sleep" ->
                    startActivity(Intent(this, Sleep::class.java))
                "symptoms" ->
                    startActivity(Intent(this, Symptoms::class.java))
                "diet" ->
                    startActivity(Intent(this, Diet::class.java))
                "medication" ->
                    startActivity(Intent(this, Medication::class.java))
                else ->
                    startActivity(Intent(this, MainActivity::class.java))
            }
        }

        //ensures all the dates on which data is stored is loaded
        prefs.readDates()

        //initialises the graph view
        val graph: GraphView = findViewById(R.id.graph)
        graph.viewport.setMinY(0.0)
        graph.viewport.setMaxY(10.0)
        graph.gridLabelRenderer.numVerticalLabels = 11
        graph.viewport.isYAxisBoundsManual = true
        val date = prefs.convertDate(prefs.getCurrentDate()).time.toDouble()
        graph.viewport.setMinX(date - 86400000)
        graph.viewport.setMaxX(date)
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
        graph.viewport.isXAxisBoundsManual = true
        graph.gridLabelRenderer.isHighlightZeroLines = false
        graph.gridLabelRenderer.setHumanRounding(false)

        //the user's previous selection of graphs is loaded
        // if applicable and the switches are toggled accordingly
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
        colourSwitches()
        drawGraphs(getArrayOfSelectedGraphs())

        //these listeners are called when their respective switches are clicked
        //they update the selectedGraphs dictionary and call the update method
        spoonsSwitch.setOnCheckedChangeListener { _, isChecked ->
            selectedGraphs["spoons"] = isChecked
            update()
        }
        hoursSwitch.setOnCheckedChangeListener { _, isChecked ->
            selectedGraphs["hours"] = isChecked
            update()
        }
        healthSwitch.setOnCheckedChangeListener { _, isChecked ->
            selectedGraphs["health"] = isChecked
            update()
        }
        fatigueSwitch.setOnCheckedChangeListener { _, isChecked ->
            selectedGraphs["fatigue"] = isChecked
            update()
        }
    }

    /*this method calls all the functions to
       - update shared preferences with the switches that are currently selected
       - colour code the switches
       - draw all the selected graphs
       - refresh the graph view
     */
    private fun update()
    {
        writeSelectedGraphs()
        colourSwitches()
        drawGraphs(getArrayOfSelectedGraphs())
        reloadGraph()
    }

    //this colour codes the switches so that when they are selected
    //they are highlighted in the same colour as the graph drawn and
    // when they are not selected they are grey
    private fun colourSwitches()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            var colour = Color.BLACK

            //blue for spoons
            colour = if (selectedGraphs["spoons"]!!)
            { ContextCompat.getColor(this, R.color.activity_graph) }
            else { Color.GRAY }
            spoonsSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)

            //purple for hours of sleep
            colour = if (selectedGraphs["hours"]!!)
            { ContextCompat.getColor(this, R.color.sleep_graph) }
            else { Color.GRAY }
            hoursSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
            hoursSwitch.trackDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)

            //dark green for health
            colour = if (selectedGraphs["health"]!!)
            { Color.rgb(90, 176, 124) }
            else { Color.GRAY }
            healthSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
            healthSwitch.trackDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)

            //light green for fatigue
            colour = if (selectedGraphs["fatigue"]!!)
            { ContextCompat.getColor(this, R.color.symptoms_graph)}
            else { Color.GRAY }
            fatigueSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
            fatigueSwitch.trackDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
        }
    }

    //this actually reloads the whole activity, but it does so
    // in order to update the graph view when a graph has
    // been selected/deselected
    private fun reloadGraph()
    {
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    //this updates shared preferences with the switches that are currently selected
    private fun writeSelectedGraphs()
    {
        var selectedSummary = ""
        for (graph in arrayOf("spoons", "hours", "health", "fatigue")) {
            selectedSummary += if (selectedGraphs[graph] == true) "1" else "0"
        }
        prefs.writeSetting("SELECTEDGRAPHS", selectedSummary)
    }

    //this takes the dictionary of selectedGraphs and creates
    // an array of just the graphs that are set to true
    private fun getArrayOfSelectedGraphs() : Array<String>
    {
        val selected = mutableListOf<String>()
        for (graph in selectedGraphs.keys)
        {
            if (selectedGraphs[graph] == true) { selected.add(graph) }
        }
        return selected.toTypedArray()
    }

    //this controls the graph view, cycling through the array
    // of selected graphs and drawing them
    private fun drawGraphs(attributes: Array<String>)
    {
        val graph: GraphView = findViewById(R.id.graph)
        //the graph view is reset
        if (graph.series.isNotEmpty())
        {
            graph.removeAllSeries()
        }
        var minDate: Date? = null
        var maxDate: Date? = null
        var maxY: Double = 0.0
        /*Each graph is drawn in turn.
        Firstly the graph name is used to deduce the category
        and colour that the graph should be drawn.
        The points of the graph are stored in myDataPoints.
        The data points are found by cycling through all the dates where data
        is stored and checking if there is relevant data to the current graph
        being drawn.
        The min and max date are updated when a date when a date with
        relevant data is found to be greater than or less than the current
        min and max date.
        The data points are synthesised into a series and this is added
        to the graph view */
        for (attribute in attributes) {
            val dataPoints: MutableList<DataPoint> = mutableListOf()
            var thisCategory = ""
            var graphColor = Color.BLACK
            when (attribute) {
                "spoons" -> {
                    thisCategory = "activity"
                    graphColor = ContextCompat.getColor(this, R.color.activity_graph)
                }
                "hours" -> {
                    thisCategory = "sleep"
                    graphColor = ContextCompat.getColor(this, R.color.sleep_graph)
                }
                "health" -> {
                    thisCategory = "symptoms"
                    graphColor = Color.rgb(90, 176, 124)
                }
                "fatigue" -> {
                    thisCategory = "symptoms"
                    graphColor = ContextCompat.getColor(this, R.color.symptoms_graph)
                }
            }
            for (stringDate in prefs.dateList) {
                val attributeData = prefs.readData(stringDate, thisCategory, attribute)
                if (attributeData != "ERROR: DATA NOT FOUND") {
                    val date: Date = prefs.convertDate(stringDate)
                    if ((maxDate == null) || (date > maxDate)) {
                        maxDate = date
                    }
                    if ((minDate == null) || (date < minDate)) {
                        minDate = date
                    }
                    var y = attributeData.toInt().toDouble()
                    //ignores negative y values
                    if ( y < 0 ) { y = 0.0 }
                    if (y > maxY) {maxY = y}
                    dataPoints.add(DataPoint(date, y))
                }
            }
            if (dataPoints.size != 0) {

                val series = LineGraphSeries(dataPoints.toTypedArray())
                series.color = graphColor
                series.isDrawDataPoints = true
                series.dataPointsRadius = 7F
                series.thickness = 6
                graph.addSeries(series)
            }
        }
        //this sets the minimum and maximum point of the x axis
        // & max for Y for the data found
        if ((minDate != null) && (maxDate != null))
        {
            val viewportLimit: Long = 5184000000 //x width in milliseconds
            if (maxDate.time - minDate.time > viewportLimit)
            {
                //if the graph gets too big, you can scroll & scale
                graph.viewport.setMinX((maxDate.time - viewportLimit).toDouble())
                graph.viewport.isScrollable = true
                graph.viewport.isScalable = true
            }
            else {
                graph.viewport.setMinX(minDate.time.toDouble())
                graph.viewport.isScrollable = false
                graph.viewport.isScalable = false
            }
            graph.viewport.setMaxX(maxDate.time.toDouble())
        }
        if (maxY > 10)
        {
            graph.viewport.setMaxY(maxY)
            graph.gridLabelRenderer.numVerticalLabels = (maxY+1).toInt()
        }
    }
}