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

package com.cisco.hicn.forwarder.interfaces;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.utility.Constants;
import com.google.common.collect.Sets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import com.cisco.hicn.facemgrlibrary.supportlibrary.FacemgrLibrary;

public class InterfacesFragment extends Fragment {
    private FrameLayout frameLayout;
    private Timer timer;
    private final String FACEMGR_TAG = "FACEMGR";
    private ArrayList<Facelet> faceletsArrayList = new ArrayList<>();
    private ListView listView;
    private SharedPreferences sharedPreferences;
    private static FaceletAdapter faceletAdapter;
    private int bottomSize = 0;

    private HashMap<String, Facelet> faceletHashMap = new HashMap<>();


    private OnFragmentInteractionListener mListener;

    public InterfacesFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_interfaces, container, false);
        listView = root.findViewById(R.id.list);

        faceletAdapter = new FaceletAdapter(faceletsArrayList, getContext());
        faceletAdapter.setNotifyOnChange(false); // we manually call notifyDataSetChanged after clear or batch additions
        listView.setAdapter(faceletAdapter);
        frameLayout = root.findViewById(R.id.interfaces_framelayout);
        CheckBox interfaceShowallCheckbox = root.findViewById(R.id.interface_showall_checkbox);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        interfaceShowallCheckbox.setChecked(sharedPreferences.getBoolean(getString(R.string.interface_showall_key), Boolean.parseBoolean(getString(R.string.default_interface_showall))));
        interfaceShowallCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.interface_showall_key), b);
            editor.commit();
        });
        return root;

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

    public void setBottomSize(int bottomSize) {
        this.bottomSize = bottomSize;

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class RetrieveInterfaceTask extends TimerTask {

        private int serial;


        RetrieveInterfaceTask(int param) {
            this.serial = param;
        }

        @Override
        public void run() {
            /*
             * Note: this code never clears the list of facelets unless the
             * face manager is found in a stopped state: this is okay as soon as
             * facelets are never removed in the face manager, which is
             * indeed the case.
             */

            HashMap<String, Facelet> faceletHashMapLocal = new HashMap<>();
            if (FacemgrLibrary.getInstance().isRunningFacemgr()) {
                boolean showAll = sharedPreferences.getBoolean(getString(R.string.interface_showall_key), Boolean.parseBoolean(getString(R.string.default_interface_showall)));
                try {
                    String listFaceletsString = FacemgrLibrary.getInstance().getListFacelets();
                    Log.d(FACEMGR_TAG, listFaceletsString);
                    JSONObject listFaceletsJsonObject = new JSONObject(listFaceletsString);
                    JSONArray listFaceletsJsonArray = listFaceletsJsonObject.getJSONArray("facelets");
                    for (int i = 0; i < listFaceletsJsonArray.length(); i++) {
                        int id = getJSONInt(listFaceletsJsonArray.getJSONObject(i), Constants.ID);
                        String netdevice = getJSONString(listFaceletsJsonArray.getJSONObject(i), Constants.NETDEVICE);
                        String netdeviceType = getJSONString(listFaceletsJsonArray.getJSONObject(i), Constants.NETDEVICE_TYPE);
                        String family = getJSONString(listFaceletsJsonArray.getJSONObject(i), Constants.FAMILY);
                        String localAddr = getJSONString(listFaceletsJsonArray.getJSONObject(i), Constants.LOCAL_ADDR);
                        int localPort = getJSONInt(listFaceletsJsonArray.getJSONObject(i), Constants.LOCAL_PORT);
                        String remoteAddr = getJSONString(listFaceletsJsonArray.getJSONObject(i), Constants.REMOTE_ADDR);
                        int remotePort = getJSONInt(listFaceletsJsonArray.getJSONObject(i), Constants.REMOTE_PORT);
                        String faceType = getJSONString(listFaceletsJsonArray.getJSONObject(i), Constants.FACE_TYPE);
                        String status = getJSONString(listFaceletsJsonArray.getJSONObject(i), Constants.STATUS);
                        boolean error = getJSONBool(listFaceletsJsonArray.getJSONObject(i), Constants.ERROR);
                        if (!showAll) {
                            if (status.equals("CLEAN")) {
                                sharedPreferences.getBoolean(getString(R.string.interface_showall_key), Boolean.getBoolean(getString(R.string.default_interface_showall)));
                                faceletHashMapLocal.put(netdevice + family + id, new Facelet(id, netdevice, netdeviceType, family, localAddr, localPort, remoteAddr, remotePort, faceType, status, error));
                            }
                        } else {
                            sharedPreferences.getBoolean(getString(R.string.interface_showall_key), Boolean.getBoolean(getString(R.string.default_interface_showall)));
                            faceletHashMapLocal.put(netdevice + family + id , new Facelet(id, netdevice, netdeviceType, family, localAddr, localPort, remoteAddr, remotePort, faceType, status, error));
                        }
                    }

                } catch (JSONException e) {
                    Log.e(FACEMGR_TAG, "Impossible to parse the facemgr output");
                }
            } else {
                getActivity().runOnUiThread(() ->
                {
                    faceletAdapter.clear();
                    faceletAdapter.notifyDataSetChanged();
                });
            }

            if (isAdded() && isVisible()) {

                getActivity().runOnUiThread(() ->
                {
                    Set<String> sources = faceletHashMap.keySet();
                    Set<String> target = faceletHashMapLocal.keySet();
                    Set<String> differences = Sets.difference(sources, target);

                    Set<String> copyDifferences = differences.stream().collect(Collectors.toSet());

                    for (String key : copyDifferences) {
                        faceletAdapter.remove(faceletHashMap.get(key));

                        faceletHashMap.remove(key);
                    }
                    for (String key : target) {
                        if (faceletHashMap.containsKey(key)) {
                            Facelet facelet = faceletHashMap.get(key);
                            Facelet faceletLocal = faceletHashMapLocal.get(key);
                            facelet.setNetdeviceType(faceletLocal.getNetdeviceType());
                            facelet.setFamily(faceletLocal.getFamily());
                            facelet.setLocalAddr(faceletLocal.getLocalAddr());
                            facelet.setLocalPort(faceletLocal.getLocalPort());
                            facelet.setRemoteAddr(faceletLocal.getRemoteAddr());
                            facelet.setRemotePort(faceletLocal.getRemotePort());
                            facelet.setFaceType(faceletLocal.getFaceType());
                            facelet.setStatus(faceletLocal.getStatus());
                            facelet.setError(faceletLocal.isError());
                        } else {
                            Facelet faceletLocal = faceletHashMapLocal.get(key);
                            faceletHashMap.put(faceletLocal.getNetdevice() + faceletLocal.getFamily() + faceletLocal.getId(), faceletLocal);
                            faceletAdapter.add(faceletLocal);
                        }
                    }
                    faceletAdapter.notifyDataSetChanged();
                });
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        timer = new Timer();
        timer.schedule(new RetrieveInterfaceTask(0), 0, 1000);

    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    private String getJSONString(JSONObject jsonObject, String name) {
        String value = null;
        try {
            value = jsonObject.getString(name);
        } catch (JSONException e) {
            Log.d(FACEMGR_TAG, "Impossible to parse " + name);
        }
        return value;
    }

    private int getJSONInt(JSONObject jsonObject, String name) {
        int value = -1;
        try {
            value = jsonObject.getInt(name);
        } catch (JSONException e) {
            Log.d(FACEMGR_TAG, "Impossible to parse " + name);
        }
        return value;
    }

    private boolean getJSONBool(JSONObject jsonObject, String name) {
        boolean value = false;
        try {
            value = jsonObject.getBoolean(name);
        } catch (JSONException e) {
            Log.d(FACEMGR_TAG, "Impossible to parse " + name);
        }
        return value;
    }

}
