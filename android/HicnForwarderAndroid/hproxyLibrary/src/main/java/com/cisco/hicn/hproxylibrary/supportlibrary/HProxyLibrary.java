/*
 * Copyright (c) 2019-2020 Cisco and/or its affiliates.
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


package com.cisco.hicn.hproxylibrary.supportlibrary;

import android.app.Activity;

import android.content.ComponentName;

import android.content.ServiceConnection;
import android.os.IBinder;


import com.cisco.hicn.hproxylibrary.service.ForwarderWrapper;
import com.cisco.hicn.hproxylibrary.service.ProxyBackend;

public class HProxyLibrary {
    private static HProxyLibrary sInstance = null;

    public static HProxyLibrary getInstance() {
        if (sInstance == null) {
            sInstance = new HProxyLibrary();
        }
        return sInstance;
    }

    public static void stopInstance() {
        if (sInstance == null)
            return;
        sInstance = null;
    }

    public HProxyLibrary() {
    }

    public static void setForwarderWrapper(ForwarderWrapper forwarderWrapper) {
    }

    public static void setForwarderService(Class<?> backendAndroidServiceClass) {
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

        }
        public void onServiceDisconnected(ComponentName className) {

        }
    };

    public static void setActivity(Activity activity) {
    }

    public static Activity getActivity() {
        return null;
    }

    public static PuntingSpec[] getPuntingSpecs() {
        return null;
    }

    public boolean isRunning() {
        return false;
    }

    public static boolean isHProxyEnabled() {
        return false;
    }



    // END WITH_START_STOP

    private final String getTag() {
        return HProxyLibrary.class.getSimpleName();
    }

    public static boolean isStunServiceAvailable() {
        return false;
    }


}







