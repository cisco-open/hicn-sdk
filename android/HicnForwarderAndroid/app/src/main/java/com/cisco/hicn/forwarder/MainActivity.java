/*
 * Copyright (c) 2022 Cisco and/or its affiliates.
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

package com.cisco.hicn.forwarder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cisco.hicn.forwarder.applications.ApplicationsFragment;
import com.cisco.hicn.forwarder.forwarder.ForwarderFragment;
import com.cisco.hicn.forwarder.hiperf.HiPerfFragment;
import com.cisco.hicn.forwarder.interfaces.InterfacesFragment;
import com.cisco.hicn.forwarder.preferences.PreferencesFragment;
import com.cisco.hicn.facemgrlibrary.utility.NetdeviceTypeEnum;
import com.google.android.material.navigation.NavigationView;

import com.cisco.hicn.facemgrlibrary.supportlibrary.FacemgrLibrary;

public class MainActivity extends AppCompatActivity
        implements
        PreferencesFragment.OnFragmentInteractionListener,
        ForwarderFragment.OnFragmentInteractionListener,
        InterfacesFragment.OnFragmentInteractionListener,
        ApplicationsFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private NavigationView nav_View;
    private FragmentManager fragmentManager;
    private HiPerfFragment hiperf;


    private HomeActivity home;
    private PreferencesFragment settings;

    public static Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkEnabledPermission(Manifest.permission.FOREGROUND_SERVICE);
        checkEnabledPermission(Manifest.permission.BIND_VPN_SERVICE);
        checkEnabledPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);

        MainActivity.context = getApplicationContext();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean aa = pm.isIgnoringBatteryOptimizations(this.getPackageName());




        setContentView(R.layout.activity_main);
        mDrawer = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nav_View = findViewById(R.id.nav_View);
        nav_View.setNavigationItemSelectedListener(this);
        fragmentManager = getSupportFragmentManager();

        // Declare fragments here
        home = new HomeActivity();
        settings = new PreferencesFragment();
        hiperf = new HiPerfFragment();
        hiperf.setHome(home);

        FacemgrLibrary facemgr = FacemgrLibrary.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int facemgrLogLevel = Integer.parseInt(sharedPreferences.getString(getString(R.string.facemgr_log_level_key), getString(R.string.facemgr_default_log_level)));
        facemgr.setLogLevel(facemgrLogLevel);

        boolean enableBonjour = sharedPreferences.getBoolean(getString(R.string.enable_bonjour_key), false);
        facemgr.enableDiscovery(enableBonjour);

        boolean enableNexHopIPv4 = sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv4_key), true);
        facemgr.enableIPv4(enableNexHopIPv4 ? 1 : 0);

        boolean enableNexHopIPv6 = sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv6_key), false);
        facemgr.enableIPv6(enableNexHopIPv6 ? 1 : 0);

        boolean enableWifi = sharedPreferences.getBoolean(getString(R.string.enable_wifi_key), true);

        if (enableWifi) {

            int wifiSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_source_port_ipv4_key), getString(R.string.default_wifi_source_port_ipv4)));
            String wifiNextHopIPv4 = sharedPreferences.getString(getString(R.string.wifi_nexthop_ipv4_key), getString(R.string.default_wifi_nexthop_ipv4));
            int wifiNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_nexthop_port_ipv4_key), getString(R.string.default_wifi_nexthop_port_ipv4)));
            facemgr.updateInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_WIFI.getValue(), wifiSourcePortIPv4, wifiNextHopIPv4, wifiNextHopPortIPv4);

            int wifiSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_source_port_ipv6_key), getString(R.string.default_wifi_source_port_ipv6)));
            String wifiNextHopIPv6 = sharedPreferences.getString(getString(R.string.wifi_nexthop_ipv6_key), getString(R.string.default_wifi_nexthop_ipv6));
            int wifiNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_nexthop_port_ipv6_key), getString(R.string.default_wifi_nexthop_port_ipv6)));
            facemgr.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIFI.getValue(), wifiSourcePortIPv6, wifiNextHopIPv6, wifiNextHopPortIPv6);
        }

        boolean cellularWifi = sharedPreferences.getBoolean(getString(R.string.enable_cellular_key), true);

        if (cellularWifi) {
            int cellularSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_source_port_ipv4_key), getString(R.string.default_cellular_source_port_ipv4)));
            String cellularNextHopIPv4 = sharedPreferences.getString(getString(R.string.cellular_nexthop_ipv4_key), getString(R.string.default_cellular_nexthop_ipv4));
            int cellularNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_nexthop_port_ipv4_key), getString(R.string.default_cellular_nexthop_port_ipv4)));
            facemgr.updateInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_CELLULAR.getValue(), cellularSourcePortIPv4, cellularNextHopIPv4, cellularNextHopPortIPv4);

            int cellularSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_source_port_ipv6_key), getString(R.string.default_cellular_source_port_ipv6)));
            String cellularNextHopIPv6 = sharedPreferences.getString(getString(R.string.cellular_nexthop_ipv6_key), getString(R.string.default_cellular_nexthop_ipv6));
            int cellularNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_nexthop_port_ipv6_key), getString(R.string.default_cellular_nexthop_port_ipv6)));
            facemgr.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_CELLULAR.getValue(), cellularSourcePortIPv6, cellularNextHopIPv6, cellularNextHopPortIPv6);
        }

        boolean wiredWifi = sharedPreferences.getBoolean(getString(R.string.enable_wired_key), true);

        if (wiredWifi) {
            int wiredSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv4_key), getString(R.string.default_wired_source_port_ipv4)));
            String wiredNextHopIPv4 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv4_key), getString(R.string.default_wired_nexthop_ipv4));
            int wiredNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv4_key), getString(R.string.default_wired_nexthop_port_ipv4)));
            facemgr.updateInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), wiredSourcePortIPv4, wiredNextHopIPv4, wiredNextHopPortIPv4);

            int wiredSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv6_key), getString(R.string.default_wired_source_port_ipv6)));
            String wiredNextHopIPv6 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv6_key), getString(R.string.default_wired_nexthop_ipv6));
            int wiredNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv6_key), getString(R.string.default_wired_nexthop_port_ipv6)));
            facemgr.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), wiredSourcePortIPv6, wiredNextHopIPv6, wiredNextHopPortIPv6);
        }
        fragmentManager.beginTransaction().replace(R.id.viewLayout, home).commit();

        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();
        boolean ignoringOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName);

        if (ignoringOptimizations) {
            return;
        }

        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

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

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment;
        switch (menuItem.getItemId()) {
            case R.id.settings:
                fragment = settings;
                break;
            case R.id.forwarder:
                fragment = home;
                break;
            case R.id.hiperf:
                fragment = hiperf;
                break;
            default:
                fragment = home;
                break;
        }
        setTitle(menuItem.getTitle());

        fragmentManager.beginTransaction().replace(R.id.viewLayout, fragment).commit();
        mDrawer.closeDrawers();
        return true;

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

}
