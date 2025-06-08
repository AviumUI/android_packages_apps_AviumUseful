package org.exthm.exthmuseful.service.music;

public interface HeadsetStateListener {
    void onHeadsetConnected(String deviceName, boolean isBluetooth);
    void onHeadsetDisconnected(boolean isBluetooth);
}