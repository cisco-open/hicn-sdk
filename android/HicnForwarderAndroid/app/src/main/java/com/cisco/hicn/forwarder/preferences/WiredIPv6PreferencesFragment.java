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

package com.cisco.hicn.forwarder.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.PreferenceFragmentCompat;

import com.cisco.hicn.forwarder.R;

import com.cisco.hicn.facemgrlibrary.supportlibrary.FacemgrLibrary;
import com.cisco.hicn.facemgrlibrary.utility.NetdeviceTypeEnum;

import org.apache.http.conn.util.InetAddressUtilsHC4;

public class WiredIPv6PreferencesFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences = null;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.wired_ipv6, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        getPreferenceScreen().findPreference(getString(R.string.wired_source_port_ipv6_key)).setEnabled(sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv6_key), false));
        getPreferenceScreen().findPreference(getString(R.string.wired_nexthop_ipv6_key)).setEnabled(sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv6_key), false));
        getPreferenceScreen().findPreference(getString(R.string.wired_nexthop_port_ipv6_key)).setEnabled(sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv6_key), false));

        getPreferenceScreen().findPreference(getString(R.string.wired_source_port_ipv6_key)).setOnPreferenceChangeListener((preference, newValue) -> {

            int sourcePort = Integer.parseInt((String) newValue);

            if (sourcePort < 0 || sourcePort > 65535)
                return false;
            String nextHopIp = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv6_key), getString(R.string.default_wired_nexthop_ipv6));
            int nextHopPort = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv6_key), getString(R.string.default_wired_nexthop_port_ipv6)));

            FacemgrLibrary facemgr = FacemgrLibrary.getInstance();

            facemgr.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), sourcePort, nextHopIp, nextHopPort);
            return true;

        });

        getPreferenceScreen().findPreference(getString(R.string.wired_nexthop_ipv6_key)).setOnPreferenceChangeListener((preference, newValue) -> {

            String nextHopIp = (String) newValue;
            if (!InetAddressUtilsHC4.isIPv6Address(nextHopIp)) {
                return false;
            }

            int sourcePort = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv6_key), getString(R.string.default_wired_source_port_ipv6)));
            int nextHopPort = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv6_key), getString(R.string.default_wired_nexthop_port_ipv6)));

            FacemgrLibrary facemgr = FacemgrLibrary.getInstance();

            facemgr.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), sourcePort, nextHopIp, nextHopPort);
            return true;

        });

        getPreferenceScreen().findPreference(getString(R.string.wired_nexthop_port_ipv6_key)).setOnPreferenceChangeListener((preference, newValue) -> {

            int nextHopPort = Integer.parseInt((String) newValue);

            if (nextHopPort < 0 || nextHopPort > 65535)
                return false;
            int sourcePort = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv6_key), getString(R.string.default_wired_source_port_ipv6)));

            String nextHopIp = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv6_key), getString(R.string.default_wired_nexthop_ipv6));

            FacemgrLibrary facemgr = FacemgrLibrary.getInstance();

            facemgr.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), sourcePort, nextHopIp, nextHopPort);
            return true;

        });
    }
}
