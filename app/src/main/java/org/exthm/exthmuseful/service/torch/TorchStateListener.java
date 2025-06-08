package org.exthm.exthmuseful.service.torch;

public interface TorchStateListener {
    void onTorchStateChanged(boolean isOn);
    void onTorchUnavailable(String cameraId);
}