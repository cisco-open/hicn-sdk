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

package com.cisco.hicn.forwarder.applications;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cisco.hicn.forwarder.R;

import java.util.List;

import com.cisco.hicn.hproxylibrary.supportlibrary.PuntingSpec;

public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder>{


    Context context1;
    List<Application> appList;

    public ApplicationsAdapter(Context context, List<Application> list){

        context1 = context;

        appList = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public CardView cardView;
        public ImageView imageView;
        public TextView textView_App_Name;
        public TextView textView_App_Package_Name;
        public Switch sw_app;

        /*
        private final ItemExampleBinding mBinding;

        public ExampleViewHolder(ItemExampleBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(Application item) {
            mBinding.setModel(item);
        }
*/
        public ViewHolder (View view){

            super(view);

            cardView = (CardView) view.findViewById(R.id.card_view);
            imageView = (ImageView) view.findViewById(R.id.img_app_icon);
            textView_App_Name = (TextView) view.findViewById(R.id.tv_app_name);
            textView_App_Package_Name = (TextView) view.findViewById(R.id.tv_app_pkg_name);
            sw_app =  (Switch) view.findViewById(R.id.sw_app);
        }
    }

    @Override
    public ApplicationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View view2 = LayoutInflater.from(context1).inflate(R.layout.card_application,parent,false);

        ViewHolder viewHolder = new ViewHolder(view2);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position){

        ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context1);

        Application app = (Application) appList.get(position);
        //String ApplicationLabelName = apkInfoExtractor.GetAppName(ApplicationPackageName);
        //Drawable drawable = apkInfoExtractor.getAppIconByPackageName(ApplicationPackageName);

        viewHolder.textView_App_Name.setText(app.getName());

        viewHolder.textView_App_Package_Name.setText(app.getPackageName());

        viewHolder.imageView.setImageDrawable(app.getIcon());

        SharedPreferences settings = context1.getSharedPreferences(PuntingSpec.PREFS_NAME, 0);

        boolean punt = settings.getBoolean(app.getPackageName(), app.getPuntByDefault());
        viewHolder.sw_app.setChecked(punt);

        viewHolder.sw_app.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                //SharedPreferences settings;
                SharedPreferences.Editor editor;

                //SharedPreferences settings = context1.getSharedPreferences(PuntingSpec.PREFS_NAME, 0);
                editor = settings.edit();
                editor.putBoolean(app.getPackageName(), bChecked);
                editor.commit();

            }
        });
    }

    @Override
    public int getItemCount(){

        return appList.size();
    }

}
