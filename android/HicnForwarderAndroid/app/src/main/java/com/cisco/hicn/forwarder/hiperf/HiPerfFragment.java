/*
 * Copyright (c) 2019 Cisco and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cisco.hicn.forwarder.hiperf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cisco.hicn.forwarder.HomeActivity;
import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.AndroidUtility;
import com.cisco.hicn.forwarder.supportlibrary.Hiperf;
import com.cisco.hicn.forwarder.utility.Constants;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;


public class HiPerfFragment extends Fragment {

    private LineData aaa;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private String TAG = Constants.HIPERF;
    //UI elements

    private static Context context;
    private static View root;


    private EditText hiperfHicnNameEditText = null;
    private Button hiperfStartButton = null;
    private Button hiperfStopButton = null;
    private LineChart hiperfTimeLineChart = null;

    //Preferences
    private SharedPreferences hiperfSharedPreferences;

    //thread
    private Timer hiperfTime = null;
    public static Queue<Integer> hiperfGraphQueue = new LinkedList<>();
    private Timer hiperfGraphTimer = null;
    static ArrayBlockingQueue<String> logBlockingQueue = new ArrayBlockingQueue<>(1000);

    //UI behavior
    private boolean hiperfRunning = false;

    private ArrayList<Entry> hiperfTimeArrayList = new ArrayList<>();
    private ArrayList<Integer> hiperfTimeIntArrayList = new ArrayList<>();
    private ArrayList<Long> hiperfTimestampArrayList = new ArrayList<>();
    private long hiperfStartGraph = 0;
    private HomeActivity home;

    public static HiPerfFragment newInstance(int index) {
        HiPerfFragment fragment = new HiPerfFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        context = getActivity();
        hiperfSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        root = inflater.inflate(R.layout.fragment_hiperf, container, false);
        initHiPerfFragment(root);
        return root;
    }

    private void initHiPerfFragment(View root) {
        //Get components by IDs

        AndroidUtility.getInstance().setHiperfGraphQueue(hiperfGraphQueue);
        hiperfHicnNameEditText = root.findViewById(R.id.hiperf_hicn_name_edittext);

        hiperfStartButton = root.findViewById(R.id.hiperf_start_button);
        hiperfStopButton = root.findViewById(R.id.hiperf_stop_button);
        hiperfStopButton.setEnabled(false);


        hiperfHicnNameEditText.setText(hiperfSharedPreferences.getString(getString(R.string.hiperf_hicn_name_key), getString(R.string.default_hiperf_hicn_name)));

        if (hiperfRunning) {
            hiperfStartButton.setEnabled(false);
            hiperfHicnNameEditText.setEnabled(false);
            hiperfStopButton.setEnabled(true);
        } else {

            hiperfStartButton.setEnabled(true);
            hiperfStopButton.setEnabled(false);
            hiperfHicnNameEditText.setEnabled(true);
        }


        hiperfStartButton.setOnClickListener(view -> {
            hiperfRunning = true;

            hiperfHicnNameEditText.setEnabled(false);
            hiperfStartButton.setEnabled(false);
            hiperfStopButton.setEnabled(true);
            SharedPreferences.Editor hiperfEditor = hiperfSharedPreferences.edit();
            hiperfEditor.putString(getString(R.string.hiperf_hicn_name_key), hiperfHicnNameEditText.getText().toString());
            hiperfEditor.commit();
            hiperfRunning = true;
            hiperfTime = new Timer();
            hiperfStartGraph = System.currentTimeMillis() / 1000;
            hiperfGraphQueue.clear();

            hiperfTime.schedule(new TimerTask() {
                @Override
                public void run() {
                    setHiperfTimeLineChartData(0, 0, true);

                    hiperfGraphTimer = new Timer();

                    hiperfGraphTimer.scheduleAtFixedRate(new TimerTask() {
                        public void run() {

                            if (hiperfGraphQueue.size() > 0) {
                                int bandwidth = hiperfGraphQueue.poll();
                                setHiperfTimeLineChartData(System.currentTimeMillis(), bandwidth, false);
                            } else {
                                setHiperfTimeLineChartData(System.currentTimeMillis(), 0, false);
                            }

                            if (isAdded() && isVisible()) {
                                getActivity().runOnUiThread(() -> {
                                            hiperfTimeLineChart.invalidate();
                                        }
                                );
                            }

                        }
                    }, 0, 1000);

                    SharedPreferences.Editor hiperfEditor = hiperfSharedPreferences.edit();
                    hiperfEditor.putString(getString(R.string.hiperf_hicn_name_key), hiperfHicnNameEditText.getText().toString());

                    hiperfEditor.commit();

                    Hiperf hiperf = Hiperf.getInstance();

                    float raaqmBetaParameter = hiperfSharedPreferences.getFloat(getString(R.string.hiperf_raaqm_beta_parameter_key), Float.parseFloat(getString(R.string.default_hiperf_raaqm_beta)));

                    float raaqmDropFactorParameter = Float.valueOf(hiperfSharedPreferences.getString(getString(R.string.hiperf_raaqm_drop_factor_key), getString(R.string.default_hiperf_raaqm_drop_factor)));

                    long interestLifetimeParameter = Long.valueOf(hiperfSharedPreferences.getString(getString(R.string.hiperf_interest_lifetime_key), getString(R.string.default_hiperf_interest_lifetime)));
                    int windowSize = -1;

                    if (hiperfSharedPreferences.getBoolean(getString(R.string.enable_hiperf_window_size_key), false)) {
                        String ccc = hiperfSharedPreferences.getString(getString(R.string.hiperf_window_size_key), getString(R.string.default_hiperf_window_size));
                        windowSize = Integer.valueOf(hiperfSharedPreferences.getString(getString(R.string.hiperf_window_size_key), getString(R.string.default_hiperf_window_size)));
                    }

                    boolean enableRtcProtocol = hiperfSharedPreferences.getBoolean(getString(R.string.enable_hiperf_rtc_protocol_key), false);

                    hiperf.startHiPerf(
                            hiperfHicnNameEditText.getText().toString(), raaqmBetaParameter, raaqmDropFactorParameter, windowSize, 1000, enableRtcProtocol, interestLifetimeParameter);
                    if (isAdded() && isVisible()) {
                        getActivity().runOnUiThread(() -> {
                                    hiperfHicnNameEditText.setEnabled(true);
                                    hiperfStartButton.setEnabled(true);
                                    hiperfStopButton.setEnabled(false);
                                }
                        );
                    }
                    hiperfRunning = false;
                }
            }, 0);

        });

        hiperfStopButton.setOnClickListener(view -> {

            Hiperf hiperf = Hiperf.getInstance();
            hiperf.stopHiPerf();
            hiperfTime.cancel();
            hiperfGraphTimer.cancel();
            hiperfRunning = false;
        });


        hiperfTimeLineChart = root.findViewById(R.id.hiperf_time_linechart);
        if (hiperfTimeArrayList.size() == 0)
            setHiperfTimeLineChartData(0, 0, true);
        else
            recoverHiperfTimeLineChartData();
        hiperfTimeLineChart.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        hiperfTimeLineChart.getLegend().setEnabled(false);
        hiperfTimeLineChart.setBackgroundColor(Color.WHITE);
        hiperfTimeLineChart.getDescription().setEnabled(false);
        hiperfTimeLineChart.setTouchEnabled(true);
        hiperfTimeLineChart.setDrawGridBackground(false);
        hiperfTimeLineChart.setDragEnabled(true);
        hiperfTimeLineChart.setScaleEnabled(true);
        hiperfTimeLineChart.setPinchZoom(true);


        XAxis xAxis = hiperfTimeLineChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setDrawLabels(false);

        YAxis yAxis = hiperfTimeLineChart.getAxisLeft();
        hiperfTimeLineChart.getAxisRight().setEnabled(false);
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        yAxis.setAxisMaximum(100f);


        // // Create Limit Lines // //
        LimitLine llXAxis = new LimitLine(9f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        yAxis.setDrawLimitLinesBehindData(true);
        xAxis.setDrawLimitLinesBehindData(true);
    }

    private void recoverHiperfTimeLineChartData() {
        XAxis xAxis = hiperfTimeLineChart.getXAxis();
        YAxis yAxis = hiperfTimeLineChart.getAxisLeft();
        long maxValue = (int) hiperfTimeArrayList.get(hiperfTimeArrayList.size() - 1).getY();

        yAxis.enableGridDashedLine(10f, 10f, 0f);
        yAxis.setAxisMaximum(maxValue + (int) (maxValue * 0.10) + 5);
        yAxis.setAxisMinimum(-5);


        // vertical grid lines
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        xAxis.setAxisMinimum(hiperfTimeArrayList.get(hiperfTimeArrayList.size() - 1).getX() - 30);
        xAxis.setAxisMaximum(hiperfTimeArrayList.get(hiperfTimeArrayList.size() - 1).getX());

        LineDataSet set1 = new LineDataSet(hiperfTimeArrayList, "HiPerf");


        set1.setDrawIcons(false);

        // draw dashed line
        set1.enableDashedLine(10f, 5f, 0f);

        // black lines and points
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);

        // line thickness and point size
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);

        // draw points as solid circles
        set1.setDrawCircleHole(false);

        // customize legend entry
        set1.setFormLineWidth(1f);
        set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);

        // text size of values
        set1.setValueTextSize(9f);

        // draw selection line as dashed
        set1.enableDashedHighlightLine(10f, 5f, 0f);

        // set the filled area
        set1.setDrawFilled(true);
        set1.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return hiperfTimeLineChart.getAxisLeft().getAxisMinimum();
            }
        });

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets
        LineData data = new LineData(dataSets);
        hiperfTimeLineChart.setData(data);
        set1.setValues(hiperfTimeArrayList);
        set1.notifyDataSetChanged();
        hiperfTimeLineChart.getData().notifyDataChanged();
        hiperfTimeLineChart.notifyDataSetChanged();

    }

    private void setHiperfTimeLineChartData(long timestamp, int value, boolean init) {
        if (init) {
            hiperfTimeArrayList.clear();
            hiperfTimeIntArrayList.clear();
            hiperfTimestampArrayList.clear();
        }

        int countOldValues = 0;

        for (int i = 0; i < hiperfTimestampArrayList.size(); i++) {
            if ((timestamp / 1000 - hiperfStartGraph) - hiperfTimestampArrayList.get(i) > Constants.MAX_HIPERF_TIME_LINECHART_XAXIS) {
                countOldValues++;
            }
            break;
        }

        for (int i = 0; i < countOldValues; i++) {
            hiperfTimeArrayList.remove(0);
            hiperfTimeIntArrayList.remove(0);
            hiperfTimestampArrayList.remove(0);
        }

        Log.d(TAG, "hiperfTimeIntArrayList size: " + hiperfTimeIntArrayList.size());

        if (!init) {
            XAxis xAxis = hiperfTimeLineChart.getXAxis();
            hiperfTimeArrayList.add(new Entry(timestamp / 1000 - hiperfStartGraph, value));//, getResources().getDrawable(R.drawable.star)));
            hiperfTimeIntArrayList.add(value);
            hiperfTimestampArrayList.add(timestamp / 1000 - hiperfStartGraph);

            YAxis yAxis = hiperfTimeLineChart.getAxisLeft();
            long maxValue = Collections.max(hiperfTimeIntArrayList);

            yAxis.enableGridDashedLine(10f, 10f, 0f);
            yAxis.setAxisMaximum(maxValue + (int) (maxValue * 0.10) + 5);
            yAxis.setAxisMinimum(-5);

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setAxisMinimum(timestamp / 1000 - hiperfStartGraph - 30);
            xAxis.setAxisMaximum(timestamp / 1000 - hiperfStartGraph);

        } else {
            XAxis xAxis = hiperfTimeLineChart.getXAxis();
            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);

            xAxis.setAxisMinimum(-30);
            xAxis.setAxisMaximum(0);


        }

        LineDataSet set1;

        if (hiperfTimeLineChart.getData() != null &&
                hiperfTimeLineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) hiperfTimeLineChart.getData().getDataSetByIndex(0);
            set1.setValues(hiperfTimeArrayList);
            set1.notifyDataSetChanged();
            hiperfTimeLineChart.getData().notifyDataChanged();
            hiperfTimeLineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(hiperfTimeArrayList, "HiPerf");


            set1.setDrawIcons(false);

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);

            // line thickness and point size
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            set1.setDrawFilled(true);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return hiperfTimeLineChart.getAxisLeft().getAxisMinimum();
                }
            });

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets
            LineData data = new LineData(dataSets);
            hiperfTimeLineChart.setData(data);
        }


    }

    public void setHome(HomeActivity home) {
        this.home = home;
    }
}