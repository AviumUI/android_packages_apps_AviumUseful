/*
 * Copyright (C) 2025-2026 The AviumUI Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.avium.aviumuseful.service.music;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class EarConnectListener {
    private static final String TAG = "EarConnectListener";
    private final Context context;
    private final HeadsetStateListener listener;
    private final AudioManager audioManager;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothHeadset bluetoothHeadset;
    private boolean isListening = false;

    public EarConnectListener(Context context, HeadsetStateListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        } else {
            Log.w(TAG, "BLUETOOTH_CONNECT permission not granted, Bluetooth features might be limited.");
            this.bluetoothAdapter = null;
        }
    }

    private final BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                String name = intent.getStringExtra("name");

                if (state == 1) {
                    Log.i(TAG, "Wired headset connected: " + (name != null ? name : "Unknown"));
                    if (listener != null) {
                        listener.onHeadsetConnected(name != null ? name : "有线耳机", false);
                    }
                } else if (state == 0) {
                    Log.i(TAG, "Wired headset disconnected.");
                    if (listener != null) {
                        listener.onHeadsetDisconnected(false);
                    }
                }
            }
        }
    };

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = "蓝牙耳机";
                if (device != null) {
                    try {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            deviceName = device.getName() != null ? device.getName() : "未知蓝牙设备";
                        } else {
                            Log.w(TAG, "BLUETOOTH_CONNECT permission not granted for getting device name.");
                        }
                    } catch (SecurityException e) {
                        Log.e(TAG, "SecurityException getting Bluetooth device name: " + e.getMessage());
                    }
                }


                if (state == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Bluetooth headset connected: " + deviceName);
                    if (listener != null) {
                        listener.onHeadsetConnected(deviceName, true);
                    }
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Bluetooth headset disconnected: " + deviceName);
                    if (listener != null) {
                        listener.onHeadsetDisconnected(true);
                    }
                }
            }
        }
    };

    private final BluetoothProfile.ServiceListener bluetoothProfileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = (BluetoothHeadset) proxy;
                Log.i(TAG, "Bluetooth Headset profile service connected.");
                // You could check for already connected devices here if needed
                // List<BluetoothDevice> connectedDevices = bluetoothHeadset.getConnectedDevices();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null;
                Log.i(TAG, "Bluetooth Headset profile service disconnected.");
            }
        }
    };

    public boolean startListening() {
        if (isListening) {
            Log.w(TAG, "Listener already started.");
            return true;
        }

        // Register for wired headset plug events
        IntentFilter headsetFilter = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        context.registerReceiver(headsetPlugReceiver, headsetFilter);
        Log.i(TAG, "Registered wired headset receiver.");

        // Register for Bluetooth headset events
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            // Get proxy for Bluetooth Headset profile
            boolean gotProfile = bluetoothAdapter.getProfileProxy(context, bluetoothProfileListener, BluetoothProfile.HEADSET);
            if (!gotProfile) {
                Log.w(TAG, "Could not get Bluetooth Headset profile proxy.");
            }

            IntentFilter bluetoothFilter = new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            // Optional: Also listen for adapter state changes if you want to re-register on BT ON
            // bluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(bluetoothStateReceiver, bluetoothFilter);
            Log.i(TAG, "Registered Bluetooth headset receiver.");
        } else {
            Log.w(TAG, "Bluetooth adapter not available or not enabled. Skipping Bluetooth listener registration.");
        }
        isListening = true;
        return true;
    }

    public void stopListening() {
        if (!isListening) {
            Log.w(TAG, "Listener not active or already stopped.");
            return;
        }
        try {
            context.unregisterReceiver(headsetPlugReceiver);
            Log.i(TAG, "Unregistered wired headset receiver.");
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Wired headset receiver was not registered: " + e.getMessage());
        }

        if (bluetoothAdapter != null) {
            try {
                context.unregisterReceiver(bluetoothStateReceiver);
                Log.i(TAG, "Unregistered Bluetooth headset receiver.");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Bluetooth headset receiver was not registered: " + e.getMessage());
            }
            if (bluetoothHeadset != null) {
                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
                bluetoothHeadset = null;
            }
        }
        isListening = false;
    }

}