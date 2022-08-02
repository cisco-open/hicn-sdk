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

package com.cisco.hicn.hproxylibrary.service;

import android.app.Service;
import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;

import java.util.HashSet;
import java.util.Properties;

import com.cisco.hicn.hproxylibrary.R;
import com.cisco.hicn.hproxylibrary.supportlibrary.HProxyLibrary;
import com.cisco.hicn.hproxylibrary.supportlibrary.PuntingSpec;



public class ProxyBackendNative extends ProxyBackend {

    private ParcelFileDescriptor mTunPFd = null; // A11v3

    public ProxyBackendNative(Service parentService) {
        super(parentService);

    }


    private String getTag() {
        return ProxyBackendNative.class.getSimpleName();
    }
}
