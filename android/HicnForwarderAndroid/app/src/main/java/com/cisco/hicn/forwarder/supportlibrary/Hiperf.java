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

public class Hiperf {

    private static Hiperf sInstance = null;

    static {
        System.loadLibrary("hiperf-wrapper");
    }

    public static Hiperf getInstance() {
        if (sInstance == null) {
            sInstance = new Hiperf();
        }
        return sInstance;
    }


    public native void startHiPerf(String hicnName,
                                   double betaParameter,
                                   double dropFactorParameter,
                                   int windowSize,
                                   long statsInterval,
                                   boolean rtcProtocol,
                                   long interestLiftime);

    public native void stopHiPerf();

}
