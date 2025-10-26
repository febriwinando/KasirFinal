package tech.id.kasir.utility.btt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothHelper {
    private static final String PREFS_NAME = "kasir_prefs";
    private static final String PREF_LAST_DEVICE = "last_device_address";
    private static final String TAG = "BluetoothHelper";

    public static BluetoothSocket connectDevice(Activity context, BluetoothAdapter adapter,
                                                String address, String name, Handler handler, UUID uuid) {
        BluetoothSocket socket = null;
        try {
            BluetoothDevice device = adapter.getRemoteDevice(address);
            socket = createSocket(context, device, uuid);
            socket.connect();

            // Simpan alamat terakhir yang berhasil
            saveLastDevice(context, address);

            handler.obtainMessage(UtilBluetooth.CONNECTING_STATUS, 1, -1, name).sendToTarget();
            return socket;
        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
            handler.obtainMessage(UtilBluetooth.CONNECTING_STATUS, -1, -1).sendToTarget();
            try { if (socket != null) socket.close(); } catch (IOException ignored) {}
            return null;
        }
    }

    private static BluetoothSocket createSocket(Activity context, BluetoothDevice device, UUID uuid) throws IOException {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
            }
        }
        try {
            Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, uuid);
        } catch (Exception e) {
            Log.e(TAG, "Could not create insecure socket", e);
            return device.createRfcommSocketToServiceRecord(uuid);
        }
    }

    private static void saveLastDevice(Activity context, String address) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        prefs.edit().putString(PREF_LAST_DEVICE, address).apply();
    }

    public static String getLastDevice(Activity context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        return prefs.getString(PREF_LAST_DEVICE, null);
    }

    public static boolean autoConnectIfAvailable(Activity context, BluetoothAdapter adapter, Handler handler, UUID uuid) {
        String lastAddress = getLastDevice(context);
        if (lastAddress != null) {
            BluetoothSocket socket = connectDevice(context, adapter, lastAddress, "Auto Connect", handler, uuid);
            return socket != null && socket.isConnected();
        }
        return false;
    }
}
