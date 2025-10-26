package tech.id.kasir.transaksi;

import android.bluetooth.BluetoothSocket;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.BuildCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import tech.id.kasir.R;
import tech.id.kasir.database.DBHelper;
import tech.id.kasir.response_api.OrderItemRequest;
import tech.id.kasir.utility.btt.ModelProdukFinalTransaksi;
import tech.id.kasir.utility.btt.SintaksPOST;
import tech.id.kasir.utility.btt.UtilBluetooth;
import static tech.id.kasir.pengaturan.PengaturanPerangkatActivity.bluetoothSocket;
import static tech.id.kasir.pengaturan.PengaturanPerangkatActivity.outputStreamer;

public class TransaksiActivity extends AppCompatActivity {

    Date date = new Date();
    // Tentukan format tanggal
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    ArrayList<OrderItemRequest> listOrders = new ArrayList<>();
    RecyclerView rvListOrders;
    ImageView kembalidariTransaksi;
    TextView tanggalTransaksi;
    TextView tvHasilTotalView;
    TextView tvTotalOrder;
    TextView tvUangTerima;
    TextView tvUangKembali;
    TextView tvInvoice;
    TextInputEditText tietUangTerima;
    DBHelper dbHelper = new DBHelper(this);

    TransaksiAdapter transaksiAdapter;
    String noMeja;
    Integer id_order;
    Double uangDiterima, uang_kembali;
    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
    RadioButton radioStatusSelected;
    int selected;
    ImageButton ivPrintVactur;
    RadioGroup rgStatusTransaksi;
    UtilBluetooth utilBluetooth = new UtilBluetooth();
//    public static OutputStream outputStreamer;
    SintaksPOST sintaksPOST = new SintaksPOST();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaksi);

        Bundle intentPesananMeja = getIntent().getExtras();
        noMeja = Objects.requireNonNull(intentPesananMeja).getString("nomeja");
        id_order = intentPesananMeja.getInt("id_order");

        kembalidariTransaksi = findViewById(R.id.kembalidariTransaksi);
        tanggalTransaksi = findViewById(R.id.tanggalTransaksi);
        tvHasilTotalView = findViewById(R.id.tvHasilTotalView);
        rvListOrders = findViewById(R.id.rvDaftarTransaksi);
        tvTotalOrder = findViewById(R.id.tvTotalOrder);
        tietUangTerima = findViewById(R.id.tietUangTerima);
        tvUangTerima = findViewById(R.id.tvUangTerima);
        tvUangKembali = findViewById(R.id.tvUangKembali);
        tvInvoice = findViewById(R.id.tvInvoice);
        ivPrintVactur = findViewById(R.id.ivPrintVactur);
        rgStatusTransaksi = findViewById(R.id.rgStatusTransaksi);

        selected = rgStatusTransaksi.getCheckedRadioButtonId();
        radioStatusSelected = findViewById(selected);

        rvListOrders.setHasFixedSize(true);


        double total = dbHelper.getTotalHargaByOrderId(id_order);
        String totalFormatted = formatRupiah.format(total);


        String invoice = dbHelper.getInvoiceByMeja(noMeja);

        if (invoice != null) {
            tvInvoice.setText(invoice);
        }

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

        ivPrintVactur.setOnClickListener(v -> {
            String status = "0";
            selected = rgStatusTransaksi.getCheckedRadioButtonId();
            radioStatusSelected = findViewById(selected);
            if (selected != -1){
                if (radioStatusSelected.getText().toString().equals("Lunas")){
                    status = "1";

                    if (tietUangTerima.getText().toString().trim().isEmpty()){
                        tietUangTerima.setError("Harap Isi Kolom Tersebut");
                        return;
                    }


                    if (uang_kembali < 0.00){
                        tietUangTerima.setError("Harap Isi Dengan Benar");
                        return;
                    }
                }else{
                    status = "2";
                    uangDiterima = (double) 0;
                    uang_kembali = (double) 0;
                }


            }else{
                uangDiterima = (double) 0;
                uang_kembali =  (double) 0;
            }



            if (utilBluetooth.isConnected(bluetoothSocket)){
                try {
                    outputStreamer = bluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(this, "Berhasil", Toast.LENGTH_SHORT).show();

                sintaksPOST.droping(TransaksiActivity.this, listOrders, bluetoothSocket,
                        outputStreamer, "pengunjung", invoice, today, "costumer", formatRupiah.format(total), formatRupiah.format(total),
                        formatRupiah.format(uangDiterima), String.valueOf(uang_kembali), formatRupiah.format(total), status);

                sintaksPOST.openCashDrawer(outputStreamer);
            }else{

                Toast.makeText(this, "Gagal", Toast.LENGTH_SHORT).show();


//                viewListBluetooth();

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
                    this::finish
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