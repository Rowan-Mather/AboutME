package com.example.aboutme

import android.R.attr.checked
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
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

        var selectedGraphs = HashMap<String, Boolean>()
        selectedGraphs["spoons"] = true
        spoonsSwitch.isChecked = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            spoonsSwitch.thumbDrawable.setColorFilter(
                Color.rgb(112, 200, 255),
                PorterDuff.Mode.MULTIPLY
            )
        }
        drawGraphs(getArrayOfSelectedGraphs(selectedGraphs))

        spoonsSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                spoonsSwitch.thumbDrawable.setColorFilter(
                    if (isChecked) Color.rgb(112, 200, 255) else Color.GRAY,
                    PorterDuff.Mode.MULTIPLY
                )
            }
            selectedGraphs["spoons"] = isChecked
            drawGraphs(getArrayOfSelectedGraphs(selectedGraphs))
        })
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
        graph.viewport.setMinY(0.0)
        graph.viewport.setMaxY(10.0)
        graph.gridLabelRenderer.numVerticalLabels = 11
        graph.viewport.isYAxisBoundsManual = true
        graph.gridLabelRenderer.setHumanRounding(false)
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
            graph.viewport.setMinX(minDate.time.toDouble())
            graph.viewport.setMaxX(maxDate.time.toDouble())
            graph.viewport.isXAxisBoundsManual = true
            graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(applicationContext)
        }
    }
}
