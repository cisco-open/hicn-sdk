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

package com.cisco.hicn.facemgrlibrary.supportlibrary;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.net.SocketException;

import com.cisco.hicn.facemgrlibrary.utility.Constants;

public class FacemgrUtility {

    private static Context mContext;
    private static FacemgrUtility sInstance = null;


    public static FacemgrUtility getInstance() {
        if (sInstance == null) {
            sInstance = new FacemgrUtility();
        }
        return sInstance;
    }

    public static int getNetworkType(String networkName) {
        return getNetworkType(mContext, networkName);
    }

    public static int getNetworkType(Context context, String networkName) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                return -1; //error
            }

            for (Network network : connectivityManager.getAllNetworks()) {
                LinkProperties prop = connectivityManager.getLinkProperties(network);
                if (prop.getInterfaceName() != null && prop.getInterfaceName().equals(networkName.trim())) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                    if (capabilities == null) {
                        return Constants.AU_INTERFACE_TYPE_UNDEFINED; //error
                    }

                    if (capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            return Constants.AU_INTERFACE_TYPE_WIRED;
                        }
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return Constants.AU_INTERFACE_TYPE_WIFI;
                        }
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return Constants.AU_INTERFACE_TYPE_CELLULAR;
                        }
                        return Constants.AU_INTERFACE_TYPE_UNDEFINED;
                    } else {
                        return Constants.AU_INTERFACE_TYPE_UNAVAILABLE; //not ready
                    }
                }

            }

            try {
                NetworkInterface networkInterface = NetworkInterface.getByName(networkName);
                if (networkInterface.isLoopback())
                    return Constants.AU_INTERFACE_TYPE_LOOPBACK;
            } catch (SocketException e) {
                Log.d(FacemgrUtility.class.getCanonicalName(), "error");
            }
        }
        return Constants.AU_INTERFACE_TYPE_UNDEFINED; //don't care
    }

    public static int getWifiRSSI() {
        return getWifiRSSI(mContext);
    }

    public static int getWifiRSSI(Context context) {
        try {
            if (context == null)
                return -1;
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return -1; //error
            }
            //wifiManager.startScan();
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            int rssi = wifiInfo.getRssi();
            Log.i("FACEMGR RSSI", Integer.toString(rssi));
            return rssi;
        } catch (Exception e) {
            // eg. ACCESS_WIFI_STATE permission is missing
            e.printStackTrace();
            Log.e("FACEMGR", e.toString());
            return -2;
        }
    }

    public static int getTextureCount() {
        return 0;
    }

    public static void setContext(Context context) {
        mContext = context;
    }
}
