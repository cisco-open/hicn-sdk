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

package io.fd.hicn.hicntools.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import io.fd.hicn.hicntools.MainActivity;
import io.fd.hicn.hicntools.R;
import io.fd.hicn.hicntools.adapter.HiGetListViewAdapter;
import io.fd.hicn.hicntools.adapter.HiGetOutputListViewElement;
import io.fd.hicn.hicntools.service.Constants;

public class HiGetFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private ArrayList<HiGetOutputListViewElement> higetOutputListViewElementArrayList = new ArrayList<HiGetOutputListViewElement>();
    private HiGetListViewAdapter higetListViewAdapter = null;

    private EditText higetUrlEditText = null;
    private EditText higetDownloadPathEditText = null;
    private Button higetDownloadButton = null;
    private Button higetStopButton = null;
    private ListView higetResultListView = null;

    //Preferences
    private SharedPreferences higetSharedPreferences = null;


    private String TAG = "HiGet";

    public static HiGetFragment newInstance(int index) {
        HiGetFragment fragment = new HiGetFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Context context = getActivity();
        higetSharedPreferences = context.getSharedPreferences(
                getString(R.string.higet_shared_preferences), Context.MODE_PRIVATE);
        View root = inflater.inflate(R.layout.fragment_higet, container, false);
        initHiGetFragment(root);
        return root;
    }

    private void initHiGetFragment(View root) {
        //Get components by IDs

        higetUrlEditText = root.findViewById(R.id.higet_url_edittext);
        higetDownloadPathEditText = root.findViewById(R.id.higet_downlaod_path_edittext);
        higetDownloadButton = root.findViewById(R.id.higet_download_button);
        higetStopButton = root.findViewById(R.id.higet_stop_button);
        higetResultListView = root.findViewById(R.id.higet_results_listview);

        higetUrlEditText.setText(higetSharedPreferences.getString(getString(R.string.higet_url_key), Constants.DEFAULT_HIGET_URL));
        higetDownloadPathEditText.setText(higetSharedPreferences.getString(getString(R.string.higet_download_path_key), Constants.DEFAULT_HIGET_DOWNLOAD_PATH));

        higetListViewAdapter = new HiGetListViewAdapter(getActivity(), higetOutputListViewElementArrayList);
        higetResultListView = root.findViewById(R.id.higet_results_listview);
        higetResultListView.setAdapter(higetListViewAdapter);

        higetDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                higetUrlEditText.setEnabled(false);
                higetDownloadPathEditText.setEnabled(false);
                higetDownloadButton.setEnabled(false);
                higetStopButton.setEnabled(true);
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor higetEditor = higetSharedPreferences.edit();
                        higetEditor.putString(getString(R.string.higet_url_key), higetUrlEditText.getText().toString());
                        higetEditor.putString(getString(R.string.higet_download_path_key), higetDownloadPathEditText.getText().toString());
                        higetEditor.commit();
                        ((MainActivity)getActivity()).setDisableSwipe(2);
                        String[] urlSplitted = higetUrlEditText.getText().toString().split(File.separator);
                        File downloadPath = new File(higetDownloadPathEditText.getText().toString());
                        if (!downloadPath.exists()) {
                            downloadPath.mkdirs();
                        }

                        byte[] content = downloadFile(higetUrlEditText.getText().toString());
                        if (content.length > 0) {
                            String nameFile = writeToFile(content, higetDownloadPathEditText.getText().toString(), urlSplitted[urlSplitted.length - 1]);
                            higetOutputListViewElementArrayList.add(0, new HiGetOutputListViewElement(higetUrlEditText.getText().toString(), higetDownloadPathEditText.getText().toString(), nameFile, md5(content), content.length));
                        } else {
                            higetOutputListViewElementArrayList.add(0, new HiGetOutputListViewElement(higetUrlEditText.getText().toString(), Constants.HIGET_DASH, Constants.HIGET_DASH, Constants.HIGET_DASH, 0));
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                higetListViewAdapter.notifyDataSetChanged();
                                higetUrlEditText.setEnabled(true);
                                higetDownloadPathEditText.setEnabled(true);
                                higetDownloadButton.setEnabled(true);
                                higetStopButton.setEnabled(false);
                            }
                        });
                        ((MainActivity)getActivity()).enableSwipe();
                    }
                });
            }
        });
        higetStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                stopDownload();
            }
        });


    }


    private String writeToFile(byte[] content, String path, String nameFile) {
        try {
            Log.v("name", nameFile);
            nameFile = checkGenerateNameFile(path, nameFile.trim());
            Log.v("name", nameFile);
            PrintStream out = new PrintStream(new FileOutputStream(path + File.separator + nameFile));

            out.write(content, 0, content.length);
        } catch (FileNotFoundException e) {
            Log.v(TAG, e.toString());
        }
        return nameFile;
    }

    private String checkGenerateNameFile(String path, String nameFile) {
        String newNameFile = nameFile.trim();

        File file;
        int count = 1;
        do {
            file = new File(path + File.separator + newNameFile);


            if (file.exists()) {

                newNameFile = nameFile.trim() + Constants.HIGET_UNDERSCORE + Integer.toString(count);
                count++;
            }
        } while (file.exists());
        return newNameFile;
    }

    public static final String md5(final byte[] s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s);
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public native byte[] downloadFile(String path);

    public native void stopDownload();

}