/*
 * Copyright (c) 2022 Cisco and/or its affiliates.
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

package com.cisco.hicn.forwarder;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cisco.hicn.forwarder.applications.ApplicationsFragment;
import com.cisco.hicn.forwarder.forwarder.ForwarderFragment;
import com.cisco.hicn.forwarder.hiperf.HiPerfFragment;
import com.cisco.hicn.forwarder.interfaces.InterfacesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends Fragment {
    BottomNavigationView bottomNavigationView;

    FragmentManager fragmentManager;

    ForwarderFragment forwarder;
    InterfacesFragment interfaces;
    ApplicationsFragment applications;
//    HiPerfFragment hiperf;

    private OnFragmentInteractionListener mListener;

    public HomeActivity() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getChildFragmentManager();

        forwarder = new ForwarderFragment();
        interfaces = new InterfacesFragment();
        applications = new ApplicationsFragment();
        //hiperf = new HiPerfFragment();
        //hiperf.setHome(this);

        fragmentManager.beginTransaction().replace(R.id.subviewLayout, forwarder).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        bottomNavigationView = getView().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.action_forwarder:
                    fragment = forwarder;
                    break;
                case R.id.action_interfaces:
                    fragment = interfaces;
                    break;
                case R.id.action_applications:
                     fragment = applications;
                     break;
                /*
                case R.id.action_hiperf:
                    fragment = hiperf;
                    break;
                */
                default:
                    return false;
            }

            fragmentManager.beginTransaction().replace(R.id.subviewLayout, fragment).commit();
            return false;

        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
    }

}
