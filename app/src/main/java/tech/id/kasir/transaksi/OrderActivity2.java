package tech.id.kasir.transaksi;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.id.kasir.R;
import tech.id.kasir.api.RetrofitClient;
import tech.id.kasir.database.DBHelper;
import tech.id.kasir.response_api.Menu;
import tech.id.kasir.response_api.MenuResponse;

public class OrderActivity2 extends AppCompatActivity {

    DBHelper dbHelper;
//    public static int jumlahMenu = 0;
//
//
//    static ArrayList<Integer> listIdProduk = new ArrayList<Integer>();
//    static ArrayList<String> listNamaProduk = new ArrayList<String>();
//    static ArrayList<Integer> listKategoriProdukId = new ArrayList<>();
//    static ArrayList<String> listKategoriNama = new ArrayList<String>();
//
//    static ArrayList<Integer> listStok = new ArrayList<Integer>();
//    static ArrayList<String> listHarga = new ArrayList<>();
//    static ArrayList<String> listDeskripsi = new ArrayList<>();
//    static ArrayList<String> listGambar = new ArrayList<>();
//    static ArrayList<String> listStatus = new ArrayList<>();

    ArrayList<Menu> listMenu = new ArrayList<>();

    RecyclerView rvListMenu;
    String noMeja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);
        Bundle intentPesananMeja = getIntent().getExtras();
        noMeja = intentPesananMeja.getString("nomeja");
        dbHelper = new DBHelper(OrderActivity2.this);

//        dbHelper.get

        rvListMenu = findViewById(R.id.rvListProduk);
        rvListMenu.setHasFixedSize(true);

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
                    Toast.makeText(OrderActivity2.this, "Gagal ambil data restoran", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.deleteAllMenus();


                    List<Menu> menuList = response.body().getMenus(); // Ambil list menu dari response
                    dbHelper.insertMenus(menuList);

//                    for (Menu menu : menuList){
//                        Log.d("Menus", menu.getKategori().getNama_kategori());
//                    }

//                    Toast.makeText(OrderActivity.this, "Data menu berhasil disimpan ke SQLite", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OrderActivity2.this, "Gagal mengambil data menu dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                Toast.makeText(OrderActivity2.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//
//    private void databaseProduk() {
//
//        Cursor menus = dbHelper.getMenus();
//        if (menus.getCount() == 0){
//            Toast.makeText(this, "Data Produk Kosong", Toast.LENGTH_SHORT).show();
//            return;
//        }else{
//            jumlahMenu = dbHelper.getMenus().getCount();
//        }
//
//
//        while (menus.moveToNext()){
//
//            listIdProduk.add(menus.getInt(0));
//            listNamaProduk.add(menus.getString(1));
//            listKategoriProdukId.add(menus.getInt(2));
//            listKategoriNama.add(menus.getString(3));
//            listHarga.add(menus.getString(4));
//            listStok.add(menus.getInt(5));
//            listDeskripsi.add(menus.getString(6));
//            listGambar.add(menus.getString(7));
//            listStatus.add(menus.getString(8));
//
//
//        }
//
//    }


    @Override
    protected void onResume() {
        super.onResume();
        getDataMenus();


        listMenu.clear();
        listMenu.addAll(getMenusFromDatabase());
        showRecyclerList();

//        listMenu.clear();
//        listIdProduk.clear();
//        listNamaProduk.clear();
//        listKategoriProdukId.clear();
//        listKategoriNama.clear();
//        listHarga.clear();
//        listStok.clear();
//        listDeskripsi.clear();
//        listGambar.clear();
//        listStatus.clear();
//        databaseProduk();
//        listMenu.addAll(getDaftarMenus());
//        showRecyclerList();
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


//    static ArrayList<Menu> getDaftarMenus() {
//        ArrayList<Menu> list = new ArrayList<>();
//        list.clear();
//        for (int position = 0; position < jumlahMenu; position++) {
//            Menu menus = new Menu();
//            menus.setId(listIdProduk.get(position));
//            menus.setNama_produk(listNamaProduk.get(position));
//            menus.setKategori_id(listKategoriProdukId.get(position));
//            menus.setKategori_nama(listKategoriNama.get(position));
//            menus.setHarga(listHarga.get(position));
//            menus.setStok(listStok.get(position));
//            menus.setDeskripsi(listDeskripsi.get(position));
//            menus.setGambar(listGambar.get(position));
//            menus.setStatus(listStatus.get(position));
//            list.add(menus);
//        }
//        return list;
//    }

    OrderAdapter orderAdapter;
    private void showRecyclerList(){
        rvListMenu.setLayoutManager(new GridLayoutManager(this, 2));
        orderAdapter = new OrderAdapter(listMenu, OrderActivity2.this, noMeja);
        rvListMenu.setAdapter(orderAdapter);

//        orderAdapter.setOnItemClickCallback(data -> {
//            Toast.makeText(this, "ada "+data.getHarga(), Toast.LENGTH_SHORT).show();
//            String invoice = SIMPLE_FORMAT_INVOICE.format(new Date());
//            Cursor idPesanan = databaseKasir.getDataOrderanMejaByStatusMeja(noMeja, "0");
//
//            if (idPesanan.getCount() > 0){
//                while (idPesanan.moveToNext()){
//                    int id_order = Integer.parseInt(idPesanan.getString(0));
//                    invoice = idPesanan.getString(1);
//                    Cursor checkTambahPesanan = databaseKasir.getDataTambahPesananMeja(String.valueOf(id_order), noMeja, data.getId());
//                    if (checkTambahPesanan.getCount() > 0){
//                        checkTambahPesanan.moveToNext();
//                        int jumlahPesanan = Integer.parseInt(checkTambahPesanan.getString(5))+1;
//                        databaseKasir.updateTambahPesananMeja(checkTambahPesanan.getInt(0), String.valueOf(jumlahPesanan));
//                    }else{
//                        databaseKasir.insertDataPesananMeja(idPesanan.getString(0), invoice, noMeja, data.getId(), "1", data.getHarga_produk(), SIMPLE_FORMAT_TANGGAL.format(new Date()), SIMPLE_FORMAT_JAM.format(new Date()), "0", costumer );
//                    }
//                    if (order_tambah){
//                        Cursor checkTambahPesanantambah = databaseKasir.getDataTambahPesananMejaTambah(String.valueOf(id_order), noMeja, data.getId());
//                        if (checkTambahPesanantambah.getCount() > 0){
//                            checkTambahPesanantambah.moveToNext();
//                            int jumlahPesanan = Integer.parseInt(checkTambahPesanantambah.getString(5))+1;
//                            databaseKasir.updateTambahPesananMejaTambah(checkTambahPesanantambah.getInt(0), String.valueOf(jumlahPesanan));
//                        }else{
//                            databaseKasir.insertDataPesananMejaTambah(idPesanan.getString(0), invoice, noMeja, data.getId(), "1", data.getHarga_produk(), SIMPLE_FORMAT_TANGGAL.format(new Date()), SIMPLE_FORMAT_JAM.format(new Date()), "0", costumer );
//
//                        }
//                    }
//
//                }
//            }
//            else{

//
//                Cursor getNewNumber = databaseKasir.getDataOrderanMeja();
//                int idOrderList = 1;
//                if (getNewNumber.getCount()>0){
//                    getNewNumber.moveToNext();
//                    idOrderList = Integer.parseInt(getNewNumber.getString(0))+1;
//                }
//                databaseKasir.insertDataOrderanMeja(String.valueOf(idOrderList), noMeja, invoice, "0");
//                Cursor idNewOrderanMeja = databaseKasir.getDataOrderanMejaByStatusMeja(noMeja, "0");
//                idNewOrderanMeja.moveToNext();
//
//                databaseKasir.insertDataPesananMeja(idNewOrderanMeja.getString(0),
//                        invoice, noMeja, data.getId(), "1", data.getHarga_produk(), SIMPLE_FORMAT_TANGGAL.format(new Date()), SIMPLE_FORMAT_JAM.format(new Date()),
//                        "0", costumer );
//            }

//            ibNextTransaksi.setEnabled(true);
//            ibNextTransaksi.setClickable(true);
//        });
    }
}