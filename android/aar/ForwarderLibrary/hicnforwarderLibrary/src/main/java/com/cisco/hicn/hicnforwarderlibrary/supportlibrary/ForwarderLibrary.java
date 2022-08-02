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

package com.cisco.hicn.hicnforwarderlibrary.supportlibrary;

import android.util.Log;

import com.cisco.hicn.common.BuildConfig;
import com.cisco.hicn.common.SocketBinder;

public class ForwarderLibrary {

    private static ForwarderLibrary sInstance = null;
    private SocketBinder mSocketBinder;

    static {
        System.loadLibrary("forwarder-wrapper");
    }

    public static ForwarderLibrary getInstance() {
        if (sInstance == null) {
            sInstance = new ForwarderLibrary();
        }
        return sInstance;
    }

    public void setSocketBinder(SocketBinder socketBinder) {
        mSocketBinder = socketBinder;
    }

    private boolean bindSocket(int sock, String ifname) {
        Log.i("Hicn.forwarder", "request to bind a socket(" + sock + ") with " + ifname);
        return mSocketBinder.bindSocket(sock, ifname);
    }

    private ForwarderLibrary() {
    }

    public static native String getForwarderVersion();

    public native boolean isRunningForwarder();

    public native void startForwarder(int capacity, int logLevel);

    public native void stopForwarder();

    public static String getForwarderAarVersion() {
        return BuildConfig.VERSION;
    }
}
