package tech.id.kasir.login;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.id.kasir.R;
import tech.id.kasir.api.RetrofitClient;
import tech.id.kasir.dashboard.BerandaActivity;
import tech.id.kasir.dashboard.DaftarMejaWaitersActivity;
import tech.id.kasir.database.DBHelper;
import tech.id.kasir.response_api.Pengguna;
import tech.id.kasir.response_api.Restoran;
import tech.id.kasir.response_api.RestoranResponse;
import tech.id.kasir.response_api.login;

public class MasukActivity extends AppCompatActivity {

    TextInputEditText email, password;
    LinearLayout llLogin;
    TextView emptyemail, emptypassword, tvBtnMasuk;
    String sEmail, sPassword;
    ProgressBar pbBtnLogin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_masuk);


        DBHelper dbHelper = new DBHelper(this);
        if (dbHelper.isUserLoggedIn()) {
            startActivity(new Intent(this, BerandaActivity.class));
            finish();
        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.pasword);
        llLogin = findViewById(R.id.llLogin);
        emptyemail = findViewById(R.id.emptyemail);
        emptypassword = findViewById(R.id.emptypassword);
        tvBtnMasuk = findViewById(R.id.tvBtnMasuk);
        pbBtnLogin = findViewById(R.id.pbBtnLogin);

        llLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sEmail = Objects.requireNonNull(email.getText()).toString();
                sPassword = Objects.requireNonNull(password.getText()).toString();

                if (sEmail.isEmpty()){
                    if (sPassword.isEmpty()){
                        emptyemail.setVisibility(View.VISIBLE);
                        emptypassword.setVisibility(View.VISIBLE);
                    }else{
                        emptypassword.setVisibility(View.GONE);
                        emptyemail.setVisibility(View.VISIBLE);
                    }
                } else if (sPassword.isEmpty()) {
                    if (sEmail.isEmpty()){
                        emptyemail.setVisibility(View.VISIBLE);
                        emptypassword.setVisibility(View.VISIBLE);
                    }else {
                        emptyemail.setVisibility(View.GONE);
                        emptypassword.setVisibility(View.VISIBLE);
                    }
                }else{
                    pbBtnLogin.setVisibility(View.VISIBLE);
                    tvBtnMasuk.setText("proses login ...");
                    kirimdata(email.getText().toString().trim(), password.getText().toString().trim());
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        DBHelper dbHelper = new DBHelper(this);
        if (dbHelper.isUserLoggedIn()) {
            startActivity(new Intent(this, BerandaActivity.class));
            finish();
        }
    }

    public void kirimdata(String username, String password){

        Call<login> call = RetrofitClient.getInstance().getApi().loginadmin(
                username, password
        );

        call.enqueue(new Callback<login>() {
            @Override
            public void onResponse(@NonNull Call<login> call, @NonNull Response<login> response) {

                if (!response.isSuccessful()){
                    Log.d("response", response.message());
                    Toast.makeText(MasukActivity.this, "tidak merespon", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(response.body().isStatus()){
                    Pengguna pengguna = response.body().getPengguna();

                    DBHelper dbHelper = new DBHelper(MasukActivity.this);
                    dbHelper.insertPengguna(pengguna);

                    getDataRestoran(pengguna.getRestoran_id());

//                    Toast.makeText(MasukActivity.this, pengguna.getNama() + " berhasil login", Toast.LENGTH_SHORT).show();
//
//                    startActivity(new Intent(MasukActivity.this, DaftarMejaWaitersActivity.class));
                }

            }

            @Override
            public void onFailure(@NonNull Call<login> call, @NonNull Throwable t) {
                Log.d("Hasil Error", t.toString());
                Toast.makeText(MasukActivity.this, t.toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void getDataRestoran(int restoranId){

        DBHelper dbHelper = new DBHelper(MasukActivity.this);

        Call<RestoranResponse> call = RetrofitClient.getInstance().getApi().getRestoran(restoranId);

        call.enqueue(new Callback<RestoranResponse>() {
            @Override
            public void onResponse(Call<RestoranResponse> call, Response<RestoranResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MasukActivity.this, "Gagal ambil data restoran", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Restoran restoran = response.body().getRestoran();

                    dbHelper.insertRestoran(restoran);
                    startActivity(new Intent(MasukActivity.this, DaftarMejaWaitersActivity.class));
                    Toast.makeText(MasukActivity.this, "Restoran disimpan: " + restoran.getRestoran(), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MasukActivity.this, "Gagal mendapatkan data restoran", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RestoranResponse> call, Throwable t) {
                Toast.makeText(MasukActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_LAYOUT_FLAGS
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}