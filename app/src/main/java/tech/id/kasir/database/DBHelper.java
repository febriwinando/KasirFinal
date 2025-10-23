package tech.id.kasir.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

import tech.id.kasir.response_api.Menu;
import tech.id.kasir.response_api.Pengguna;
import tech.id.kasir.response_api.Restoran;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "kasir.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_PENGGUNA = "pengguna";
    public static final String TABLE_RESTORAN = "restoran";
    public static final String TABLE_MENU = "menus";
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String TABLE_ORDER = "orders";
    public static final String TABLE_ORDER_ITEM_DETAILS = "order_item_details";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_PENGGUNA + " (" +
                "id INTEGER PRIMARY KEY, " +
                "restoran_id INTEGER, " +
                "nama TEXT, " +
                "email TEXT, " +
                "no_hp TEXT, " +
                "alamat TEXT, " +
                "foto TEXT, " +
                "role TEXT, " +
                "tugas TEXT, " +
                "status TEXT)";
        db.execSQL(CREATE_TABLE);

        String CREATE_RESTORAN_TABLE = "CREATE TABLE " + TABLE_RESTORAN + "("
                + "id INTEGER PRIMARY KEY,"
                + "restoran TEXT,"
                + "kontak TEXT,"
                + "email TEXT,"
                + "owner TEXT,"
                + "meja INTEGER,"
                + "alamat TEXT,"
                + "kelurahan_id TEXT,"
                + "kecamatan_id TEXT,"
                + "kabupaten_id TEXT,"
                + "provinsi_id TEXT,"
                + "jam_buka TEXT,"
                + "jam_tutup TEXT,"
                + "logo TEXT,"
                + "status TEXT"
                + ")";
        db.execSQL(CREATE_RESTORAN_TABLE);


        String CREATE_TABLE_MENU = "CREATE TABLE "+ TABLE_MENU+" (" +
                "id INTEGER PRIMARY KEY, " +
                "nama_produk TEXT, " +
                "kategori_id INTEGER, " +
                "kategori_nama TEXT, " +
                "harga TEXT, " +
                "stok INTEGER, " +
                "deskripsi TEXT, " +
                "gambar TEXT, " +
                "status TEXT)";

        db.execSQL(CREATE_TABLE_MENU);

        String CREATE_TABLE_ORDER = "CREATE TABLE "+TABLE_ORDER+" ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "restoran_id INTEGER,"+
                "pengguna_id INTEGER,"+
                "nomor_invoice TEXT,"+
                "meja TEXT,"+
                "total_harga TEXT,"+
                "status TEXT,"+
                "catatan TEXT,"+
                "created_at TEXT,"+
                "kostumer TEXT)";

        db.execSQL(CREATE_TABLE_ORDER);

        String CREATE_TABLE_ORDER_ITEMS = "CREATE TABLE "+TABLE_ORDER_ITEMS+" ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "order_id INTEGER, "+
                "menu_id INTEGER, "+
                "nama_menu TEXT, "+
                "jumlah INTEGER, "+
                "harga_satuan TEXT, "+
                "catatan TEXT,"+
                "FOREIGN KEY(order_id) REFERENCES orders(id))";

        db.execSQL(CREATE_TABLE_ORDER_ITEMS);


        String CREATE_TABLE_ORDER_ITEM_DETAILS = "CREATE TABLE "+TABLE_ORDER_ITEM_DETAILS+" ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "order_item_id INTEGER,"+
                "note TEXT,"+
                "qty INTEGER,"+
                "FOREIGN KEY(order_item_id) REFERENCES order_items(id))";

        db.execSQL(CREATE_TABLE_ORDER_ITEM_DETAILS);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENGGUNA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESTORAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEM_DETAILS);

        onCreate(db);
    }
    public Cursor getOrdersByStatusAndMeja(String status, String meja) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM orders WHERE status = ? AND meja = ? LIMIT 1", new String[]{status, meja});
    }

    public Cursor getPenggunaId() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM "+TABLE_PENGGUNA+" LIMIT 1", null);
    }

    public List<String> getAllInvoiceNumbers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (List<String>) db.rawQuery("SELECT nomor_invoice FROM orders ", null);
    }

    public Cursor getOrdersByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM orders WHERE status = ?", new String[]{status});
    }

    public Cursor getOrderItemsByOrderId(long orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM order_items WHERE order_id = ?", new String[]{String.valueOf(orderId)});
    }

    public Cursor getOrderItemsByMenuId(int order_id, int menuId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM order_items WHERE order_id = ? and menu_id = ?", new String[]{String.valueOf(order_id), String.valueOf(menuId)});
    }

    public int getTotalJumlahByMenuId(int order_id, int menuId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int total = 0;

        // Gunakan fungsi agregat SUM() di SQL
        Cursor cursor = db.rawQuery(
                "SELECT SUM(jumlah) AS total_jumlah FROM order_items WHERE order_id = ? AND menu_id = ?",
                new String[]{String.valueOf(order_id), String.valueOf(menuId)}
        );

        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("total_jumlah"));
        }
        cursor.close();
        return total;
    }


    public void addOrUpdateOrderItem(int order_id, int menu_id, String nama_menu, int qty, String harga_satuan, String catatan) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT jumlah FROM order_items WHERE order_id = ? AND menu_id = ?",
                new String[]{String.valueOf(order_id), String.valueOf(menu_id)}
        );

        if (cursor.moveToFirst()) {
            // Sudah ada, update jumlahnya
            int jumlahSekarang = cursor.getInt(cursor.getColumnIndexOrThrow("jumlah"));
            int jumlahBaru = jumlahSekarang + qty;

            db.execSQL("UPDATE order_items SET jumlah = ? WHERE order_id = ? AND menu_id = ?",
                    new Object[]{jumlahBaru, order_id, menu_id});
        } else {
            // Belum ada, insert baru
            db.execSQL("INSERT INTO order_items (order_id, menu_id, nama_menu, jumlah, harga_satuan, catatan) VALUES (?, ?, ?, ?, ?, ?)",
                    new Object[]{order_id, menu_id, nama_menu, qty, harga_satuan, catatan});
        }

        cursor.close();
    }

    public void decreaseOrRemoveOrderItem(int order_id, int menu_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT jumlah FROM order_items WHERE order_id = ? AND menu_id = ?",
                new String[]{String.valueOf(order_id), String.valueOf(menu_id)}
        );

        if (cursor.moveToFirst()) {
            int jumlahSekarang = cursor.getInt(cursor.getColumnIndexOrThrow("jumlah"));
            if (jumlahSekarang > 1) {
                // kurangi 1
                int jumlahBaru = jumlahSekarang - 1;
                db.execSQL("UPDATE order_items SET jumlah = ? WHERE order_id = ? AND menu_id = ?",
                        new Object[]{jumlahBaru, order_id, menu_id});
            } else {
                // kalau sudah 1, hapus barisnya
                db.execSQL("DELETE FROM order_items WHERE order_id = ? AND menu_id = ?",
                        new Object[]{order_id, menu_id});
            }
        }

        cursor.close();
    }

//    public long insertOrderItemDetail(long orderItemId, String note, int qty) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("order_item_id", orderItemId);
//        values.put("note", note);
//        values.put("qty", qty);
//        return db.insert(TABLE_ORDER_ITEM_DETAILS, null, values);
//    }

    public void insertOrderItemDetail(int orderItemId, String note, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("order_item_id", orderItemId);
        values.put("note", note);
        values.put("qty", qty);
        db.insert(TABLE_ORDER_ITEM_DETAILS, null, values);
        db.close();
    }


    public double getTotalHargaByOrderId(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0.0;

        String query = "SELECT SUM(jumlah * harga_satuan) AS total FROM " + TABLE_ORDER_ITEMS +
                " WHERE order_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
        }

        cursor.close();
        db.close();
        return total;
    }




    public boolean insertOrder(int restoranId, int penggunaId, String nomorInvoice, String meja,
                            String totalHarga, String status, String catatan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("restoran_id", restoranId);
        values.put("pengguna_id", penggunaId);
        values.put("nomor_invoice", nomorInvoice);
        values.put("meja", meja);
        values.put("total_harga", totalHarga);
        values.put("status", status);
        values.put("catatan", catatan);

        long result = db.insert("orders", null, values);;
        if (result == -1 )
            return false;
        else
            return true;

    }


    public int updateOrder(long orderId, int restoranId, int penggunaId, String nomorInvoice,
                           String meja, String totalHarga, String status, String catatan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("restoran_id", restoranId);
        values.put("pengguna_id", penggunaId);
        values.put("nomor_invoice", nomorInvoice);
        values.put("meja", meja);
        values.put("total_harga", totalHarga);
        values.put("status", status);
        values.put("catatan", catatan);

        return db.update("orders", values, "id=?", new String[]{String.valueOf(orderId)});
    }

    public int updateOrderItem(long itemId, long orderId, int menuId, String namaMenu,
                               int jumlah, String hargaSatuan, String catatan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("order_id", orderId);
        values.put("menu_id", menuId);
        values.put("nama_menu", namaMenu);
        values.put("jumlah", jumlah);
        values.put("harga_satuan", hargaSatuan);
        values.put("catatan", catatan);

        return db.update("order_items", values, "id=?", new String[]{String.valueOf(itemId)});
    }

    public long insertOrderItem(long orderId, int menuId, String namaMenu, int jumlah,
                                String hargaSatuan, String catatan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("order_id", orderId);
        values.put("menu_id", menuId);
        values.put("nama_menu", namaMenu);
        values.put("jumlah", jumlah);
        values.put("harga_satuan", hargaSatuan);
        values.put("catatan", catatan);

        return db.insert("order_items", null, values);
    }


    public void insertPengguna(Pengguna pengguna) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("id", pengguna.getId());
        values.put("restoran_id", pengguna.getRestoran_id());
        values.put("nama", pengguna.getNama());
        values.put("email", pengguna.getEmail());
        values.put("no_hp", pengguna.getNo_hp());
        values.put("alamat", pengguna.getAlamat());
        values.put("foto", pengguna.getFoto());
        values.put("role", pengguna.getRole());
        values.put("tugas", pengguna.getTugas());
        values.put("status", pengguna.getStatus());

        db.insertWithOnConflict(TABLE_PENGGUNA, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }


    public boolean isUserLoggedIn() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PENGGUNA + " LIMIT 1", null);
        boolean ada = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return ada;
    }


    public void insertRestoran(Restoran restoran) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("id", restoran.getId());
        values.put("restoran", restoran.getRestoran());
        values.put("kontak", restoran.getKontak());
        values.put("email", restoran.getEmail());
        values.put("owner", restoran.getOwner());
        values.put("meja", restoran.getMeja());
        values.put("alamat", restoran.getAlamat());
        values.put("kelurahan_id", restoran.getKelurahan_id());
        values.put("kecamatan_id", restoran.getKecamatan_id());
        values.put("kabupaten_id", restoran.getKabupaten_id());
        values.put("provinsi_id", restoran.getProvinsi_id());
        values.put("jam_buka", restoran.getJam_buka());
        values.put("jam_tutup", restoran.getJam_tutup());
        values.put("logo", restoran.getLogo());
        values.put("status", restoran.getStatus());

        // Cek dulu, kalau sudah ada: update
        int updated = db.update(TABLE_RESTORAN, values, "id = ?", new String[]{String.valueOf(restoran.getId())});
        if (updated == 0) {
            db.insert(TABLE_RESTORAN, null, values);
        }

        db.close();
    }

    public Restoran getRestoran() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RESTORAN + " LIMIT 1", null);

        Restoran restoran = null;

        if (cursor.moveToFirst()) {
            restoran = new Restoran();
            restoran.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            restoran.setRestoran(cursor.getString(cursor.getColumnIndexOrThrow("restoran")));
            restoran.setKontak(cursor.getString(cursor.getColumnIndexOrThrow("kontak")));
            restoran.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            restoran.setOwner(cursor.getString(cursor.getColumnIndexOrThrow("owner")));
            restoran.setMeja(cursor.getInt(cursor.getColumnIndexOrThrow("meja")));
            restoran.setAlamat(cursor.getString(cursor.getColumnIndexOrThrow("alamat")));
            restoran.setKelurahan_id(cursor.getString(cursor.getColumnIndexOrThrow("kelurahan_id")));
            restoran.setKecamatan_id(cursor.getString(cursor.getColumnIndexOrThrow("kecamatan_id")));
            restoran.setKabupaten_id(cursor.getString(cursor.getColumnIndexOrThrow("kabupaten_id")));
            restoran.setProvinsi_id(cursor.getString(cursor.getColumnIndexOrThrow("provinsi_id")));
            restoran.setJam_buka(cursor.getString(cursor.getColumnIndexOrThrow("jam_buka")));
            restoran.setJam_tutup(cursor.getString(cursor.getColumnIndexOrThrow("jam_tutup")));
            restoran.setLogo(cursor.getString(cursor.getColumnIndexOrThrow("logo")));
            restoran.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        }

        cursor.close();
        db.close();
        return restoran;
    }


    public void insertMenus(List<Menu> menuList) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            for (Menu menu : menuList) {
                ContentValues values = new ContentValues();
                values.put("id", menu.getId());
                values.put("nama_produk", menu.getNama_produk());
                values.put("kategori_id", menu.getKategori_id());
                Log.d("kategori_nama", menu.getKategori().getNama_kategori());

                // Cek null untuk kategori
                if (menu.getKategori() != null) {
                    values.put("kategori_nama", menu.getKategori().getNama_kategori());
                } else {
                    values.put("kategori_nama", "");
                }

                values.put("harga", menu.getHarga());
                values.put("stok", menu.getStok());
                values.put("deskripsi", menu.getDeskripsi());
                values.put("gambar", menu.getGambar());
                values.put("status", menu.getStatus());

                // Gunakan replace untuk overwrite jika id sudah ada
                db.insertWithOnConflict(TABLE_MENU, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void deleteAllMenus() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MENU, null, null);
        db.close();
    }

    public Cursor getMenus() {
        SQLiteDatabase db = this.getReadableDatabase(); // lebih baik untuk SELECT
        return db.rawQuery("SELECT * FROM " + TABLE_MENU, null);
    }




}
