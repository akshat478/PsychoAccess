package com.example.mad;

import android.os.Build;
import android.util.Log;
import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;

public class SocketHandler {
    private static Socket mSocket;

    public static synchronized void setSocket(String laptopIp) {
        if (mSocket == null) {
            try {
                // Use 10.0.2.2 for Android Emulator, otherwise use the provided Laptop IP
                String targetIp = isEmulator() ? "10.0.2.2" : laptopIp;
                String url = "http://" + targetIp + ":3000";

                Log.d("SocketHandler", "Initializing socket to: " + url);
                mSocket = IO.socket(url);
            } catch (URISyntaxException e) {
                Log.e("SocketHandler", "Socket initialization failed", e);
            }
        }
    }

    private static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic")
                || Build.FINGERPRINT.contains("vbox")
                || Build.PRODUCT.contains("sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static synchronized Socket getSocket() {
        return mSocket;
    }

    public static synchronized void establishConnection() {
        if (mSocket != null && !mSocket.connected()) {
            Log.d("SocketHandler", "Establishing connection...");
            mSocket.connect();
        }
    }
}