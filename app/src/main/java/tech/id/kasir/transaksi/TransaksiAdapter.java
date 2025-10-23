package tech.id.kasir.transaksi;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import tech.id.kasir.R;
import tech.id.kasir.response_api.OrderItemRequest;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ListViewHolder>{
    public final static Locale localeID = new Locale("in", "ID");
    public static NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
    Activity context;
    ArrayList<OrderItemRequest> modelProdukFinalTransaksis;
    String noMeja;
    public TransaksiAdapter(ArrayList<OrderItemRequest> modelProdukFinalTransaksis, Activity context, String noMeja) {
        this.context = context;
        this.modelProdukFinalTransaksis = modelProdukFinalTransaksis;
        this.noMeja = noMeja;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_transaksi_finish, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        OrderItemRequest modelProdukFinalTransaksi = modelProdukFinalTransaksis.get(holder.getAdapterPosition());
        holder.tvDaftarProduk.setText(modelProdukFinalTransaksi.getNama_menu());
        holder.tvKuantitasDaftarPrduk.setText(String.valueOf(modelProdukFinalTransaksi.getJumlah()));
        holder.tvJumlahTransaksi.setText(formatRupiah.format( modelProdukFinalTransaksi.getJumlah() * Double.parseDouble(modelProdukFinalTransaksi.getHarga_satuan())));
    }

    @Override
    public int getItemCount() {
        return modelProdukFinalTransaksis.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        TextView tvDaftarProduk, tvKuantitasDaftarPrduk, tvJumlahTransaksi;
        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDaftarProduk= itemView.findViewById(R.id.tvDaftarProduk);
            tvKuantitasDaftarPrduk= itemView.findViewById(R.id.tvKuantitasDaftarPrduk);
            tvJumlahTransaksi= itemView.findViewById(R.id.tvJumlahTransaksi);

        }
    }
}
