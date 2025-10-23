package tech.id.kasir.transaksi;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.os.BuildCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.id.kasir.R;
import tech.id.kasir.api.RetrofitClient;
import tech.id.kasir.dashboard.DaftarMejaWaitersActivity;
import tech.id.kasir.database.DBHelper;
import tech.id.kasir.dialog.DialogView;
import tech.id.kasir.login.MasukActivity;
import tech.id.kasir.response_api.Menu;
import tech.id.kasir.response_api.MenuResponse;
import tech.id.kasir.response_api.Order;
import tech.id.kasir.response_api.Restoran;
import tech.id.kasir.response_api.RestoranResponse;

public class OrderActivity extends AppCompatActivity {

    DBHelper dbHelper;
    ArrayList<Menu> listMenu = new ArrayList<>();
    DialogView dialogView = new DialogView(OrderActivity.this);
    RecyclerView rvListMenu;
    String noMeja;
    TextInputEditText tietCariProduk;
    ImageView ibNextTransaksi, kembalidariOrderActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);
        Bundle intentPesananMeja = getIntent().getExtras();
        noMeja = intentPesananMeja.getString("nomeja");
        dbHelper = new DBHelper(OrderActivity.this);

//        dbHelper.get

        rvListMenu = findViewById(R.id.rvListProduk);
        tietCariProduk = findViewById(R.id.tietCariProduk);
        ibNextTransaksi = findViewById(R.id.ibNextTransaksi);
        kembalidariOrderActivity = findViewById(R.id.kembalidariOrderActivity);

        rvListMenu.setHasFixedSize(true);

        tietCariProduk.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                orderAdapter.getFilter().filter(Objects.requireNonNull(tietCariProduk.getText()).toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        kembalidariOrderActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ibNextTransaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = dbHelper.getOrdersByStatusAndMeja("diproses", noMeja);
                if (cursor.moveToFirst()){
                    Intent pindahPesan = new Intent(OrderActivity.this, TransaksiActivity.class);
                    pindahPesan.putExtra("nomeja", noMeja);
                    pindahPesan.putExtra("id_order", cursor.getInt(0));
                    startActivity(pindahPesan);
                }
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




    private void hideSystemUI() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_LAYOUT_FLAGS
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void getDataMenus(){

        Call<MenuResponse> call = RetrofitClient.getInstance().getApi().getMenus();

        call.enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(OrderActivity.this, "Gagal ambil data restoran", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.deleteAllMenus();


                    List<Menu> menuList = response.body().getMenus(); // Ambil list menu dari response
                    dbHelper.insertMenus(menuList);

                } else {
                    Toast.makeText(OrderActivity.this, "Gagal mengambil data menu dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                Toast.makeText(OrderActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        getDataMenus();


        listMenu.clear();
        listMenu.addAll(getMenusFromDatabase());
        showRecyclerList();

    }

    private ArrayList<Menu> getMenusFromDatabase() {
        ArrayList<Menu> menuList = new ArrayList<>();
        Cursor menus = dbHelper.getMenus();

        if (menus != null && menus.moveToFirst()) {
            do {
                Menu menu = new Menu();
                menu.setId(menus.getInt(0));
                menu.setNama_produk(menus.getString(1));
                menu.setKategori_id(menus.getInt(2));
                menu.setKategori_nama(menus.getString(3));
                menu.setHarga(menus.getString(4));
                menu.setStok(menus.getInt(5));
                menu.setDeskripsi(menus.getString(6));
                menu.setGambar(menus.getString(7));
                menu.setStatus(menus.getString(8));

                menuList.add(menu);
            } while (menus.moveToNext());
            menus.close();
        }

        return menuList;
    }


    OrderAdapter orderAdapter;
    private void showRecyclerList(){

        rvListMenu.setLayoutManager(new GridLayoutManager(this, 2));
        orderAdapter = new OrderAdapter(listMenu, OrderActivity.this, noMeja);
        rvListMenu.setAdapter(orderAdapter);

        orderAdapter.setOnItemClickCallback(new OrderAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(Menu data) {

                showDynamicNoteDialog(OrderActivity.this, "Burger", data.getId());
            }

            @Override
            public void onItemLongClicked(Menu data) {
                dialogView.viewHapusItemOrder();
            }
        });
    }

    public void showDynamicNoteDialog(Context context, String menuName, int orderItemId) {
        Dialog dialogNotes = new Dialog(context, R.style.DialogStyle);
        dialogNotes.setContentView(R.layout.dialog_item_order_notes);
        dialogNotes.setCancelable(true);

        TextView btnSave = dialogNotes.findViewById(R.id.btnSave);
        TextView tvMenuName = dialogNotes.findViewById(R.id.tvMenuName);
        TextInputEditText tietNote = dialogNotes.findViewById(R.id.tietNote);
        TextInputEditText tietQtyNote = dialogNotes.findViewById(R.id.tietQtyNote);

        LinearLayout checkboxContainer = dialogNotes.findViewById(R.id.containerCheckbox);
        checkboxContainer.removeAllViews();


        String[] opsiMakanan = {"Pedas", "Tidak Pedas", "Berkuah", "Kuah Sedikit"};
        String[] opsiMinuman = {"Dingin", "Panas", "Hangat", "Banyak Es", "Es Sedikit"};

        // Pilih kategori
        String kategori = "makanan"; // nanti bisa kamu ganti dinamis
        String[] opsi = kategori.equals("makanan") ? opsiMakanan : opsiMinuman;

        for (String label : opsi) {
            // Gunakan ContextThemeWrapper agar style diterapkan
            MaterialCheckBox checkBox = new MaterialCheckBox(
                    new ContextThemeWrapper(context, R.style.CustomMaterialCheckBox), null
            );
            checkBox.setText(label);
            checkBox.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            checkboxContainer.addView(checkBox);
        }


        tvMenuName.setText(menuName);
        btnSave.setOnClickListener(v -> {
            String noteText = tietNote.getText().toString().trim();
            int qty = Integer.parseInt(tietQtyNote.getText().toString().trim().isEmpty() ? "0" : tietQtyNote.getText().toString().trim());

            // Kumpulkan checkbox yang dicentang
            JSONArray checkedOptions = new JSONArray();
            for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
                View child = checkboxContainer.getChildAt(i);
                if (child instanceof MaterialCheckBox) {
                    MaterialCheckBox cb = (MaterialCheckBox) child;
                    if (cb.isChecked()) {
                        checkedOptions.put(cb.getText().toString());
                    }
                }
            }

            // Gabungkan ke satu objek JSON
            JSONObject noteObject = new JSONObject();
            try {
                noteObject.put("text", noteText);
                noteObject.put("options", checkedOptions);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Simpan ke database
            dbHelper.insertOrderItemDetail(orderItemId, noteObject.toString(), qty);

            dialogNotes.dismiss();
        });

//        btnSave.setOnClickListener(v -> {
//            String note = tietNote.getText().toString().trim();
//            int qty = Integer.parseInt(tietQtyNote.getText().toString().trim().isEmpty() ? "0" : tietQtyNote.getText().toString().trim());
//
//            dbHelper.insertOrderItemDetail(orderItemId, note, qty);
//            dialogNotes.dismiss();
//        });

        dialogNotes.show();
    }


//    public void showDynamicNoteDialog(Context context, String menuName, int orderItemId) {
//        Dialog dialogNotes = new Dialog(context, R.style.DialogStyle);
//        dialogNotes.setContentView(R.layout.dialog_item_notes);
//        dialogNotes.setCancelable(true);
//
//
//        LinearLayout container = dialogNotes.findViewById(R.id.containerNotes);
//        TextView btnAddNote = dialogNotes.findViewById(R.id.btnAddNote);
//        TextView btnSave = dialogNotes.findViewById(R.id.btnSave);
//        TextView tvMenuName = dialogNotes.findViewById(R.id.tvMenuName);
//        tvMenuName.setText(menuName);
//
//        addNoteRow(context, container); // Baris pertama otomatis
//        btnAddNote.setOnClickListener(v -> addNoteRow(context, container));
//
//        btnSave.setOnClickListener(v -> {
//            int count = container.getChildCount();
//            DBHelper dbHelper = new DBHelper(context);
//
//            for (int i = 0; i < count; i++) {
//                View row = container.getChildAt(i);
//                TextInputEditText tietNote = row.findViewById(R.id.tietNote);
//                TextInputEditText tietQtyNote = row.findViewById(R.id.tietQtyNote);
//
//                String note = tietNote.getText().toString().trim();
//                int qty = Integer.parseInt(tietQtyNote.getText().toString().trim().isEmpty() ? "0" : tietQtyNote.getText().toString().trim());
//
//                // Simpan ke database
//                dbHelper.insertOrderItemDetail(orderItemId, note, qty);
//            }
//
//            Toast.makeText(context, "Berhasil disimpan ke database", Toast.LENGTH_SHORT).show();
//            dialogNotes.dismiss();
//        });
//
//        dialogNotes.show();
//    }

//    private void addNoteRow(Context context, LinearLayout container) {
//        View row = LayoutInflater.from(context).inflate(R.layout.item_note_row, container, false);
//        container.addView(row);
//    }

}