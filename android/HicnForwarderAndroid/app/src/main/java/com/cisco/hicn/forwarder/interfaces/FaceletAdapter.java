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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cisco.hicn.forwarder.R;

import java.util.ArrayList;

public class FaceletAdapter extends ArrayAdapter<Facelet> implements View.OnClickListener {

    private ArrayList<Facelet> faceletArrayList;

    @Override
    public void onClick(View view) {

    }

    private static class ViewHolder {
        TextView idTextView;
        TextView netdeviceTextView;
        TextView netdeviceTypeTextView;
        TextView familyTextView;
        TextView faceTypeTextView;
        TextView localAddrTextView;
        TextView localPortTextView;
        TextView remoteAddrTextView;
        TextView remotePortTextView;
        TextView statusTextView;
        TextView errorTextView;
    }

    FaceletAdapter(ArrayList<Facelet> data, Context context) {
        super(context, R.layout.row_item, data);
        this.faceletArrayList = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Facelet facelet = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.idTextView = convertView.findViewById(R.id.id);
            viewHolder.netdeviceTextView = convertView.findViewById(R.id.netdevice);
            viewHolder.netdeviceTypeTextView = convertView.findViewById(R.id.netdevice_type);
            viewHolder.familyTextView = convertView.findViewById(R.id.family);
            viewHolder.faceTypeTextView = convertView.findViewById(R.id.face_type);
            viewHolder.localAddrTextView = convertView.findViewById(R.id.local_addr);
            viewHolder.localPortTextView = convertView.findViewById(R.id.local_port);
            viewHolder.remoteAddrTextView = convertView.findViewById(R.id.remote_addr);
            viewHolder.remotePortTextView = convertView.findViewById(R.id.remote_port);
            viewHolder.statusTextView = convertView.findViewById(R.id.status);
            viewHolder.errorTextView = convertView.findViewById(R.id.error);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        assert facelet != null;
        viewHolder.idTextView.setText(facelet.getId() < 0 ? "-" : "" + facelet.getId());
        viewHolder.netdeviceTextView.setText(facelet.getNetdevice() == null ? "-" : facelet.getNetdevice());
        viewHolder.netdeviceTypeTextView.setText(facelet.getNetdeviceType() == null ? "-" : facelet.getNetdeviceType());
        viewHolder.familyTextView.setText(facelet.getFamily() == null ? "-" : facelet.getFamily());
        viewHolder.faceTypeTextView.setText(facelet.getFaceType() == null ? "-" : facelet.getFaceType());
        viewHolder.localAddrTextView.setText(facelet.getLocalAddr() == null ? "-" : facelet.getLocalAddr());
        viewHolder.localPortTextView.setText(facelet.getLocalPort() == -1 ? "-" : Integer.toString(facelet.getLocalPort()));
        viewHolder.remoteAddrTextView.setText(facelet.getRemoteAddr() == null ? "-" : facelet.getRemoteAddr());
        viewHolder.remotePortTextView.setText(facelet.getRemotePort() == -1 ? "-" : Integer.toString(facelet.getRemotePort()));
        viewHolder.statusTextView.setText(facelet.getStatus() == null ? "-" : facelet.getStatus());
        viewHolder.errorTextView.setText(facelet.isError() == true ? "True" : "False");
        return convertView;
    }

    public ArrayList<Facelet> getItems() {
        return this.faceletArrayList;
    }

}
