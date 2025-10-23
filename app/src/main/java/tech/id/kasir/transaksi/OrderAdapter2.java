package tech.id.kasir.transaksi;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tech.id.kasir.R;
import tech.id.kasir.database.DBHelper;
import tech.id.kasir.response_api.Menu;

public class OrderAdapter2 extends  RecyclerView.Adapter<OrderAdapter2.ListViewHolder>{
    private OnItemClickCallback onItemClickCallback;
    DBHelper dbHelper;
//    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    Activity context;
//    ArrayList<ModelQty> modelQties = new ArrayList<ModelQty>();
    public static ArrayList<Menu> produks;
    private final ArrayList<Menu> produksArrayList;

    String statusOrder;
    String no_meja;

    int penggunaId;

    String invoice = "123";
    String baseUrl = "http://172.15.1.202:8000/";

    public OrderAdapter2(ArrayList<Menu> produks, Activity context, String no_meja) {
        OrderAdapter2.produks = produks;
        this.context = context;
        produksArrayList = new ArrayList<>(OrderAdapter2.produks);
        dbHelper = new DBHelper(context);
        this.no_meja = no_meja;
        Cursor dataPengguna =dbHelper.getPenggunaId();
        while (dataPengguna.moveToNext()){
            penggunaId = dataPengguna.getInt(0);
        }
//        List<String> existingInvoices = dbHelper.getAllInvoiceNumbers();
//        invoice = InvoiceGenerator.generateInvoiceNumber(1, 5, existingInvoices);
//        for (int position = 0; position < produks.size(); position++) {
//            ModelQty modelQty = new ModelQty();
//            modelQty.setId(String.valueOf(produks.get(position).getId()));
//            modelQty.setQty(String.valueOf(produks.get(position).getQty_produk()));
//            modelQties.add(modelQty);
//        }
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_row_produk_transaksi_dua, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Menu viewProduk = produks.get(holder.getAdapterPosition());

        holder.tvListProduk.setText(viewProduk.getNama_produk());
        try {
            // ubah String ke double
            double harga = Double.parseDouble(viewProduk.getHarga());

            // format ke Rupiah
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String hargaFormatted = formatRupiah.format(harga);

            // hapus koma dua nol di belakang (opsional)
            hargaFormatted = hargaFormatted.replace(",00", "");

            // tampilkan
            holder.listHargaProduk.setText(hargaFormatted.replace("Rp", "Rp "));
        } catch (NumberFormatException e) {
            // kalau gagal parse (misal datanya bukan angka)
            holder.listHargaProduk.setText("Rp 0");
        }
        holder.tvKategori.setText(viewProduk.getKategori_nama());
        String imageUrl = baseUrl + viewProduk.getGambar();

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.burger) // gambar sementara (opsional)
//                .error(R.drawable.error_image) // gambar fallback jika gagal (opsional)
                .into(holder.ivListProduk);


        Cursor cursorOrder = dbHelper.getOrdersByStatusAndMeja("diproses", no_meja);
        if (cursorOrder.moveToFirst()) {
            int orderIdAktif = cursorOrder.getInt(cursorOrder.getColumnIndexOrThrow("id"));
            int totalJumlah = dbHelper.getTotalJumlahByMenuId(orderIdAktif, viewProduk.getId());
            holder.tvJumlah.setText(String.valueOf(totalJumlah));
        } else {
            holder.tvJumlah.setText("0");
        }
        cursorOrder.close();


        holder.ivTambahJumlah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Cursor cursor = dbHelper.getOrdersByStatusAndMeja("diproses", no_meja);
                if (cursor.getCount() == 0){
                    statusOrder = "draf";
                    boolean order = dbHelper.insertOrder(viewProduk.getId(), penggunaId, invoice, no_meja, null, "diproses", "");
                    if (order){
                        Cursor cursorAktif = dbHelper.getOrdersByStatusAndMeja("diproses", no_meja);
                        while (cursorAktif.moveToNext()){
                            dbHelper.insertOrderItem(cursorAktif.getInt(0), viewProduk.getId(), viewProduk.getNama_produk(), 1, viewProduk.getHarga(), "");
                            holder.tvJumlah.setText("1");
                        }
                    }
                }else{
                    while (cursor.moveToNext()) {
                        String status = cursor.getString(6);
                        if (status.equals("diproses")){
//                            dbHelper.addOrUpdateOrderItem(cursor.getInt(0), viewProduk.getId(), viewProduk.getNama_produk(), 1, viewProduk.getHarga(), "");
//                            int totalJumlah = dbHelper.getTotalJumlahByMenuId(cursor.getInt(0), viewProduk.getId());
//                            holder.tvJumlah.setText(String.valueOf(totalJumlah));

                            statusOrder = "diproses";
                            dbHelper.insertOrderItem(cursor.getInt(0), viewProduk.getId(), viewProduk.getNama_produk(), 1, viewProduk.getHarga(), "");

                            int totalJumlah = dbHelper.getTotalJumlahByMenuId(cursor.getInt(0), viewProduk.getId());
                            holder.tvJumlah.setText(String.valueOf(totalJumlah));

                        }
                        else if (status.equals("selesai")) {
                            statusOrder = "selesai";
                        }else if(status.equals("dibayar")){

                        }
                    }
                }



            }
        });

        holder.ivKurangJumlah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Cursor cursor = dbHelper.getOrdersByStatusAndMeja("diproses", no_meja);
//                if (cursor.getCount() == 0){
//                    holder.tvJumlah.setText("0");
//                }else{
//                    while (cursor.moveToNext()) {
//                        String status = cursor.getString(6);
//                        if (status.equals("diproses")){
//                            statusOrder = "diproses";
//                            dbHelper.insertOrderItem(cursor.getInt(0), viewProduk.getId(), viewProduk.getNama_produk(), 1, viewProduk.getHarga(), "");
//
//                            int totalJumlah = dbHelper.getTotalJumlahByMenuId(cursor.getInt(0), viewProduk.getId());
//                            holder.tvJumlah.setText(String.valueOf(totalJumlah));
//
//                        }
//                        else if (status.equals("selesai")) {
//                            statusOrder = "selesai";
//                        }else if(status.equals("dibayar")){
//
//                        }
//                    }
//                }



            }
        });

        holder.cvListTransaksi.setOnClickListener(v -> {
            onItemClickCallback.onItemClicked(produks.get(holder.getAdapterPosition()));
//            Cursor cursorQty = databaseKasir.getDataTambahPesananMeja(viewProduk.getId_order(), viewProduk.getNoMeja(), viewProduk.getId());
//            if (cursorQty.getCount()>0){
//                cursorQty.moveToNext();
//                holder.tvJumlah.setText(cursorQty.getString(5).toString());
//
//            }
        });

    }

    @Override
    public int getItemCount() {
        return produks.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder{
        ShapeableImageView ivListProduk;
        ImageView  ivKurangJumlah, ivTambahJumlah;
        TextView tvListProduk, tvKategori, tvJumlah, listHargaProduk;
        CardView cvListTransaksi;
        LinearLayout llListTransaksi;
        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            ivListProduk = itemView.findViewById(R.id.ivListProduk);
            tvListProduk = itemView.findViewById(R.id.tvListProduk);
            ivKurangJumlah = itemView.findViewById(R.id.ivKurangJumlah);
            ivTambahJumlah = itemView.findViewById(R.id.ivTambahJumlah);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
            llListTransaksi = itemView.findViewById(R.id.llListTransaksi);
            listHargaProduk = itemView.findViewById(R.id.listHargaProduk);
            cvListTransaksi = itemView.findViewById(R.id.cvListTransaksi);

//            etJumlah.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                    Cursor cursorQty = databaseKasir.getDataProduks();
//                    while (cursorQty.moveToNext()){
//                        if (cursorQty.getString(0).equals(produks.get(getAdapterPosition()).getId())){
//                            databaseKasir.updateTerimaBarang(produks.get(getAdapterPosition()).getId(), etJumlah.getText().toString().trim());
//                        }
//                    }
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//
//                }
//            });

        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(Menu data);
    }

    public Filter getFilter() {
        return userModelFilter;
    }

    private final Filter userModelFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Menu> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(produksArrayList);
            }else{
                String filterpattern = constraint.toString().toLowerCase().trim();

                for (Menu item : produksArrayList){
                    if (item.getNama_produk().toLowerCase().contains(filterpattern)){
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            produks.clear();
            produks.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
