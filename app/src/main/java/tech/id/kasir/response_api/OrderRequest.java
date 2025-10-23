package tech.id.kasir.response_api;

import java.util.List;

public class OrderRequest {
    private int restoran_id;
    private int pengguna_id;
    private String nomor_invoice;
    private String meja;
    private int total_harga;
    private String status;
    private String catatan;
    private List<OrderItemRequest> items;

    public int getRestoran_id() {
        return restoran_id;
    }

    public void setRestoran_id(int restoran_id) {
        this.restoran_id = restoran_id;
    }

    public int getPengguna_id() {
        return pengguna_id;
    }

    public void setPengguna_id(int pengguna_id) {
        this.pengguna_id = pengguna_id;
    }

    public String getNomor_invoice() {
        return nomor_invoice;
    }

    public void setNomor_invoice(String nomor_invoice) {
        this.nomor_invoice = nomor_invoice;
    }

    public String getMeja() {
        return meja;
    }

    public void setMeja(String meja) {
        this.meja = meja;
    }

    public int getTotal_harga() {
        return total_harga;
    }

    public void setTotal_harga(int total_harga) {
        this.total_harga = total_harga;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
