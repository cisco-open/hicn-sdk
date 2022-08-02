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
import android.os.Handler;
import android.os.Message;


public class ProxyBackend implements Runnable, Handler.Callback {
    protected ProxyBackend(final Service parentService) {
    }

    @Override
    public boolean handleMessage(Message message) {

        return false;
    }

    public void disconnect() {

    }

    public boolean connect() {

        return false;
    }

    @Override
    public void run() {


    }






    private final String getTag() {
        return "HProxyBackend"; //ProxyBackend.class.getSimpleName();
    }
}
