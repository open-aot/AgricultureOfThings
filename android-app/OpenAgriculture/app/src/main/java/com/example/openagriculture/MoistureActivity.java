package com.example.openagriculture;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.example.openagriculture.listviewitems.BarChartItem;
import com.example.openagriculture.listviewitems.ChartItem;
import com.example.openagriculture.listviewitems.LineChartItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressLint("Registered")
public class MoistureActivity extends AppCompatActivity {
    public static final int[] MATERIAL_COLORS = {
            ColorTemplate.rgb("#2ecc71"), ColorTemplate.rgb("#f1c40f"), ColorTemplate.rgb("#e74c3c"), ColorTemplate.rgb("#3498db")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_moisture);

        setTitle("Moisture");

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ListView lv = findViewById(R.id.listView);

        ArrayList<ChartItem> list = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            if (i % 3 == 0) {
                list.add(new LineChartItem(generateDataLine(i + 1), getApplicationContext()));
            } else if(i % 3 == 1) {
                list.add(new BarChartItem(generateDataBar(i + 1), getApplicationContext()));
            }
        }

        ChartDataAdapter cda = new ChartDataAdapter(getApplicationContext(), list);
        lv.setAdapter(cda);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /** adapter that supports 3 different item types */
    private class ChartDataAdapter extends ArrayAdapter<ChartItem> {

        ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            //noinspection ConstantConditions
            return getItem(position).getView(position, convertView, getContext());
        }

        @Override
        public int getItemViewType(int position) {
            // return the views type
            ChartItem ci = getItem(position);
            return ci != null ? ci.getItemType() : 0;
        }

        @Override
        public int getViewTypeCount() {
            return 3; // we have 3 different item-types
        }
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Line data
     */
    private LineData generateDataLine(int cnt) {
        ArrayList<Entry> values = new ArrayList<>();
        String[] hours = {"00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00"};

        for (int i = 0; i < 7; i++) {
            values.add(new Entry((int) (Math.random() * 70) + 30, i));
        }

        LineDataSet d = new LineDataSet(values, "Moisture (°C)");
        d.setDrawCubic(true);
        d.setDrawCircles(false);
        d.setDrawValues(false);
        d.setLineWidth(5f);
        d.setHighLightColor(Color.rgb(244, 117, 117));
        d.setColor(MATERIAL_COLORS[3]);
        d.setCircleColor(MATERIAL_COLORS[3]);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(d);

        return new LineData(hours, sets);
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Bar data
     */
    private BarData generateDataBar(int cnt) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry((int) (Math.random() * 70) + 30, i));
        }

        BarDataSet d = new BarDataSet(entries, "Avg. Moisture (%)");
        d.setColors(Collections.singletonList(MATERIAL_COLORS[3]));
        d.setBarSpacePercent(20f);

        ArrayList<IBarDataSet> sets = new ArrayList<>();
        sets.add(d);

        return new BarData(days, sets);
    }
}
