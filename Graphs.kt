package com.example.aboutme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.util.*
import kotlin.math.max


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

        val graph: GraphView = findViewById(R.id.graph)

        prefs.readDates()

        //2021-01-22

        var myDataPoints: MutableList<DataPoint> = mutableListOf()
        var minDate: Date? = null
        var maxDate: Date? = null
        var minSet = false
        for (stringDate in prefs.dateList)
        {
            var spoons = prefs.readData(stringDate, "activity", "spoons")
            if (spoons != "ERROR: DATA NOT FOUND")
            {
                val dateSplit = stringDate.split("-")
                val date: Date = Date(dateSplit[0].toInt()-1900, dateSplit[1].toInt()-1, dateSplit[2].toInt())
                maxDate = date
                if (!minSet)
                {
                    minDate = date
                    minSet = true
                }
                myDataPoints.add(DataPoint(date, spoons.toInt().toDouble()))
            }
        }

        if ((minDate != null) && (maxDate != null)){
            val series = LineGraphSeries(myDataPoints.toTypedArray())

            graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(applicationContext)
            graph.viewport.setMinX(minDate.time.toDouble())
            graph.viewport.setMaxX(maxDate.time.toDouble())
            graph.viewport.isXAxisBoundsManual = true

            graph.viewport.setMinY(0.0)
            graph.viewport.setMaxY(10.0)
            graph.gridLabelRenderer.numVerticalLabels = 11
            graph.viewport.isYAxisBoundsManual = true

            graph.gridLabelRenderer.setHumanRounding(false)

            series.color = Color.rgb(112, 200, 255)
            series.isDrawDataPoints = true;
            series.setDataPointsRadius(7F)
            series.setThickness(6)
            //series.setDataPointsRadius(10);
            graph.addSeries(series)
        }
    }
}
