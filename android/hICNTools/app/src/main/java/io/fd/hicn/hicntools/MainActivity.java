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

package io.fd.hicn.hicntools;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.fd.hicn.hicntools.ui.fragments.HiPerfFragment;
import io.fd.hicn.hicntools.ui.fragments.HiPingFragment;
import io.fd.hicn.hicntools.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("hicn");
    }

    private SectionsPagerAdapter sectionsPagerAdapter = null;
    private int currentItem = 0;
    private boolean swipeEnabled = true;
    private ImageView mainShareIamgeView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkEnabledPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        checkEnabledPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        checkEnabledPermission(Manifest.permission.FOREGROUND_SERVICE);
        setContentView(R.layout.activity_main);


        mainShareIamgeView = findViewById(R.id.main_share_iamgeview);
        final ViewPager touchView = findViewById(R.id.view_pager);
        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // disable swipe
                if (position == 1) {
                    mainShareIamgeView.setVisibility(View.INVISIBLE);
                } else {
                    mainShareIamgeView.setVisibility(View.VISIBLE);
                }

                if (!swipeEnabled) {
                    touchView.setCurrentItem(currentItem);
                }
            }

            public void onPageScrollStateChanged(int state) {
            }

            public void onPageSelected(int position) {
            }
        };
        touchView.addOnPageChangeListener(onPageChangeListener);

        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        mainShareIamgeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabLayout tabs = findViewById(R.id.tabs);
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

                switch (tabs.getSelectedTabPosition()) {
                    case 0:
                        HiPerfFragment hiperfFragment = (HiPerfFragment) getSupportFragmentManager().getFragments().get(2);
                        if (!hiperfFragment.isHipingRunning()) {
                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            String shareSub = "Hiperf Log " + dateFormat.format(date);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, hiperfFragment.getHiperfLog());
                            startActivity(Intent.createChooser(sharingIntent, "Share using"));
                        }
                        break;

                    case 1:

                        HiPingFragment hipingFragment = (HiPingFragment) getSupportFragmentManager().getFragments().get(0);
                        if (!hipingFragment.isHipingRunning()) {
                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            String shareSub = "HiPing Log " + dateFormat.format(date);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, hipingFragment.getHipingLog());
                            startActivity(Intent.createChooser(sharingIntent, "Share using"));
                        }
                        break;

                    default:
                        break;
                }
            }
        });
    }


    public void setDisableSwipe(int currentItem) {
        swipeEnabled = false;
        this.currentItem = currentItem;

    }

    public void enableSwipe() {
        swipeEnabled = true;
    }

    private void checkEnabledPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        1);
            }
        }
    }


}