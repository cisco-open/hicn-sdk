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

package com.cisco.hicn.forwarder.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cisco.hicn.forwarder.MainActivity;
import com.cisco.hicn.forwarder.R;

import com.cisco.hicn.forwarder.utility.Constants;

import com.cisco.hicn.common.NetworkServiceHelper;
import com.cisco.hicn.common.SocketBinder;
import com.cisco.hicn.facemgrlibrary.supportlibrary.FacemgrLibrary;
import com.cisco.hicn.hicnforwarderlibrary.supportlibrary.ForwarderLibrary;


public class BackendAndroidService extends Service {
    /* The key and related Intent used to communicate with the service's users */
    public static final String SERVICE_INTENT = "com.cisco.hicn.forwarder.BackendProxyServiceIntentKey";
    private final static String TAG = "BackendService";

    private static Thread sForwarderThread = null;
    private static Thread sFacemgrThread = null;

    private NetworkServiceHelper mNetService = new NetworkServiceHelper();
    private SocketBinder mSocketBinder = new SocketBinder();

    /**
     * Handler of incoming messages from clients.
     */
    static class IncomingHandler extends Handler {
        private Context applicationContext;

        IncomingHandler(Context context) {
            applicationContext = context.getApplicationContext();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /* ... */
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service (or null).
     */
    @Override
    public IBinder onBind(Intent intent) {
        startForwarderFaceManager();
        mMessenger = new Messenger(new IncomingHandler(this));
        return mMessenger.getBinder();
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    Messenger mMessenger;

    private int capacity;
    private int logLevel;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForwarderFaceManager();
        return Service.START_STICKY;
    }

    private void broadcast(String service, String color)
    {
        Intent broadcast = new Intent(SERVICE_INTENT);
        broadcast.putExtra("service", service);
        broadcast.putExtra("color", color);
        sendBroadcast(broadcast);
    }

    private void startForwarderFaceManager()
    {
        ForwarderLibrary forwarder = ForwarderLibrary.getInstance();

        if (!forwarder.isRunningForwarder()) {
            Log.d(TAG, "Starting Backend Service");
            mNetService.init(this, mSocketBinder);
            getCapacity();
            getLogLevel();
            startBackend();

            Log.i(TAG, "BackendAndroid started");

        } else {
            Log.d(TAG, "Forwarder already running.");
        }
    }

    @Override
    public void onDestroy() {

        FacemgrLibrary facemgr = FacemgrLibrary.getInstance();
        Log.d(TAG, "Destroying Forwarder");
        if (facemgr.isRunningFacemgr()) {
            facemgr.stopFacemgr();
            broadcast("facemgr", "red");
        }

        ForwarderLibrary forwarder = ForwarderLibrary.getInstance();
        if (forwarder.isRunningForwarder()) {
            forwarder.stopForwarder();
            broadcast("forwarder", "red");
        }
        mNetService.clear();

        stopForeground(true);


        super.onDestroy();
    }

    private void getCapacity() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.capacity = Integer.parseInt(sharedPreferences.getString(getString(R.string.cache_size_key), getString(R.string.default_cache_size)));
    }

    private void getLogLevel() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.logLevel = Integer.parseInt(sharedPreferences.getString(getString(R.string.forwarder_log_level_key), getString(R.string.forwarder_default_log_level)));
    }

    protected Runnable mForwarderRunner = () -> {
        ForwarderLibrary forwarder = ForwarderLibrary.getInstance();
        forwarder.setSocketBinder(mSocketBinder);
        broadcast("forwarder", "green");
        forwarder.startForwarder(capacity, logLevel);
        broadcast("facemgr", "red");
    };

    protected Runnable mFacemgrRunner = () -> {
        FacemgrLibrary facemgr = FacemgrLibrary.getInstance();
        broadcast("facemgr", "green");
        facemgr.startFacemgr();
        broadcast("facemgr", "red");
    };

    private void startBackend() {
        String NOTIFICATION_CHANNEL_ID = "12345";
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder notificationBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent activity = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentTitle("ForwarderAndroid").setContentText("ForwarderAndroid").setOngoing(true).setContentIntent(activity);
            notification = notificationBuilder.build();
        } else {
            notification = new Notification.Builder(this)
                    .setContentTitle("ForwarderAndroid")
                    .setContentText("ForwarderAndroid")
                    .build();
        }

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "ForwarderAndroid", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("ForwarderAndroid");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

        }

        startForeground(Constants.FOREGROUND_SERVICE, notification);


        ForwarderLibrary forwarder = ForwarderLibrary.getInstance();
        if (!forwarder.isRunningForwarder()) {
            sForwarderThread = new Thread(mForwarderRunner, "BackendAndroid");
            sForwarderThread.start();
        }

        FacemgrLibrary facemgr = FacemgrLibrary.getInstance();
        if (!facemgr.isRunningFacemgr()) {
            sFacemgrThread = new Thread(mFacemgrRunner, "BackendAndroid");
            sFacemgrThread.start();
        }
    }

}
