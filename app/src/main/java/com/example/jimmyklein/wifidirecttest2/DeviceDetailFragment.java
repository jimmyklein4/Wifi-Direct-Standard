package com.example.jimmyklein.wifidirecttest2;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jimmyklein.wifidirecttest2.DeviceListFragment.DeviceActionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener, GroupInfoListener {

    public static final String TAG = "DeviceDetailFragment";
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pGroup group;
    private WifiP2pInfo info;
    private ConnectionManager connectionManager;
    private Boolean isFirstSender = false;
    private ArrayList<InetAddress> ipAddresses;
    private long startTime, endTime;
    ProgressDialog progressDialog = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        isFirstSender = true;
                        startTime = System.currentTimeMillis();
                        connectionManager.sendPing(info.groupOwnerAddress, 8988);
                    }
                });

        return mContentView;
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info)
    {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text) + ((info.isGroupOwner) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);

        if (info.groupFormed && info.isGroupOwner) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ConnectionManager tmpManager = new ConnectionManager(8987);
                    InetAddress clientAddr = tmpManager.listen();
                    if(ipAddresses == null){
                        ipAddresses = new ArrayList<>();
                    }
                    if(!ipAddresses.contains(clientAddr)){
                        ipAddresses.add(clientAddr);
                    }
                    tmpManager.closeServerConnection();
                }
            }).start();
            connectionManager = new ConnectionManager(8988);
            InetAddress firstSender = connectionManager.listen();
            if(firstSender !=null && ipAddresses.size()==2) {
                if(firstSender == ipAddresses.get(0)) {
                    connectionManager.sendPing(ipAddresses.get(1), 8988);
                } else {
                    connectionManager.sendPing(ipAddresses.get(0), 8988);
                }
            }
            InetAddress secondSender = connectionManager.listen();
            if(secondSender !=null){
                connectionManager.sendPing(firstSender,8988);
            }
        } else if (info.groupFormed) {
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    connectionManager.sendObject(info.groupOwnerAddress, 8987, new String("test"));
                }
            }).start();
            mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
            connectionManager = new ConnectionManager(8988);
            InetAddress go = connectionManager.listen();
            if(go != null){
                endTime = System.currentTimeMillis();
                if(isFirstSender){
                    view.setText(getResources().getString(R.string.password)+ (endTime-startTime));
                }
            }
        }

        // hide the connect button
    }

    public void onGroupInfoAvailable(final WifiP2pGroup group)
    {
        this.group = group;
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }

    private byte[] getByteArray(){
        byte[] tmp = new byte[64];

        for(int i=0;i<64;i++){
            tmp[i]=0xF;
        }
        return tmp;
    }
}
