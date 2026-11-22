package com.kinclong.model;

public class DetailTransaksi {
    private int nomorUrut;
    private double jumlah;
    private double totalHarga;
    private String keterangan;
    private Layanan layanan;

    public DetailTransaksi() {}

    public DetailTransaksi(int nomorUrut, Layanan layanan, double jumlah, String keterangan) {
        this.nomorUrut = nomorUrut;
        this.layanan = layanan;
        this.jumlah = jumlah;
        this.keterangan = keterangan;
        hitungSubtotal();
    }

    public void hitungSubtotal() {
        if (this.layanan != null) {
            this.totalHarga = this.jumlah * this.layanan.getHargaPerUnit();
        }
    }

    public int getNomorUrut() { return nomorUrut; }
    public void setNomorUrut(int nomorUrut) { this.nomorUrut = nomorUrut; }

    public double getJumlah() { return jumlah; }
    public void setJumlah(double jumlah) {
        this.jumlah = jumlah;
        hitungSubtotal();
    }

    public double getTotalHarga() { return totalHarga; }
    public void setTotalHarga(double totalHarga) { this.totalHarga = totalHarga; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public Layanan getLayanan() { return layanan; }
    public void setLayanan(Layanan layanan) {
        this.layanan = layanan;
        hitungSubtotal();
    }

    // --- Helper untuk TableView JavaFX ---
    public String getNamaLayanan() {
        return layanan != null ? layanan.getNamaLayanan() : "";
    }

    public String getJenisSatuan() {
        return layanan != null ? layanan.getJenisSatuan() : "";
    }

    public double getHargaPerUnit() {
        return layanan != null ? layanan.getHargaPerUnit() : 0.0;
    }
}