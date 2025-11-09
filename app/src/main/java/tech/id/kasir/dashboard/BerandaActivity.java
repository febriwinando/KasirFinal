package tech.id.kasir.dashboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.id.kasir.R;
import tech.id.kasir.api.RetrofitClient;
import tech.id.kasir.database.DBHelper;
import tech.id.kasir.firebase.MyFirebaseMessagingService;
import tech.id.kasir.network.NetworkUtil;
import tech.id.kasir.pengaturan.PengaturanPerangkatActivity;
import tech.id.kasir.response_api.ApiResponse;
import tech.id.kasir.warung.TambahWarungActivity;

public class BerandaActivity extends AppCompatActivity {
    CardView cvOrder, cvSetting, cvTambahProduk;
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
        cvTambahProduk = findViewById(R.id.cvTambahProduk);

        cvTambahProduk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tambahwarung = new Intent(BerandaActivity.this, TambahWarungActivity.class);
                startActivity(tambahwarung);
            }
        });
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

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    sendTokenToServer(1, token);
                });

        DBHelper dbHelper = new DBHelper(this);

        if (dbHelper.isTableEmpty("provinsi")) {
            dbHelper.importFromJSON(this, "provinsis.json", "provinsi");
        }

        if (dbHelper.isTableEmpty("kabupaten")) {
            dbHelper.importFromJSON(this, "kabupatens.json", "kabupaten");
        }

        if (dbHelper.isTableEmpty("kecamatan")) {
            dbHelper.importFromJSON(this, "kecamatans.json", "kecamatan");
        }

        if (dbHelper.isTableEmpty("kelurahan")) {
            dbHelper.importFromJSON(this, "kelurahans.json", "kelurahan");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestNotificationPermission();
    }

    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin notifikasi diberikan ✅", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak ❌", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static void sendTokenToServer(int userId, String token) {

        Call<ApiResponse> call = RetrofitClient.getInstance().getApi().saveToken(userId, token);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, Response<ApiResponse> response) {
                if (!response.isSuccessful()) {
                    Log.d("FCM_TOKEN", 1+" - "+response.message());
                    return;
                }
                if (response.isSuccessful()) {
                    Log.d("FCM_TOKEN", "Token berhasil dikirim ke server "+response.body().getMessage());
                } else {
                    Log.e("FCM_TOKEN", "Gagal kirim token: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, Throwable t) {
                Log.e("FCM_TOKEN", "Error: " + t.getMessage());

            }
        });
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
//                    Toast.makeText(BerandaActivity.this, "Koneksi Internet Aktif", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> {
                    updateStatus(false);
//                    Toast.makeText(BerandaActivity.this, "Koneksi Internet Terputus", Toast.LENGTH_SHORT).show();
                });
            }
        };

        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    private void updateStatus(boolean connected) {
        if (connected) {
//            Toast.makeText(this, "✅ Terhubung ke Internet", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "❌ Tidak ada koneksi Internet", Toast.LENGTH_SHORT).show();
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