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

package com.cisco.hicn.forwarder.supportlibrary;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.cisco.hicn.forwarder.MainActivity;
import com.cisco.hicn.forwarder.utility.Constants;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Queue;

public class AndroidUtility {

    private static AndroidUtility sInstance = null;

    private Queue<Integer> hiperfGraphQueue;

    public static AndroidUtility getInstance() {
        if (sInstance == null) {
            sInstance = new AndroidUtility();
        }
        return sInstance;
    }



    public void setHiperfGraphQueue(Queue<Integer> hiperfGraphQueue) {
        this.hiperfGraphQueue = hiperfGraphQueue;
    }

    public Queue<Integer> getHiperfGraphQueue() {
        return hiperfGraphQueue;
    }

    public static void pushGoodput(int goodput) {
        Log.d("hiperf", "goodput: " + goodput);
        AndroidUtility.getInstance().getHiperfGraphQueue().add(goodput);
    }

    public static int getTextureCount() {
        return 0;
    }
}
