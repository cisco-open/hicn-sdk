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

package com.cisco.hicn.forwarder.forwarder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.service.BackendAndroidService;
import com.cisco.hicn.forwarder.service.BackendProxyService;

import com.cisco.hicn.facemgrlibrary.supportlibrary.FacemgrLibrary;
import com.cisco.hicn.hicnforwarderlibrary.supportlibrary.ForwarderLibrary;
import com.cisco.hicn.hproxylibrary.service.ForwarderWrapper;
import com.cisco.hicn.hproxylibrary.service.BackendAndroidVpnService;

import com.cisco.hicn.hproxylibrary.supportlibrary.HProxyLibrary;

public class ForwarderFragment extends Fragment {
    private static int VPN_REQUEST_CODE = 100;

    Messenger mMessenger = null;

    private ForwarderWrapper forwarderWrapper = new ForwarderWrapper() {
        @Override
        public boolean isRunningForwarder() {
            ForwarderLibrary forwarder = ForwarderLibrary.getInstance();
            return forwarder.isRunningForwarder();
        }
    };
    /*
     * This serves to run the BackendProxyService as soon as at least one proxy
     * has been requested
     */
    private boolean mWebexStarted = false;
    private boolean mHttpStarted = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                updateServiceStatus(null);
                return;
            }
            String service = extras.getString("service");
            String color = extras.getString("color");

            View root = getView();
            if (service.equals("forwarder")) {
                updateForwarderStatus(root, color.equals("green"));
            } else if (service.equals("facemgr")) {
                updateFacemgrStatus(root, color.equals("green"));
            }

        }
    };

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mMessenger = new Messenger(service);

            View root = getView();
            if (HProxyLibrary.isHProxyEnabled()) {
                Switch webexSwitch = root.findViewById(R.id.webex_switch);
                webexSwitch.setEnabled(true);
            }
//            Switch httpSwitch = root.findViewById(R.id.http_switch);
//            httpSwitch.setEnabled(true);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // NOTE: You will never receive a call to onServiceDisconnected()
            // for a Service running in the same process that you’re binding
            // from.
            mMessenger = null;
        }
    };


    private OnFragmentInteractionListener mListener;

    public ForwarderFragment() {
    }

    public static ForwarderFragment newInstance(String param1, String param2) {
        ForwarderFragment fragment = new ForwarderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HProxyLibrary.setForwarderService(BackendAndroidService.class);
        HProxyLibrary.setForwarderWrapper(forwarderWrapper);
        FacemgrLibrary.setContext(getContext());

    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(receiver);

        /* We will also unbind from the service if needed */
        getActivity().unbindService(mConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getActivity().registerReceiver(receiver, new IntentFilter(BackendAndroidService.SERVICE_INTENT));
        this.getActivity().registerReceiver(receiver, new IntentFilter(BackendProxyService.SERVICE_INTENT));

        Intent intent = new Intent(getActivity(), BackendProxyService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_forwarder, container, false);
        initView(root);
        return root;
    }

    private void updateForwarderStatus(View root, boolean flag)
    {
        ImageView img = root.findViewById(R.id.img_forwarder);
        if (flag) {
            img.setImageResource(android.R.drawable.presence_online);
        } else {
            img.setImageResource(android.R.drawable.presence_invisible);
        }

    }

    private void updateFacemgrStatus(View root, boolean flag)
    {
        ImageView img = root.findViewById(R.id.img_facemgr);
        if (flag) {
            img.setImageResource(android.R.drawable.presence_online);
        } else {
            img.setImageResource(android.R.drawable.presence_invisible);
        }

    }

    private void updateWebexProxyStatus(View root, boolean flag)
    {
        Switch webexSwitch = root.findViewById(R.id.webex_switch);
        webexSwitch.setOnCheckedChangeListener(null);
        webexSwitch.setChecked(flag);
        webexSwitch.setOnCheckedChangeListener(mWebexCheckedChangeListener);
    }

    private void updateHttpProxyStatus(View root, boolean flag)
    {
//        Switch httpSwitch = root.findViewById(R.id.http_switch);
//        httpSwitch.setOnCheckedChangeListener(null);
//        httpSwitch.setChecked(flag);
        //httpSwitch.setOnCheckedChangeListener(mHttpCheckedChangeListener);
    }

    private void updateServiceStatus(View root)
    {
        if (root == null)
            root = getView();
        if (root == null)
            return;
        updateForwarderStatus(root, ForwarderLibrary.getInstance().isRunningForwarder());
        updateFacemgrStatus(root, FacemgrLibrary.getInstance().isRunningFacemgr());
    }

    CompoundButton.OnCheckedChangeListener mWebexCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                startWebexProxy();
            } else {
                stopWebexProxy();
            }
        }
    };
    //CompoundButton.OnCheckedChangeListener mHttpCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
    //   @Override
    //   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    //       if (isChecked) {
    //          startHttpProxy();
    //      } else {
    //          stopHttpProxy();
    //      }
    //  }
    // };


    private void initView(View root) {
        updateServiceStatus(root);

        /* Those are only updated the first time the view is shown */
        updateWebexProxyStatus(root, HProxyLibrary.getInstance().isRunning());
        //updateHttpProxyStatus(root, HttpProxy.getInstance().isRunningHttpProxy());

        Switch forwarderSwitch = root.findViewById(R.id.forwarder_switch);
        forwarderSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                startHicnServices();
            } else {
                stopHicnServices();
            }
        });
        forwarderSwitch.setChecked(ForwarderLibrary.getInstance().isRunningForwarder());

        if (HProxyLibrary.isHProxyEnabled()) {
            Switch webexSwitch = root.findViewById(R.id.webex_switch);
            webexSwitch.setOnCheckedChangeListener(mWebexCheckedChangeListener);

            /*
             * if the view is initialized before we are bound to an already
             * running service, we gray out the button to avoid any race
             * condition where we would try to send a message with no messenger
             */
            if ((mWebexStarted || mHttpStarted) && mMessenger == null)
                webexSwitch.setEnabled(false);
        } else {
            /* Hide all controls */
            View b = root.findViewById(R.id.ly_webex);
            b.setVisibility(View.GONE);
        }
        //Switch httpSwitch = root.findViewById(R.id.http_switch);
        // httpSwitch.setOnCheckedChangeListener(mHttpCheckedChangeListener);

        /* See previous comment */
        //if ((mWebexStarted || mHttpStarted) && mMessenger == null)
        //    httpSwitch.setEnabled(false);

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

        Intent intent = new Intent(getActivity(), BackendProxyService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        //getActivity().unbindService(mConnection);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void startHicnServices() {
        Intent intent = new Intent(getActivity(), BackendAndroidService.class);
        getActivity().startService(intent);
    }

    private void stopHicnServices() {
        Intent intent = new Intent(getActivity(), BackendAndroidService.class);
        getActivity().stopService(intent);
    }

    private void startWebexProxy() {
        if (!HProxyLibrary.isHProxyEnabled())
            return;
        if (mMessenger != null) {
            HProxyLibrary.setActivity(getActivity());
            Message msg = Message.obtain(null, BackendProxyService.MSG_START_WEBEX_PROXY, 0, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (!mHttpStarted) {
            /* We start the service as soon as one component is running */
            Intent intent = new Intent(getActivity(), BackendProxyService.class);
            getActivity().startService(intent);
        }
    }

    private void stopWebexProxy() {
        if (!HProxyLibrary.isHProxyEnabled())
            return;
        if (mMessenger != null) {
            Message msg = Message.obtain(null, BackendProxyService.MSG_STOP_WEBEX_PROXY, 0, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (!mHttpStarted) {
            /* We stop the service when no more component is running */
            Intent intent = new Intent(getActivity(), BackendProxyService.class);
            getActivity().stopService(intent);
        }
    }

    private void startHttpProxy() {
        if (mMessenger != null) {
            Message msg = Message.obtain(null, BackendProxyService.MSG_START_HTTP_PROXY, 0, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (!mWebexStarted) {
            /* We start the service as soon as one component is running */
            Intent intent = new Intent(getActivity(), BackendProxyService.class);
            getActivity().startService(intent);
        }
    }

    private void stopHttpProxy() {
        if (mMessenger != null) {
            Message msg = Message.obtain(null, BackendProxyService.MSG_STOP_HTTP_PROXY, 0, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (!mWebexStarted) {
            /* We stop the service when no more component is running */
            Intent intent = new Intent(getActivity(), BackendProxyService.class);
            getActivity().stopService(intent);
        }
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(HProxyLibrary.getActivity(), BackendAndroidVpnService.class);
            this.getActivity().startService(intent.setAction(BackendAndroidVpnService.ACTION_CONNECT));
        }
    }

}
