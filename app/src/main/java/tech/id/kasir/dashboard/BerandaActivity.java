package tech.id.kasir.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

import tech.id.kasir.MainActivity;
import tech.id.kasir.R;
import tech.id.kasir.network.NetworkUtil;
import tech.id.kasir.pengaturan.PengaturanPerangkatActivity;
import tech.id.kasir.utility.btt.BluetoothHelper;

public class BerandaActivity extends AppCompatActivity {
    CardView cvOrder, cvSetting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        hideSystemUI();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        hideSystemUI();
        setContentView(R.layout.activity_beranda);
        SharedPreferences prefs = getSharedPreferences("kasir_prefs", MODE_PRIVATE);
        String lastAddress = prefs.getString("last_device_address", null);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        cvSetting = findViewById(R.id.cvSetting);
        cvOrder = findViewById(R.id.cvOrder);

        cvOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingPerangkat = new Intent(BerandaActivity.this, DaftarMejaWaitersActivity.class);
                startActivity(settingPerangkat);
            }
        });

        cvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingPerangkat = new Intent(BerandaActivity.this, PengaturanPerangkatActivity.class);
                startActivity(settingPerangkat);
            }
        });
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerNetworkCallback();
        } else {
            // Untuk versi lama, lakukan pengecekan manual
            boolean connected = NetworkUtil.isConnectedToInternet(this);
            updateStatus(connected);
        }

        BluetoothHelper.autoReconnect(this);

    }

    private void hideSystemUI() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_LAYOUT_FLAGS
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

//    private TextView tvStatus;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;

    private void registerNetworkCallback() {
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> {
                    updateStatus(true);
                    Toast.makeText(BerandaActivity.this, "Koneksi Internet Aktif", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> {
                    updateStatus(false);
                    Toast.makeText(BerandaActivity.this, "Koneksi Internet Terputus", Toast.LENGTH_SHORT).show();
                });
            }
        };

        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    private void updateStatus(boolean connected) {
        if (connected) {
            Toast.makeText(this, "✅ Terhubung ke Internet", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Tidak ada koneksi Internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}