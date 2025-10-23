package tech.id.kasir.transaksi;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.os.BuildCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import tech.id.kasir.R;
import tech.id.kasir.database.DBHelper;
import tech.id.kasir.response_api.Menu;
import tech.id.kasir.response_api.OrderItemRequest;

public class TransaksiActivity extends AppCompatActivity {

    Date date = new Date();
    // Tentukan format tanggal
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    ArrayList<OrderItemRequest> listOrders = new ArrayList<>();
    RecyclerView rvListOrders;
    ImageView kembalidariTransaksi;
    TextView tanggalTransaksi, tvHasilTotalView, tvTotalOrder, tvUangTerima, tvUangKembali;
    TextInputEditText tietUangTerima;
    DBHelper dbHelper = new DBHelper(this);

    TransaksiAdapter transaksiAdapter;
    String noMeja;
    Integer id_order;
    Double uangDiterima, uang_kembali;
    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaksi);

        Bundle intentPesananMeja = getIntent().getExtras();
        noMeja = intentPesananMeja.getString("nomeja");
        id_order = intentPesananMeja.getInt("id_order");

        kembalidariTransaksi = findViewById(R.id.kembalidariTransaksi);
        tanggalTransaksi = findViewById(R.id.tanggalTransaksi);
        tvHasilTotalView = findViewById(R.id.tvHasilTotalView);
        rvListOrders = findViewById(R.id.rvDaftarTransaksi);
        tvTotalOrder = findViewById(R.id.tvTotalOrder);
        tietUangTerima = findViewById(R.id.tietUangTerima);
        tvUangTerima = findViewById(R.id.tvUangTerima);
        tvUangKembali = findViewById(R.id.tvUangKembali);

        rvListOrders.setHasFixedSize(true);


        double total = dbHelper.getTotalHargaByOrderId(id_order);
        String totalFormatted = formatRupiah.format(total);

        tvHasilTotalView.setText(totalFormatted);
        tvTotalOrder.setText(totalFormatted);

        String today = sdf.format(date);
        tanggalTransaksi.setText(today);

        tietUangTerima.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()){
                    tvUangTerima.setText("");
                    tvUangKembali.setText("");
                }else{
                    uangDiterima = Double.parseDouble(s.toString().trim());
                    tvUangTerima.setText(formatRupiah.format(uangDiterima));
                    uang_kembali = uangDiterima - total;
                    tvUangKembali.setText(formatRupiah.format(uang_kembali));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        kembalidariTransaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (BuildCompat.isAtLeastT()) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    () -> {
                        finish();
                    }
            );
        }

    }



    @Override
    protected void onResume() {
        super.onResume();
        listOrders.clear();
        listOrders.addAll(getOrdersfromDatabase());
        showRecyclerList();
    }

    private void showRecyclerList(){
        rvListOrders.setLayoutManager(new LinearLayoutManager(this));
//        rvListOrders.setLayoutManager(new GridLayoutManager(this, 2));
        transaksiAdapter = new TransaksiAdapter(listOrders, TransaksiActivity.this, noMeja);
        rvListOrders.setAdapter(transaksiAdapter);

    }

    int totalQty;

    private ArrayList<OrderItemRequest> getOrdersfromDatabase() {
        ArrayList<OrderItemRequest> menuList = new ArrayList<>();
        Cursor orders = dbHelper.getOrderItemsByOrderId(id_order);

        if (orders != null && orders.moveToFirst()) {
            do {
                OrderItemRequest order = new OrderItemRequest();
                order.setId(orders.getInt(0));
                order.setOrder_id(orders.getInt(1));
                order.setMenu_id(orders.getInt(2));
                order.setNama_menu(orders.getString(3));
                order.setJumlah(orders.getInt(4));
                order.setHarga_satuan(orders.getString(5));
                order.setCatatan(orders.getString(6));
                totalQty += orders.getInt(4);
                menuList.add(order);
            } while (orders.moveToNext());
            orders.close();
        }

        return menuList;
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