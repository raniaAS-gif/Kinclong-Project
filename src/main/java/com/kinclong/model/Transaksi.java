package com.kinclong.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Transaksi {
    private int idTransaksi;
    private LocalDate tanggalMasuk;
    private LocalDate tanggalTransaksi;
    private double totalBayar;
    private String statusCucian; // "BELUM", "DALAM_PROSES", "SUDAH"

    // Relasi objek sesuai Class Diagram
    private Pelanggan pelanggan;
    private Kasir kasir;
    private Pembayaran pembayaran;
    private List<DetailTransaksi> listDetail = new ArrayList<>();

    public Transaksi() {
        setTanggalOtomatis();
        this.statusCucian = "BELUM";
    }

    // Method-method sesuai Class Diagram
    public void setTanggalOtomatis() {
        LocalDate hariIni = LocalDate.now();
        this.tanggalMasuk = hariIni;
        this.tanggalTransaksi = hariIni;
    }

    public void tambahDetailItem(DetailTransaksi item) {
        item.setNomorUrut(this.listDetail.size() + 1);
        item.hitungSubtotal();
        this.listDetail.add(item);
        kalkulasiTotalBayar();
    }

    public void hapusDetailItem(DetailTransaksi item) {
        this.listDetail.remove(item);
        // Rapikan ulang nomor urut setelah item dihapus
        for (int i = 0; i < this.listDetail.size(); i++) {
            this.listDetail.get(i).setNomorUrut(i + 1);
        }
        kalkulasiTotalBayar();
    }

    public void kalkulasiTotalBayar() {
        this.totalBayar = this.listDetail.stream()
                .mapToDouble(DetailTransaksi::getTotalHarga)
                .sum();
    }

    // Pengecekan status lunas berdasarkan relasi objek Pembayaran (1 ke 0..1)
    public boolean isLunas() {
        return this.pembayaran != null;
    }

    // Getter & Setter sesuai Class Diagram
    public int getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(int idTransaksi) { this.idTransaksi = idTransaksi; }

    public LocalDate getTanggalMasuk() { return tanggalMasuk; }
    public void setTanggalMasuk(LocalDate tanggalMasuk) { this.tanggalMasuk = tanggalMasuk; }

    public LocalDate getTanggalTransaksi() { return tanggalTransaksi; }
    public void setTanggalTransaksi(LocalDate tanggalTransaksi) { this.tanggalTransaksi = tanggalTransaksi; }

    public double getTotalBayar() { return totalBayar; }
    public void setTotalBayar(double totalBayar) { this.totalBayar = totalBayar; }

    public String getStatusCucian() { return statusCucian; }
    public void setStatusCucian(String statusCucian) { this.statusCucian = statusCucian; }

    public Pelanggan getPelanggan() { return pelanggan; }
    public void setPelanggan(Pelanggan pelanggan) { this.pelanggan = pelanggan; }

    public Kasir getKasir() { return kasir; }
    public void setKasir(Kasir kasir) { this.kasir = kasir; }

    public Pembayaran getPembayaran() { return pembayaran; }
    public void setPembayaran(Pembayaran pembayaran) { this.pembayaran = pembayaran; }

    public List<DetailTransaksi> getListDetail() { return listDetail; }
    public void setListDetail(List<DetailTransaksi> listDetail) { this.listDetail = listDetail; }

    // Helper getter agar kolom TableView JavaFX langsung terbaca
    public String getNamaPelanggan() {
        return pelanggan != null ? pelanggan.getNama() : "";
    }

    public String getNomorTeleponPelanggan() {
        return pelanggan != null ? pelanggan.getNomorTelepon() : "";
    }

    public String getStatusPembayaranText() {
        return isLunas() ? "LUNAS" : "BELUM LUNAS";
    }
}