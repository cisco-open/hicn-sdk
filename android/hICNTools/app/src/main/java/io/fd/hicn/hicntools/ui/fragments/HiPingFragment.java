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

package io.fd.hicn.hicntools.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

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
import com.github.mikephil.charting.utils.Utils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import io.fd.hicn.hicntools.MainActivity;
import io.fd.hicn.hicntools.R;
import io.fd.hicn.hicntools.service.Constants;

public class HiPingFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private String TAG = Constants.HIPING;
    int i = 0;
    //UI elements
    private EditText hipingHicnNameEditText = null;
    private TextView hipingExpandTextView = null;
    private LinearLayout hipingExpandLinearView = null;
    private ExpandableLayout hipingExpandableLayout = null;
    private EditText hipingSourcePortEditText = null;
    private EditText hipingDestinationPortEditText = null;
    private EditText hipingTtlEditText = null;
    private EditText hipingPingIntervalEditText = null;
    private EditText hipingMaxPingsEditText = null;
    private EditText hipingLifetimeEditText = null;
    private CheckBox hipingOpenTcpConnectionCheckBox = null;
    private CheckBox hipingSendSynMessageCheckBox = null;
    private CheckBox hipingSendAckMessageCheckBox = null;
    private Button hipingStartButton = null;
    private Button hipingStopButton = null;
    private LineChart hipingTimeLineChart = null;
    private NestedScrollView hipingLogScrollView = null;
    private EditText hipingLogEditText = null;
    private CheckBox hipingLogAutoscrollCheckBox = null;
    private Button hipingResetLogButton;

    //Preferences
    private SharedPreferences hipingSharedPreferences;

    //thread
    private Timer hipingTime = null;

    //UI behavior
    private boolean expandadbleLayoutExpanded = false;
    private boolean hipingRunning = false;
    private boolean autoScrollEnabled = true;
    private ArrayList<Entry> hipingTimeArrayList = new ArrayList<>();
    private ArrayList<Integer> hipingTimeIntArrayList = new ArrayList<>();
    private ArrayList<Long> hipintTimestampArrayList = new ArrayList<>();
    private long hipingStartGraph = 0;
    // private int index = 0;

    public static HiPingFragment newInstance(int index) {
        HiPingFragment fragment = new HiPingFragment();
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

        Context context = getActivity();
        hipingSharedPreferences = context.getSharedPreferences(
                getString(R.string.hiping_shared_preferences), Context.MODE_PRIVATE);
        View root = inflater.inflate(R.layout.fragment_hiping, container, false);
        initHiPingFragment(root);
        return root;
    }

    private void initHiPingFragment(View root) {
        //Get components by IDs

        hipingHicnNameEditText = root.findViewById(R.id.hiping_hicn_name_edittext);
        hipingExpandTextView = root.findViewById(R.id.hiping_expand_textview);
        hipingExpandLinearView = root.findViewById(R.id.hiping_expand_linearview);
        hipingExpandableLayout = root.findViewById(R.id.hiping_expandable_layout);
        hipingSourcePortEditText = root.findViewById(R.id.hiping_source_port_edittext);
        hipingDestinationPortEditText = root.findViewById(R.id.hiping_destination_port_edittext);
        hipingTtlEditText = root.findViewById(R.id.hiping_ttl_edittext);
        hipingPingIntervalEditText = root.findViewById(R.id.hiping_ping_interval_edittext);
        hipingMaxPingsEditText = root.findViewById(R.id.hiping_max_pings_edittext);
        hipingLifetimeEditText = root.findViewById(R.id.hiping_lifetime_edittext);
        hipingOpenTcpConnectionCheckBox = root.findViewById(R.id.hiping_open_tcp_connection_checkbox);
        hipingSendSynMessageCheckBox = root.findViewById(R.id.hiping_send_syn_message_checkbox);
        hipingSendAckMessageCheckBox = root.findViewById(R.id.hiping_send_ack_message_checkbox);
        hipingStartButton = root.findViewById(R.id.hiping_start_button);
        hipingStopButton = root.findViewById(R.id.hiping_stop_button);
        hipingTimeLineChart = root.findViewById(R.id.hiping_time_linechart);
        hipingLogScrollView = root.findViewById(R.id.hiping_log_scrollview);
        hipingLogEditText = root.findViewById(R.id.hiping_log_edittext);
        hipingLogAutoscrollCheckBox = root.findViewById(R.id.hiping_log_autoscroll_checkbox);
        hipingResetLogButton = root.findViewById(R.id.hiping_reset_log_button);


        hipingHicnNameEditText.setText(hipingSharedPreferences.getString(getString(R.string.hiping_hicn_name_key), Constants.DEFAULT_HIPING_HICN_NAME));
        hipingSourcePortEditText.setText(hipingSharedPreferences.getString(getString(R.string.hiping_source_port_key), Integer.toString(Constants.DEFAULT_HIPING_SOURCE_PORT)));
        hipingDestinationPortEditText.setText(hipingSharedPreferences.getString(getString(R.string.hiping_destination_port_key), Integer.toString(Constants.DEFAULT_HIPING_DESTINATION_PORT)));
        hipingTtlEditText.setText(hipingSharedPreferences.getString(getString(R.string.hiping_ttl_key), Integer.toString(Constants.DEFAULT_HIPING_TTL)));
        hipingPingIntervalEditText.setText(hipingSharedPreferences.getString(getString(R.string.hiping_ping_interval_key), Integer.toString(Constants.DEFAULT_HIPING_PING_INTERVAL)));
        hipingMaxPingsEditText.setText(hipingSharedPreferences.getString(getString(R.string.hiping_max_pings_key), Integer.toString(Constants.DEFAULT_HIPING_MAX_PINGS)));
        hipingLifetimeEditText.setText(hipingSharedPreferences.getString(getString(R.string.hiping_lifetime_key), Integer.toString(Constants.DEFAULT_HIPING_LIFETIME)));
        hipingOpenTcpConnectionCheckBox.setChecked(hipingSharedPreferences.getBoolean(getString(R.string.hiping_open_tcp_connection_key), Constants.DEFAULT_HIPING_OPEN_TCP_CONNECTION));
        hipingSendSynMessageCheckBox.setChecked(hipingSharedPreferences.getBoolean(getString(R.string.hiping_send_syn_message_key), Constants.DEFAULT_HIPING_SEND_SYN_MESSAGE));
        hipingSendAckMessageCheckBox.setChecked(hipingSharedPreferences.getBoolean(getString(R.string.hiping_send_ack_message_key), Constants.DEFAULT_HIPING_SEND_ACK_MESSAGE));


        View.OnClickListener hipingExpandOnClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                float deg = (hipingExpandLinearView.getRotation() == 90F) ? 0F : 90F;
                if (expandadbleLayoutExpanded) {
                    hipingExpandTextView.setText(R.string.hiping_more_options);
                    hipingExpandableLayout.collapse();
                } else {
                    hipingExpandableLayout.expand();
                    hipingExpandTextView.setText(R.string.hiping_hide_options);
                }
                expandadbleLayoutExpanded = !expandadbleLayoutExpanded;
                hipingExpandLinearView.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());

            }
        };

        hipingExpandTextView.setOnClickListener(hipingExpandOnClickListener);
        hipingExpandLinearView.setOnClickListener(hipingExpandOnClickListener);

        hipingOpenTcpConnectionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hipingSendSynMessageCheckBox.setChecked(false);
                    hipingSendAckMessageCheckBox.setChecked(false);
                }
            }
        });

        hipingSendSynMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hipingOpenTcpConnectionCheckBox.setChecked(false);
                    hipingSendAckMessageCheckBox.setChecked(false);
                }
            }
        });

        hipingSendAckMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hipingOpenTcpConnectionCheckBox.setChecked(false);
                    hipingSendSynMessageCheckBox.setChecked(false);
                }
            }
        });

        hipingStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hipingHicnNameEditText.setEnabled(false);
                hipingExpandTextView.setEnabled(false);
                hipingExpandableLayout.setEnabled(false);
                hipingSourcePortEditText.setEnabled(false);
                hipingDestinationPortEditText.setEnabled(false);
                hipingTtlEditText.setEnabled(false);
                hipingPingIntervalEditText.setEnabled(false);
                hipingMaxPingsEditText.setEnabled(false);
                hipingLifetimeEditText.setEnabled(false);
                hipingOpenTcpConnectionCheckBox.setEnabled(false);
                hipingSendSynMessageCheckBox.setEnabled(false);
                hipingSendAckMessageCheckBox.setEnabled(false);
                hipingStartButton.setEnabled(false);
                hipingStopButton.setEnabled(true);
                SharedPreferences.Editor hipingEditor = hipingSharedPreferences.edit();
                hipingEditor.putString(getString(R.string.hiping_hicn_name_key), hipingHicnNameEditText.getText().toString());
                hipingEditor.putString(getString(R.string.hiping_source_port_key), hipingSourcePortEditText.getText().toString());
                hipingEditor.putString(getString(R.string.hiping_destination_port_key), hipingDestinationPortEditText.getText().toString());
                hipingEditor.putString(getString(R.string.hiping_ttl_key), hipingTtlEditText.getText().toString());
                hipingEditor.putString(getString(R.string.hiping_ping_interval_key), hipingPingIntervalEditText.getText().toString());
                hipingEditor.putString(getString(R.string.hiping_max_pings_key), hipingMaxPingsEditText.getText().toString());
                hipingEditor.putString(getString(R.string.hiping_lifetime_key), hipingLifetimeEditText.getText().toString());
                hipingEditor.putBoolean(getString(R.string.hiping_open_tcp_connection_key), hipingOpenTcpConnectionCheckBox.isChecked());
                hipingEditor.putBoolean(getString(R.string.hiping_send_syn_message_key), hipingSendSynMessageCheckBox.isChecked());
                hipingEditor.putBoolean(getString(R.string.hiping_send_ack_message_key), hipingSendAckMessageCheckBox.isChecked());

                hipingEditor.commit();
                hipingRunning = true;
                /*This schedules a runnable task every second*/
                hipingTime = new Timer();
                hipingStartGraph = System.currentTimeMillis() / 1000;
                hipingTime.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("ciao", "ciao");
                        setHipingTimeLineChartData(0, 0, true);

                        ((MainActivity) getActivity()).setDisableSwipe(1);
                        startPing(hipingHicnNameEditText.getText().toString(),
                                Short.parseShort(hipingSourcePortEditText.getText().toString()),
                                Short.parseShort(hipingDestinationPortEditText.getText().toString()),
                                Long.parseLong(hipingTtlEditText.getText().toString()),
                                Long.parseLong(hipingPingIntervalEditText.getText().toString()),
                                Long.parseLong(hipingMaxPingsEditText.getText().toString()),
                                Long.parseLong(hipingLifetimeEditText.getText().toString()),
                                hipingOpenTcpConnectionCheckBox.isChecked(),
                                hipingSendSynMessageCheckBox.isChecked(),
                                hipingSendAckMessageCheckBox.isChecked());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hipingHicnNameEditText.setEnabled(true);
                                hipingExpandTextView.setEnabled(true);
                                hipingSourcePortEditText.setEnabled(true);
                                hipingDestinationPortEditText.setEnabled(true);
                                hipingTtlEditText.setEnabled(true);
                                hipingPingIntervalEditText.setEnabled(true);
                                hipingMaxPingsEditText.setEnabled(true);
                                hipingLifetimeEditText.setEnabled(true);
                                hipingOpenTcpConnectionCheckBox.setEnabled(true);
                                hipingSendSynMessageCheckBox.setEnabled(true);
                                hipingSendAckMessageCheckBox.setEnabled(true);
                                hipingStartButton.setEnabled(true);
                                hipingStopButton.setEnabled(false);

                            }
                        });
                        ((MainActivity) getActivity()).enableSwipe();


                        hipingRunning = false;
                    }
                }, 0);
            }
        });

        hipingStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopPing();
                hipingTime.cancel();
            }
        });

        hipingTimeLineChart.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        hipingTimeLineChart.getLegend().setEnabled(false);
        hipingTimeLineChart.setBackgroundColor(Color.WHITE);
        hipingTimeLineChart.getDescription().setEnabled(false);
        hipingTimeLineChart.setTouchEnabled(true);
        hipingTimeLineChart.setDrawGridBackground(false);
        hipingTimeLineChart.setDragEnabled(true);
        hipingTimeLineChart.setScaleEnabled(true);
        hipingTimeLineChart.setPinchZoom(true);


        XAxis xAxis = hipingTimeLineChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setDrawLabels(false);

        YAxis yAxis = hipingTimeLineChart.getAxisLeft();
        hipingTimeLineChart.getAxisRight().setEnabled(false);
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setAxisMinimum(0);


        // // Create Limit Lines // //
        LimitLine llXAxis = new LimitLine(9f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        yAxis.setDrawLimitLinesBehindData(true);
        xAxis.setDrawLimitLinesBehindData(true);
        hipingTimeLineChart.animateX(1500);
        setHipingTimeLineChartData(0, 0, true);

        hipingLogEditText.setKeyListener(null);
        hipingLogAutoscrollCheckBox.setChecked(hipingSharedPreferences.getBoolean(getString(R.string.hiping_log_autosroll_key), Constants.DEFAULT_HIPING_AUTOSCROLL));

        hipingLogAutoscrollCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    autoScrollEnabled = true;
                } else {
                    autoScrollEnabled = false;
                }

                SharedPreferences.Editor hipingEditor = hipingSharedPreferences.edit();
                hipingEditor.putBoolean(getString(R.string.hiping_log_autosroll_key), isChecked);
                hipingEditor.commit();
            }
        });

        hipingResetLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hipingLogEditText.setText("");
            }
        });

    }

    public void hipingUpdateGraphCallback(int rtt) {
        setHipingTimeLineChartData(System.currentTimeMillis(), rtt, false);
        hipingTimeLineChart.invalidate();
    }


    public void hipingLogCallback(String logString) {
        final String log = logString;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hipingLogEditText.append(log + "\n");
                if (autoScrollEnabled) {
                    View lastChild = hipingLogScrollView.getChildAt(hipingLogScrollView.getChildCount() - 1);
                    int bottom = lastChild.getBottom() + hipingLogScrollView.getPaddingBottom();
                    int sy = hipingLogScrollView.getScrollY();
                    int sh = hipingLogScrollView.getHeight();
                    int delta = bottom - (sy + sh);
                    hipingLogScrollView.smoothScrollBy(0, delta);
                }

            }
        });
    }

    private void setHipingTimeLineChartData(long timestamp, int value, boolean init) {
        if (init) {
            hipingTimeArrayList.clear();
            hipingTimeIntArrayList.clear();
            hipintTimestampArrayList.clear();
        }

        int countOldValues = 0;

        for (int i = 0; i < hipintTimestampArrayList.size(); i++) {
            if ((timestamp / 1000 - hipingStartGraph) - hipintTimestampArrayList.get(i) > Constants.MAX_HIPING_TIME_LINECHART_XAXIS) {
                countOldValues++;
            }
            break;
        }

        for (int i = 0; i < countOldValues; i++) {
            hipingTimeArrayList.remove(0);
            hipingTimeIntArrayList.remove(0);
            hipintTimestampArrayList.remove(0);
        }

        Log.d(TAG, "hipingTimeIntArrayList size: " + hipingTimeIntArrayList.size());

        if (!init) {
            XAxis xAxis = hipingTimeLineChart.getXAxis();
            hipingTimeArrayList.add(new Entry(timestamp / 1000 - hipingStartGraph, value));//, getResources().getDrawable(R.drawable.star)));
            hipingTimeIntArrayList.add(value);
            hipintTimestampArrayList.add(timestamp / 1000 - hipingStartGraph);

            YAxis yAxis = hipingTimeLineChart.getAxisLeft();
            long maxValue = Collections.max(hipingTimeIntArrayList);
            yAxis.setAxisMaximum(maxValue + (int) (maxValue * 0.10));
            yAxis.setAxisMinimum(0);


            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setAxisMinimum(timestamp / 1000 - hipingStartGraph - 30);
            xAxis.setAxisMaximum(timestamp / 1000 - hipingStartGraph);

        } else {
            XAxis xAxis = hipingTimeLineChart.getXAxis();
            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);

            xAxis.setAxisMinimum(-30);
            xAxis.setAxisMaximum(0);


        }

        LineDataSet set1;

        if (hipingTimeLineChart.getData() != null &&
                hipingTimeLineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) hipingTimeLineChart.getData().getDataSetByIndex(0);
            set1.setValues(hipingTimeArrayList);
            set1.notifyDataSetChanged();
            hipingTimeLineChart.getData().notifyDataChanged();
            hipingTimeLineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(hipingTimeArrayList, "HiPing");


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
                    return hipingTimeLineChart.getAxisLeft().getAxisMinimum();
                }
            });

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                //Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                // set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets
            LineData data = new LineData(dataSets);
            hipingTimeLineChart.setData(data);
        }
    }

    public String getHipingLog() {
        /*String mPath = Environment.getExternalStorageDirectory().toString() + "/" + "hiping_image.jpeg" ;
        this.hipingTimeLineChart.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(hipingTimeLineChart.getDrawingCache());
        hipingTimeLineChart.setDrawingCacheEnabled(false);
        OutputStream fout = null;
        File imageFile = new File(mPath);

        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        return this.hipingLogEditText.getText().toString();
    }

    public boolean isHipingRunning() {
        return hipingRunning;
    }

    public native void startPing(String hicnName, short sourcePort, short destPort, long ttl, long pingInterval, long maxPing, long lifeTime, boolean openTcpConnection, boolean sendSynMessage, boolean sendAckMessage);

    public native void stopPing();
}