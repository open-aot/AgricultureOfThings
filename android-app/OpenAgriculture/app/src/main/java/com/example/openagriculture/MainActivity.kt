package com.example.openagriculture

import androidx.lifecycle.MutableLiveData
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.Menu

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.util.Log
import android.content.Intent
import android.graphics.Color
import android.os.Parcelable
import androidx.navigation.NavType
import androidx.navigation.NavType.ParcelableArrayType
import com.github.mikephil.charting.animation.Easing

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val MATERIAL_COLORS = intArrayOf(
        ColorTemplate.rgb("#2ecc71"),
        ColorTemplate.rgb("#f1c40f"),
        ColorTemplate.rgb("#e74c3c"),
        ColorTemplate.rgb("#3498db")
    )

    // The internal MutableLiveData String that stores the status of the most recent request
    private val _response = MutableLiveData<String>()
    private val data = mutableListOf<OAData>()
    private val data_week = mutableListOf<OAData>()
        get() = field
    private lateinit var mChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Idea", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val drawerLayout: androidx.drawerlayout.widget.DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        mChart = findViewById(R.id.main_chart)

        getOpenAgricultureData("home")
    }

    override fun onBackPressed() {
        val drawerLayout: androidx.drawerlayout.widget.DrawerLayout = findViewById(R.id.drawer_layout)

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                getOpenAgricultureData("home")
            }
            R.id.nav_temperature -> {
                getOpenAgricultureData("temp")
            }
            R.id.nav_humidity -> {
                getOpenAgricultureData("hum")
            }
            R.id.nav_moisture -> {
                getOpenAgricultureData("mois")
            }
        }

        val drawerLayout: androidx.drawerlayout.widget.DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    private fun renderHomeData() {
        val xAxis: XAxis = mChart.xAxis
        xAxis.isEnabled = true
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.enableGridDashedLine(10f, 10f, 0f)
        xAxis.setDrawLimitLinesBehindData(true)

        val yAxis: YAxis = mChart.axisLeft
        yAxis.setAxisMaxValue(105f)
        yAxis.setAxisMinValue(0f)

        mChart.setDescription("")
        mChart.setTouchEnabled(true)
        mChart.setPinchZoom(true)
        mChart.setTouchEnabled(true)
        mChart.isDragEnabled = true
        mChart.setScaleEnabled(true)
        mChart.setPinchZoom(false)
        mChart.axisRight.isEnabled = false
        mChart.legend.isEnabled = false
        mChart.axisLeft.setDrawGridLines(false)
        mChart.xAxis.setDrawGridLines(false)

        mChart.xAxis.setDrawGridLines(true)
        mChart.axisLeft.setDrawGridLines(true)
        mChart.axisRight.setDrawGridLines(true)

        val legend = mChart.legend
        legend.isEnabled = true
        legend.form = Legend.LegendForm.CIRCLE
        legend.textColor = Color.BLACK
        legend.position = Legend.LegendPosition.ABOVE_CHART_RIGHT

        setHomeData()
    }

    private fun setHomeData() {
        val values1 = ArrayList<Entry>()
        val values2 = ArrayList<Entry>()
        val values3 = ArrayList<Entry>()
        val hours: ArrayList<String> = ArrayList()
        val size = data.size - 1

        for (i in 0..size) {
//            hours.add(i, data[i].Timestamp.split(" ")[3].substring(0, 5))
            hours.add(i, data[i].Timestamp.substring(0, 16))
            values1.add(Entry(data[i].Temperature.toFloat(), i))
            values2.add(Entry(data[i].Humidity.toFloat(), i))
            values3.add(Entry(data[i].Moisture.toFloat(), i))
//            values1.add(Entry(((Math.random() * 70) + 30).toFloat(), i))
//            values2.add(Entry(((Math.random() * 70) + 30).toFloat(), i))
//            values3.add(Entry(((Math.random() * 70) + 30).toFloat(), i))
        }

        val set0 = LineDataSet(values1, "Temperature")
        val set1 = LineDataSet(values2, "Humidity")
        val set2 = LineDataSet(values3, "Moisture")

        set0.setDrawCubic(true)
        set1.setDrawCubic(true)
        set2.setDrawCubic(true)

//        set0.color = MATERIAL_COLORS[2]
//        set1.color = MATERIAL_COLORS[0]
//        set2.color = MATERIAL_COLORS[3]

        set0.color = MATERIAL_COLORS[2]
        set1.color = MATERIAL_COLORS[1]
        set2.color = MATERIAL_COLORS[3]

        set0.lineWidth = 3f
        set0.valueTextSize = 9f
        set0.setDrawValues(false)
        set0.setDrawCircles(false)

        set1.lineWidth = 3f
        set1.valueTextSize = 9f
        set1.setDrawValues(false)
        set1.setDrawCircles(false)

        set2.lineWidth = 3f
        set2.valueTextSize = 9f
        set2.setDrawValues(false)
        set2.setDrawCircles(false)

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set0)
        dataSets.add(set1)
        dataSets.add(set2)

        val data = LineData(hours, dataSets)
        mChart.data = data

        mChart.animateX(420, Easing.EasingOption.Linear)
        mChart.invalidate()
    }

    private fun getOpenAgricultureData(mod: String) {
        OApi.retrofitService.getProperties().enqueue(object: Callback<List<OAData>> {
            /**
             * Invoked when a network exception occurred talking to the server or when an unexpected
             * exception occurred creating the request or processing the response.
             */
            override fun onFailure(call: Call<List<OAData>>, t: Throwable) {
                _response.value = "Failure: " + t.message
                Log.v("F", t.message)
            }

            /**
             * Invoked for a received HTTP response.
             */
            override fun onResponse(call: Call<List<OAData>>, response: Response<List<OAData>>) {
                if (response.isSuccessful) {
                    _response.value = "Success: ${response.body()?.size} Mars properties retrieved"

                    data.clear()
                    data.addAll(ArrayList(response.body()))

                    for(d in data) {
                        if (d.Moisture > 100.0)
                            d.Moisture = 100.0
                    }

                    graphs(mod)
                } else {
                    Log.e("F", "Error response, no access to resource?")
                }
            }
        })

        OApi.retrofitService.getPropertiesWeek().enqueue(object: Callback<List<OAData>> {
            override fun onFailure(call: Call<List<OAData>>, t: Throwable) {
                _response.value = "Failure: " + t.message
            }

            override fun onResponse(call: Call<List<OAData>>, response: Response<List<OAData>>) {
                if (response.isSuccessful) {
                    data_week.clear()
                    data_week.addAll(ArrayList(response.body()))

                    for(d in data_week) {
                        if (d.Moisture > 100.0)
                            d.Moisture = 100.0
                    }
                }
            }
        })
    }

    private fun graphs(mod: String) {
        val i: Intent
        val dw: ArrayList<OAData> = data_week as ArrayList<OAData>

        when (mod) {
            "home" -> renderHomeData()
            "temp" -> {
                i = Intent(this, TemperatureActivity::class.java)
                i.putExtra("week", dw)
                startActivity(i)
            }
            "hum" -> {
                i = Intent(this, HumidityActivity::class.java)
                startActivity(i)
            }
            "mois" -> {
                i = Intent(this, MoistureActivity::class.java)
                startActivity(i)
            }
        }
    }

}
