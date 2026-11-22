package com.kinclong.model;

public class Layanan {
    private int idLayanan;
    private String namaLayanan;
    private String jenisSatuan;
    private double hargaPerUnit;

    public Layanan() {}

    public Layanan(int idLayanan, String namaLayanan, String jenisSatuan, double hargaPerUnit) {
        this.idLayanan = idLayanan;
        this.namaLayanan = namaLayanan;
        this.jenisSatuan = jenisSatuan;
        this.hargaPerUnit = hargaPerUnit;
    }

    public int getIdLayanan() { return idLayanan; }
    public void setIdLayanan(int idLayanan) { this.idLayanan = idLayanan; }

    public String getNamaLayanan() { return namaLayanan; }
    public void setNamaLayanan(String namaLayanan) { this.namaLayanan = namaLayanan; }

    public String getJenisSatuan() { return jenisSatuan; }
    public void setJenisSatuan(String jenisSatuan) { this.jenisSatuan = jenisSatuan; }

    public double getHargaPerUnit() { return hargaPerUnit; }
    public void setHargaPerUnit(double hargaPerUnit) { this.hargaPerUnit = hargaPerUnit; }

    @Override
    public String toString() {
        return namaLayanan + " - Rp " + String.format("%,.0f", hargaPerUnit) + "/" + jenisSatuan;
    }
}