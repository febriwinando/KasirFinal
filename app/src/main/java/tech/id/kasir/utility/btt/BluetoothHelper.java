package tech.id.kasir.utility.btt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import tech.id.kasir.utility.btt.BluetoothPermissionManager;

public class BluetoothHelper {

    public static BluetoothSocket bluetoothSocket;
    public static OutputStream outputStreamer;

    private static final String TAG = "BluetoothHelper";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static void autoReconnect(Activity activity) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(activity, "Bluetooth belum aktif", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🟩 Pastikan permission sudah diberikan
        BluetoothPermissionManager.ensurePermissions(activity, 1001);

        // Jika permission belum diberikan, jangan lanjut
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permission BLUETOOTH_CONNECT belum diberikan");
                return;
            }
        }

        SharedPreferences prefs = activity.getSharedPreferences("kasir_prefs", Context.MODE_PRIVATE);
        String lastAddress = prefs.getString("last_device_address", null);

        if (lastAddress == null) {
            Log.d(TAG, "Tidak ada perangkat terakhir yang tersimpan");
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(lastAddress);

        // 🟩 Aman dipanggil karena permission sudah dicek di atas
        String deviceName = (device.getName() != null) ? device.getName() : "Perangkat Tidak Dikenal";
        Log.d(TAG, "Mencoba reconnect ke: " + deviceName + " (" + lastAddress + ")");

        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
                bluetoothSocket.connect();

                if (bluetoothSocket.isConnected()) {
                    outputStreamer = bluetoothSocket.getOutputStream();
                    Log.d(TAG, "Reconnect berhasil ke " + deviceName);
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity, "Terhubung ke " + deviceName, Toast.LENGTH_SHORT).show());
                }

            } catch (IOException e) {
                Log.e(TAG, "Gagal reconnect: " + e.getMessage());
                try {
                    if (bluetoothSocket != null) bluetoothSocket.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Gagal menutup socket: " + ex.getMessage());
                }
            }
        }).start();
    }
}
