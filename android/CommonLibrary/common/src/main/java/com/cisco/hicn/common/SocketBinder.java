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

import android.net.Network;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

public class SocketBinder {
    private static final String TAG = "Hicn." + SocketBinder.class.getSimpleName();
    private Hashtable<String, Network> mNetworks = new Hashtable<>();

    public void addNetwork(String ifname, Network network) {
        Log.i(TAG, "Network added: " + ifname);
        mNetworks.put(ifname, network);
    }

    public int getNumberOfNetworks() {
        return mNetworks.size();
    }

    public ArrayList<String> getAllNetworkNames() {
        return Collections.list(mNetworks.keys());
    }

    public Network getNetwork(String ifname) {
        return mNetworks.get(ifname);
    }

    public boolean bindSocket(int sock, String ifname) {
        boolean ret = false;

        Network network = mNetworks.get(ifname);
        if (network != null)
            ret = bindSocket(sock, network);

        Log.i(TAG, "bindSocket(" + sock + ", " + ifname + ")=" + ret);
        return ret;
    }

    private boolean bindSocket(int sock, Network network) {
        try {
            FileDescriptor fd = new FileDescriptor();
            Field field = FileDescriptor.class.getDeclaredField("descriptor");
            field.setAccessible(true);
            field.setInt(fd, sock);
            field.setAccessible(false);

            network.bindSocket(fd);
            return true;
        } catch (SecurityException | IllegalArgumentException
                | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return false;
    }
}
