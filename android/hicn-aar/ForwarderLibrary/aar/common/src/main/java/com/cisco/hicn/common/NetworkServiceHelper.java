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

package com.cisco.hicn.common;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;
import androidx.annotation.NonNull;
import java.net.Inet4Address;

public class NetworkServiceHelper {
    private static final String TAG = "Hicn." + NetworkServiceHelper.class.getSimpleName();

    private ConnectivityManager mConMan;
    private SocketBinder mSocketBinder;
    private ConnectivityManager.NetworkCallback mMobileNetworkCallback;
    private ConnectivityManager.NetworkCallback mWifiNetworkCallback;

    public void init(Context ctx, SocketBinder socketBinder) {
        mConMan = (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mSocketBinder = socketBinder;

        requestMobileNetwork();
        requestWifiNetwork();
    }

    public void clear() {
        if (mConMan != null) {
            if (mMobileNetworkCallback != null) {
                mConMan.unregisterNetworkCallback(mMobileNetworkCallback);
                mMobileNetworkCallback = null;
                Log.i(TAG, "Mobile network released");
            }
            if (mWifiNetworkCallback != null) {
                mConMan.unregisterNetworkCallback(mWifiNetworkCallback);
                mWifiNetworkCallback = null;
                Log.i(TAG, "Wi-Fi network released");
            }
        }
    }

    private void requestMobileNetwork() {
        final int[] transportTypes = new int[]{NetworkCapabilities.TRANSPORT_CELLULAR};
        final int[] capabilities = new int[]{NetworkCapabilities.NET_CAPABILITY_INTERNET, NetworkCapabilities.NET_CAPABILITY_NOT_VPN};

        if (mMobileNetworkCallback != null) {
            Log.d(TAG, "Already mobile network requested");
            return;
        }
        mMobileNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                mSocketBinder.addNetwork(getIfname(transportTypes, capabilities), network);
            }
        };

        Log.i(TAG, "Request mobile network");
        requestPreferredNetworksWith(transportTypes, capabilities, mMobileNetworkCallback);
    }

    private void requestWifiNetwork() {
        final int[] transportTypes = new int[]{NetworkCapabilities.TRANSPORT_WIFI};
        final int[] capabilities = new int[]{NetworkCapabilities.NET_CAPABILITY_INTERNET, NetworkCapabilities.NET_CAPABILITY_NOT_VPN};


        if (mWifiNetworkCallback != null) {
            Log.d(TAG, "Already Wi-Fi network requested");
            return;
        }
        mWifiNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                mSocketBinder.addNetwork(getIfname(transportTypes, capabilities), network);
            }
        };

        Log.i(TAG, "Request Wi-Fi network");
        requestPreferredNetworksWith(transportTypes, capabilities, mWifiNetworkCallback);
    }

    private void requestPreferredNetworksWith(@NonNull int[] transportTypes, @NonNull int[] capabilities,
                                              @NonNull ConnectivityManager.NetworkCallback callback) {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        for (int trans : transportTypes)
            builder.addTransportType(trans);

        for (int cap : capabilities)
            builder.addCapability(cap);

        NetworkRequest request = builder.build();
        mConMan.requestNetwork(request, callback);
    }

    private boolean hasAllFlags(NetworkCapabilities nc,
                                @NonNull int[] transportTypes, @NonNull int[] capabilities) {
        for (int t : transportTypes) {
            if (!nc.hasTransport(t))
                return false;
        }
        for (int c : capabilities) {
            if (!nc.hasCapability(c))
                return false;
        }
        return true;
    }

    private String getIfname(@NonNull int[] transportTypes, @NonNull int[] capabilities) {
        String ifname = "";

        final Network[] networks = mConMan.getAllNetworks();
        for (final Network nw : networks) {
            NetworkCapabilities nc = mConMan.getNetworkCapabilities(nw);
            if (hasAllFlags(nc, transportTypes, capabilities)) {
                LinkProperties lp = mConMan.getLinkProperties(nw);
                Log.d(TAG, "iface: " + lp.toString());
                for (LinkAddress addr : lp.getLinkAddresses()) {
                    if (addr.getAddress() instanceof Inet4Address) {
                        ifname = lp.getInterfaceName();
                        break;
                    }
                }
            }
        }
        return ifname;
    }
}
