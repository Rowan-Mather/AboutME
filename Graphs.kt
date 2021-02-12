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

        prefs.readDates()

        val graph: GraphView = findViewById(R.id.graph)
        graph.viewport.setMinY(0.0)
        graph.viewport.setMaxY(10.0)
        graph.gridLabelRenderer.isHighlightZeroLines = false
        graph.gridLabelRenderer.numVerticalLabels = 11
        graph.viewport.isYAxisBoundsManual = true
        graph.gridLabelRenderer.setHumanRounding(false)

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

        colourSwitches(selectedGraphs)
        drawGraphs(getArrayOfSelectedGraphs(selectedGraphs))

        /*
        spoonsSwitch.isChecked = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            spoonsSwitch.thumbDrawable.setColorFilter(
                Color.rgb(112, 200, 255),
                PorterDuff.Mode.MULTIPLY
            )
        }
        drawGraphs(getArrayOfSelectedGraphs(selectedGraphs))*/

        spoonsSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            delay(3000)
            selectedGraphs["spoons"] = isChecked
            writeSelectedGraphs(selectedGraphs)
            colourSwitches(selectedGraphs)
            drawGraphs(getArrayOfSelectedGraphs(selectedGraphs))
            reloadGraph()
        })

        hoursSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            delay(3000)
            selectedGraphs["hours"] = isChecked
            writeSelectedGraphs(selectedGraphs)
            colourSwitches(selectedGraphs)
            drawGraphs(getArrayOfSelectedGraphs(selectedGraphs))
            reloadGraph()
        })
    }

    fun delay(millis: Long)
    {
        /*
        val start : CountDownTimer(3000, 1000) {
            override fun onFinish() {
                // When timer is finished 
                // Execute your code here
            }
        }.start()*/
    }

    fun colourSwitches(selectedGraphs: HashMap<String, Boolean>)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            var colour = Color.BLACK

            colour = if (selectedGraphs["spoons"]!!)
            { Color.rgb(112, 200, 255) } else { Color.GRAY }
            spoonsSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)

            colour = if (selectedGraphs["hours"]!!)
            { Color.rgb(183, 125, 255) } else { Color.GRAY }
            hoursSwitch.thumbDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
            hoursSwitch.trackDrawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY)
        }
    }

    fun reloadGraph()
    {
        finish()
        startActivity(intent)
        overridePendingTransition(0,0)
    }

    fun writeSelectedGraphs(selectedGraphs: HashMap<String, Boolean>)
    {
        var selectedSummary = ""
        for (graph in arrayOf("spoons", "hours", "health", "fatigue")) {
            selectedSummary += if (selectedGraphs[graph] == true) "1" else "0"
        }
        prefs.writeSetting("SELECTEDGRAPHS", selectedSummary)
    }

    fun getArrayOfSelectedGraphs(selectedGraphs: HashMap<String, Boolean>) : Array<String>
    {
        var selected = mutableListOf<String>()
        for (graph in selectedGraphs.keys)
        {
            if (selectedGraphs[graph] == true) { selected.add(graph) }
        }
        return selected.toTypedArray()
    }

    fun drawGraphs(attributes: Array<String>)
    {
        val graph: GraphView = findViewById(R.id.graph)
        graph.removeAllSeries()
        var minDate: Date? = null
        var maxDate: Date? = null
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
        if ((minDate != null) && (maxDate != null))
        {
            graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(applicationContext)
            graph.viewport.setMinX(minDate.time.toDouble())
            graph.viewport.setMaxX(maxDate.time.toDouble())
            graph.viewport.isXAxisBoundsManual = true
        }
    }
}
