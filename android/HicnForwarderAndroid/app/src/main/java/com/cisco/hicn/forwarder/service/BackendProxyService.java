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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.cisco.hicn.hproxylibrary.R;
import com.cisco.hicn.hproxylibrary.service.BackendAndroidVpnService;
import com.cisco.hicn.hproxylibrary.service.ProxyBackend;
import com.cisco.hicn.hproxylibrary.service.ProxyBackendNative;
import com.cisco.hicn.hproxylibrary.supportlibrary.HProxyLibrary;

public class BackendProxyService extends Service implements Handler.Callback {
    /* The key and related Intent used to communicate with the service's users */
    public static final String SERVICE_INTENT = "com.cisco.hicn.forwarder.BackendProxyServiceIntentKey";
    private Intent broadcast = new Intent(SERVICE_INTENT);

        private static int VPN_REQUEST_CODE = 100;


    private static final String TAG = "BackendProxyService";

    private static Thread sHttpProxyThread = null;
    private ProxyBackend mProxyBackend = null;

    private Handler mHandler;

    /**
     * Command to the service to display a message
     */
    public static final int MSG_START_WEBEX_PROXY = 1;
    public static final int MSG_STOP_WEBEX_PROXY = 2;
    public static final int MSG_START_HTTP_PROXY = 3;
    public static final int MSG_STOP_HTTP_PROXY = 4;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        private Context applicationContext;

        IncomingHandler(Context context) {
            applicationContext = context.getApplicationContext();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_WEBEX_PROXY:
                    startWebexProxy();
                    break;
                case MSG_STOP_WEBEX_PROXY:
                    stopWebexProxy();
                    break;
                //case MSG_START_HTTP_PROXY:
                //    startHttpProxy();
                //    break;
                //case MSG_STOP_HTTP_PROXY:
                //    stopHttpProxy();
                //    break;
                default:
                    //Toast.makeText(applicationContext, "Unhandled message", Toast.LENGTH_SHORT).show();
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
        mMessenger = new Messenger(new IncomingHandler(this));
        return mMessenger.getBinder();
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    Messenger mMessenger;

    private String prefix;
    private int listeningPort;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
         * Eventually perform default actions here...
         */
        return Service.START_STICKY;
    }

    private void startWebexProxy()
    {
        Log.i("HProxyBackend", "Starting");
        if (!HProxyLibrary.isHProxyEnabled())
            return;

        if (HProxyLibrary.getInstance().isRunning())
            return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean force_vpn = sharedPreferences.getBoolean(getString(R.string.pref_hproxy_force_vpn_key),
                Boolean.parseBoolean(getString(R.string.pref_hproxy_force_vpn_default)));

        //if (ProxyBackend.getHicnService()) { // A11v1

        /*
         * public static boolean isServiceAvailable()
         *
         * This API gives information, if the SemTunService is available on
         * device (true – for available and vice versa). This API should be
         * called first, to know if the service is available or not.
         */
        if (!force_vpn && HProxyLibrary.isStunServiceAvailable()) { // A11v3
            /* Start native */
            Log.i("HProxyBackend", "Native");
            //mProxyBackend = ProxyBackend.getProxyBackend(this); // A11v1
            mProxyBackend = new ProxyBackendNative(this); // A11v3
            mProxyBackend.connect();
        } else {
            /*
             * Start the VPN fallback if needed, in that case, the ProxyBackend will
             * be started from the BackendAndroidVpnService
             */
            Log.i("HProxyBackend", "Not native");
            Intent intentProxy = BackendAndroidVpnService.prepare(getApplicationContext());//dummyFragment.getActivity());

            if (intentProxy != null) {
                HProxyLibrary.getActivity().startActivityForResult(intentProxy, VPN_REQUEST_CODE);
            } else {

                Intent intent = new Intent(HProxyLibrary.getActivity(), BackendAndroidVpnService.class);
                HProxyLibrary.getActivity().startService(intent.setAction(BackendAndroidVpnService.ACTION_CONNECT));
                /*
                Intent service = new Intent(getApplicationContext(), BackendAndroidVpnService.class).setAction(BackendAndroidVpnService.ACTION_CONNECT);
                startService(service);
                */
            }
        }

        /* Broadcast intent to inform about service status change */
        sendBroadcast(broadcast);
    }

    private void stopWebexProxy()
    {
        if (!HProxyLibrary.isHProxyEnabled())
            return;

        if (!HProxyLibrary.getInstance().isRunning())
            return;

        /* Stop VPN */

        // XXX we should in fact remember the status of the VPN when it was started, and not the current status.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean force_vpn = sharedPreferences.getBoolean(getString(R.string.pref_hproxy_force_vpn_key), Boolean.parseBoolean(getString(R.string.pref_hproxy_force_vpn_default)));

        if (!force_vpn && HProxyLibrary.isStunServiceAvailable()) { // A11v3
        //if (ProxyBackend.getHicnService()) { // A11v1
            /* Disconnect native */
            mProxyBackend.disconnect();
        } else {
            /* Stops VPN */
            //activity.startService(getServiceIntent().setAction(BackendAndroidVpnService.ACTION_DISCONNECT));
            Intent service = new Intent(getApplicationContext(), BackendAndroidVpnService.class).setAction(BackendAndroidVpnService.ACTION_DISCONNECT);
            startService(service);
        }

        /* Broadcast intent to inform about service status change */
        sendBroadcast(broadcast);
    }


    @Override
    public void onDestroy() {
        /*
         * We can do this as those function check the current thread status. In the case of the
         * Webex proxy, we might not stop it systematically as this might create an unwanted
         * ProxyBackend, and thus trigger VPN for nothing
         */
        stopWebexProxy();
        //stopHttpProxy();

        // stopForeground(true);
    }

    @Override
    public boolean handleMessage(Message message) {
        Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        //if (message.what != R.string.hproxy_disconnected) {
        //updateForegroundNotification(message.what);
        //}
        return true;
    }

}
