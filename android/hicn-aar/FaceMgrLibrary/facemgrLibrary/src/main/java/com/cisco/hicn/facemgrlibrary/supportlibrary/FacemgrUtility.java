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
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.util.Pair;

import com.cisco.hicn.facemgrlibrary.utility.Constants;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashMap;

public class FacemgrUtility {
    private static Context mContext = null;
    private static FacemgrUtility sInstance = null;
    private static ConnectivityManager.NetworkCallback mConnectivityCallback = null;

    /* Down events do not report any information about the network but netId. So we need to maintain
     * those in a map to be able to interpret down events correctly and forge the corresponding
     * facelet.
     */
    private static HashMap<Long, Pair<String, Integer>> mapNetId =
            new HashMap<Long, Pair<String, Integer>>();

    public static FacemgrUtility getInstance() {
        if (sInstance == null) {
            sInstance = new FacemgrUtility();
        }
        return sInstance;
    }

    public static int getNetworkType(ConnectivityManager cm, Network network) {
        NetworkCapabilities nc = cm.getNetworkCapabilities(network);
        if (nc == null) {
            return Constants.AU_INTERFACE_TYPE_UNDEFINED;
        }
        if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return Constants.AU_INTERFACE_TYPE_UNDEFINED; // not ready
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return Constants.AU_INTERFACE_TYPE_WIRED;
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return Constants.AU_INTERFACE_TYPE_WIFI;
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return Constants.AU_INTERFACE_TYPE_CELLULAR;
        }
        return Constants.AU_INTERFACE_TYPE_UNDEFINED;
    }

    public static int getWifiRSSI() {
        return getWifiRSSI(mContext);
    }

    public static int getWifiRSSI(Context context) {
        try {
            if (context == null) return -1;
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return -1; // error
            }
            // wifiManager.startScan();
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

    /* Android SDK interface */

    public static int initialize() {
        return initialize(mContext);
    }

    public static int initialize(Context context) {
        if (context == null) return -1;

        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return -1;

        mConnectivityCallback = new ConnectivityManager.NetworkCallback() {
            private void onEvent(Network network, boolean up) {
                if (!up) {
                    /* For down events, we have no information as both cm.getNetworkCapabilities in
                     * getNetworkType and cm.getLinkProperties will return null. We rely on an
                     * internal map to remember needed information about those interfaces :
                     * netdevice and family, which are used as identifiers in the facelet.
                     */
                    Pair<String, Integer> pair = mapNetId.get(new Long(network.getNetworkHandle()));
                    if (pair == null) return;

                    FacemgrLibrary.onNetworkEvent(pair.first, Constants.AU_INTERFACE_TYPE_UNDEFINED, false, pair.second, null);
                    return;
                }

                int iftype = getNetworkType(cm, network);
                int family = 0; // AF_UNSPEC;

                LinkProperties lp = cm.getLinkProperties(network);
                if (lp == null) return;

                Log.i("FACEMGR", "Network " + lp.toString() + "is up");

                for (LinkAddress addr : lp.getLinkAddresses()) {
                    String ifname = lp.getInterfaceName();

                    if ((ifname != null) && (iftype == Constants.AU_INTERFACE_TYPE_UNDEFINED)) {
                        try {
                            NetworkInterface networkInterface = NetworkInterface.getByName(ifname);
                            if (networkInterface.isLoopback()) {
                                iftype = Constants.AU_INTERFACE_TYPE_LOOPBACK;
                            }
                        } catch (SocketException e) {
                            Log.d(FacemgrUtility.class.getCanonicalName(), "error");
                        }
                    }

                    if (addr.getAddress() instanceof Inet4Address) {
                        family = 2; //AF_INET
                    }
                    if (addr.getAddress() instanceof Inet6Address) {
                        family = 10; //AF_INET6
                    }

                    /* Store association of facelet keys with netId */
                    mapNetId.put(new Long(network.getNetworkHandle()), new Pair<String, Integer>(ifname, family));

                    FacemgrLibrary.onNetworkEvent(ifname, iftype, true, family, addr.getAddress().getHostAddress());
                }
            } // onEvent

            @Override
            public void onAvailable(Network network){
                onEvent(network, true);
            }
            @Override
            public void onLost(Network network) {
                onEvent(network, false);
            }
        };

        NetworkRequest request =
            new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED) // XXX
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                .build();

        cm.registerNetworkCallback(request, mConnectivityCallback);
        return 0;
    } // initialize

    public static int terminate() {
        return terminate(mContext);
    }

    public static int terminate(Context context) {
        if (context == null) return -1;
        if (mConnectivityCallback == null) return -1;

        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return -1;

        cm.unregisterNetworkCallback(mConnectivityCallback);
        mConnectivityCallback = null;

        return 0;
    } // finalize

}
