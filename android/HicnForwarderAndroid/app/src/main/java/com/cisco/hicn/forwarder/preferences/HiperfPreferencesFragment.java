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

package com.cisco.hicn.forwarder.preferences;

import android.net.Uri;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.cisco.hicn.forwarder.R;

public class HiperfPreferencesFragment extends PreferenceFragmentCompat {


    private OnFragmentInteractionListener mListener;

    public HiperfPreferencesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        setPreferencesFromResource(R.xml.hiperf, s);

        SwitchPreference enableHiperWindowSizeCheckBoxPreference = getPreferenceScreen().findPreference(getString(R.string.enable_hiperf_window_size_key));

        getPreferenceScreen().findPreference(getString(R.string.hiperf_window_size_key)).setEnabled(enableHiperWindowSizeCheckBoxPreference.isChecked());


        getPreferenceScreen().findPreference(getString(R.string.hiperf_raaqm_beta_key)).setOnPreferenceChangeListener((preference, newValue) -> {

            int hiperfRaaqm = Integer.parseInt((String) newValue);

            if (hiperfRaaqm < 0 || hiperfRaaqm > 65535)
                return false;
            return true;
        });

        getPreferenceScreen().findPreference(getString(R.string.hiperf_raaqm_drop_factor_key)).setOnPreferenceChangeListener((preference, newValue) -> {

            double hiperfRaaqmDropFactor = Double.parseDouble((String) newValue);

            if (hiperfRaaqmDropFactor < 0 || hiperfRaaqmDropFactor > 100)
                return false;
            return true;
        });

        getPreferenceScreen().findPreference(getString(R.string.hiperf_interest_lifetime_key)).setOnPreferenceChangeListener((preference, newValue) -> {

            long hiperfInterestLifetime = Long.parseLong((String) newValue);

            if (hiperfInterestLifetime < 0 || hiperfInterestLifetime > 10000000)
                return false;
            return true;
        });

        getPreferenceScreen().findPreference(getString(R.string.enable_hiperf_window_size_key)).setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enableWindowSize = (boolean) newValue;

            getPreferenceScreen().findPreference(getString(R.string.hiperf_window_size_key)).setEnabled(enableWindowSize);

            return true;
        });

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}



