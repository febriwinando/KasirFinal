package tech.id.kasir.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Objects;

import tech.id.kasir.R;
import tech.id.kasir.adpter.DaftarMejaAdapter;
import tech.id.kasir.adpter.ModelMeja;
import tech.id.kasir.database.DBHelper;
import tech.id.kasir.login.MasukActivity;
import tech.id.kasir.response_api.Restoran;
import tech.id.kasir.transaksi.OrderActivity;

public class DaftarMejaWaitersActivity extends AppCompatActivity {

    RecyclerView rvDaftarMeja;
    DaftarMejaAdapter daftarMejaAdapter;
    ArrayList<ModelMeja> modelMejas = new ArrayList<>();
    static ArrayList<String> daftarMeja = new ArrayList<>();
    TextView tvNotifMejaKosong;
    ImageView kembaliDariDaftarMeja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daftar_meja_waiters);
        DBHelper dbHelper = new DBHelper(this);
        Restoran restoran = dbHelper.getRestoran();
        tvNotifMejaKosong= findViewById(R.id.tvNotifMejaKosong);
        rvDaftarMeja= findViewById(R.id.rvDaftarMeja);
        kembaliDariDaftarMeja= findViewById(R.id.kembaliDariDaftarMeja);

        if (restoran.getMeja() == 0){
            tvNotifMejaKosong.setVisibility(View.VISIBLE);
        }else {
            tvNotifMejaKosong.setVisibility(View.GONE);

            for (int n = 1; n <= restoran.getMeja(); n++) {
                daftarMeja.add(String.valueOf(n));
            }

            modelMejas.addAll(getDaftarMeja());
            TampilkanDftarMeja();
        }


        TextInputEditText cariMeja = findViewById(R.id.tietCariNomorMeja);
        cariMeja.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                daftarMejaAdapter.getFilter().filter(Objects.requireNonNull(cariMeja.getText()).toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    static ArrayList<ModelMeja> getDaftarMeja() {
        ArrayList<ModelMeja> list = new ArrayList<>();
        list.clear();
        for (int position = 0; position < daftarMeja.size(); position++) {
            ModelMeja modelMeja = new ModelMeja();
            modelMeja.setJmlhMeja(daftarMeja.get(position));
            list.add(modelMeja);
        }
        return list;
    }

    public void TampilkanDftarMeja(){
        rvDaftarMeja.setLayoutManager(new GridLayoutManager(this, 4));
        daftarMejaAdapter = new DaftarMejaAdapter(modelMejas, DaftarMejaWaitersActivity.this);
        rvDaftarMeja.setAdapter(daftarMejaAdapter);

        daftarMejaAdapter.setOnItemClickCallback(new DaftarMejaAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(ModelMeja data) {
                Intent intentMeja = new Intent(DaftarMejaWaitersActivity.this, OrderActivity.class);
                intentMeja.putExtra("nomeja", data.getJmlhMeja());
                startActivity(intentMeja);
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
}