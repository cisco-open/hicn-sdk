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

import android.net.Uri;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.cisco.hicn.forwarder.R;

import org.apache.http.conn.util.InetAddressUtilsHC4;

public class HproxyPreferencesFragment extends PreferenceFragmentCompat {


    private OnFragmentInteractionListener mListener;

    public HproxyPreferencesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        setPreferencesFromResource(R.xml.hproxy, s);

        getPreferenceScreen().findPreference(getString(R.string.pref_hproxy_server_key)).setOnPreferenceChangeListener((preference, newValue) -> {


            String serverAddress = (String) newValue;
            if (!InetAddressUtilsHC4.isIPv4Address(serverAddress)) {
                return false;
            }
            return true;
        });

        getPreferenceScreen().findPreference(getString(R.string.pref_hproxy_port_key)).setOnPreferenceChangeListener((preference, newValue) -> {

            int serverPort = Integer.parseInt((String) newValue);

            if (serverPort < 0 || serverPort > 65536)
                return false;
            return true;

        });

        getPreferenceScreen().findPreference(getString(R.string.hproxy_secret_key)).setOnPreferenceChangeListener((preference, newValue) -> {

            String secret = (String) newValue;
            if (secret != null && !secret.isEmpty()) {
                return false;
            }
            return true;

        });

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}



