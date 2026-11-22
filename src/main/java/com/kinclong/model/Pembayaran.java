package com.kinclong.model;

import java.time.LocalDate;

public class Pembayaran {
    private int idPembayaran;
    private String metodeBayar; // "CASH", "QRIS", "TRANSFER"
    private LocalDate tanggalBayar;
    private double nominalUang;

    public Pembayaran() {
        this.tanggalBayar = LocalDate.now();
    }

    public Pembayaran(int idPembayaran, String metodeBayar, LocalDate tanggalBayar, double nominalUang) {
        this.idPembayaran = idPembayaran;
        this.metodeBayar = metodeBayar;
        this.tanggalBayar = tanggalBayar;
        this.nominalUang = nominalUang;
    }

    // Method sesuai Class Diagram
    public double hitungKembalian(double totalTagihan) {
        if ("QRIS".equalsIgnoreCase(metodeBayar) || "TRANSFER".equalsIgnoreCase(metodeBayar)) {
            return 0.0;
        }
        return this.nominalUang - totalTagihan;
    }

    public int getIdPembayaran() { return idPembayaran; }
    public void setIdPembayaran(int idPembayaran) { this.idPembayaran = idPembayaran; }

    public String getMetodeBayar() { return metodeBayar; }
    public void setMetodeBayar(String metodeBayar) { this.metodeBayar = metodeBayar; }

    public LocalDate getTanggalBayar() { return tanggalBayar; }
    public void setTanggalBayar(LocalDate tanggalBayar) { this.tanggalBayar = tanggalBayar; }

    public double getNominalUang() { return nominalUang; }
    public void setNominalUang(double nominalUang) { this.nominalUang = nominalUang; }
}