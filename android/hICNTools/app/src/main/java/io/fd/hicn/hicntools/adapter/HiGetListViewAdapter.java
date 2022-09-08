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

package io.fd.hicn.hicntools.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import io.fd.hicn.hicntools.R;
import io.fd.hicn.hicntools.service.Constants;

public class HiGetListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HiGetOutputListViewElement> higetOutputListViewElementArrayList;
    private static LayoutInflater higetInflater = null;

    public HiGetListViewAdapter(Context context, ArrayList<HiGetOutputListViewElement> higetOutputListViewElementArrayList) {
        this.context = context;
        this.higetOutputListViewElementArrayList = higetOutputListViewElementArrayList;
        higetInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return higetOutputListViewElementArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return higetOutputListViewElementArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = higetInflater.inflate(R.layout.list_view_row, null);

        TextView higetListViewUrlTextview = view.findViewById(R.id.higet_list_view_url_textview);
        higetListViewUrlTextview.setText(higetOutputListViewElementArrayList.get(position).getHigetUrl());
        TextView higetSavedPathTextView = view.findViewById(R.id.higet_saved_path_textview);
        higetSavedPathTextView.setText(higetOutputListViewElementArrayList.get(position).getHigetSavedPath() + File.separator + higetOutputListViewElementArrayList.get(position).getHigetNameFile());
        TextView higetMd5TextView = view.findViewById(R.id.higet_md5_textview);
        higetMd5TextView.setText(higetOutputListViewElementArrayList.get(position).getHigetMd5());
        TextView higetSizeTextView = view.findViewById(R.id.higet_size_textview);
        higetSizeTextView.setText(Integer.toString(higetOutputListViewElementArrayList.get(position).getHigetSize()));
        TextView higetDateTextView = view.findViewById(R.id.higet_date_textview);
        higetDateTextView.setText(higetOutputListViewElementArrayList.get(position).getHigetDateString(Constants.HIGET_FORMAT_DATA));
        return view;
    }
}