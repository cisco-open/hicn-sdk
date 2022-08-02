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

public class WiredPreferencesFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.wired, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sharedPreferences.getBoolean(getString(R.string.enable_wired_key), false)) {
            getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setSummary(getString(R.string.enabled));
            getPreferenceManager().findPreference((getString(R.string.wired_ipv4_preferences_key))).setEnabled(true);
            getPreferenceManager().findPreference((getString(R.string.wired_ipv6_preferences_key))).setEnabled(true);
        } else {
            getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setSummary(getString(R.string.disabled));
            getPreferenceManager().findPreference((getString(R.string.wired_ipv4_preferences_key))).setEnabled(false);
            getPreferenceManager().findPreference((getString(R.string.wired_ipv6_preferences_key))).setEnabled(false);
        }

        getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setSummary(getString(R.string.enabled));
                getPreferenceManager().findPreference((getString(R.string.wired_ipv4_preferences_key))).setEnabled(true);
                getPreferenceManager().findPreference((getString(R.string.wired_ipv6_preferences_key))).setEnabled(true);

                FacemgrLibrary facemgr = FacemgrLibrary.getInstance();
                int wiredSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv4_key), getString(R.string.default_wired_source_port_ipv4)));
                String wiredNextHopIPv4 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv4_key), getString(R.string.default_wired_nexthop_ipv4));
                int wiredNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv4_key), getString(R.string.default_wired_nexthop_port_ipv4)));
                facemgr.updateInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), wiredSourcePortIPv4, wiredNextHopIPv4, wiredNextHopPortIPv4);

                int wiredSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv6_key), getString(R.string.default_wired_source_port_ipv6)));
                String wiredNextHopIPv6 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv6_key), getString(R.string.default_wired_nexthop_ipv6));
                int wiredNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv6_key), getString(R.string.default_wired_nexthop_port_ipv6)));
                facemgr.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), wiredSourcePortIPv6, wiredNextHopIPv6, wiredNextHopPortIPv6);
            } else {
                getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setSummary(getString(R.string.enabled));
                getPreferenceManager().findPreference((getString(R.string.wired_ipv4_preferences_key))).setEnabled(false);
                getPreferenceManager().findPreference((getString(R.string.wired_ipv6_preferences_key))).setEnabled(false);

                FacemgrLibrary facemgr = FacemgrLibrary.getInstance();
                facemgr.unsetInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue());
                facemgr.unsetInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue());
            }
            return true;
            
        });
    }
}
