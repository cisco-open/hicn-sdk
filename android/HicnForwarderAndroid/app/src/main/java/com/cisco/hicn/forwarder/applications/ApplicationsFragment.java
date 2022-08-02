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

package com.cisco.hicn.forwarder.applications;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cisco.hicn.forwarder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cisco.hicn.hproxylibrary.supportlibrary.HProxyLibrary;
import com.cisco.hicn.hproxylibrary.supportlibrary.PuntingSpec;

public class ApplicationsFragment extends Fragment {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager recyclerViewLayoutManager;
    public CheckBox cb_supported_apps;

    private OnFragmentInteractionListener mListener;

    public ApplicationsFragment() {
    }

    public static ApplicationsFragment newInstance(String param1, String param2) {
        ApplicationsFragment fragment = new ApplicationsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_applications, container, false);

        cb_supported_apps = (CheckBox) root.findViewById(R.id.cb_supported_apps);

        SharedPreferences settings = getActivity().getSharedPreferences(PuntingSpec.PREFS_NAME, 0);
        boolean supported_apps = settings.getBoolean("supported_apps_only", true);
        cb_supported_apps.setChecked(supported_apps);
        cb_supported_apps.setEnabled(false);

        cb_supported_apps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                //SharedPreferences settings;
                SharedPreferences.Editor editor;
                //SharedPreferences settings = context1.getSharedPreferences(PuntingSpec.PREFS_NAME, 0);
                editor = settings.edit();
                if (bChecked)
                    editor.putBoolean("supported_apps_only", bChecked);
                editor.commit();

                // XXX refilter recycler view
                // XXX see
            }
        });

        recyclerView = (RecyclerView) root.findViewById(R.id.rv_applications);
        recyclerView.setHasFixedSize(true);
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        //assigning adapter to RecyclerView

        //Set<String> punted_set = new HashSet<>();
        Map<String, PuntingSpec> punted_map = new HashMap<>();
        for (PuntingSpec p : HProxyLibrary.getInstance().getPuntingSpecs()) {
            punted_map.put(p.androidPackage, p);
            //punted_set.add(p.androidPackage);
        }

        List<Application> pkg_list = new ApkInfoExtractor(getActivity()).getApplications();
        List<Application> pkg_filtered_list = new ArrayList<Application>();
        for(Application app : pkg_list) {
            if (!punted_map.containsKey(app.getPackageName()))
                continue;
            PuntingSpec p = punted_map.get(app.getPackageName());

            app.setPuntByDefault(p.puntByDefault);
            pkg_filtered_list.add(app);
        }
        //List<String> pkg_filtered_list = new ArrayList<String>(pkg_filtered_set);
        adapter = new ApplicationsAdapter(getActivity(), pkg_filtered_list);


        //adapter.setNotifyOnChange(false); // we manually call notifyDataSetChanged after clear or batch additions

        recyclerView.setAdapter(adapter);
        //adapter.notifyDataSetChanged();

        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.app_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // Here is where we are going to implement the filter logic
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

 */
}
