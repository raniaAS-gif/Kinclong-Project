package com.kinclong.model;

public class Kasir extends Karyawan {
    public Kasir() {
        setJabatan("KASIR");
    }

    public Kasir(int idKaryawan, String namaKaryawan, String nomorTelepon,
                 String username, String password, String status) {
        super(idKaryawan, namaKaryawan, nomorTelepon, "KASIR", username, password, status);
    }

    @Override
    public String tampilkanDashboard() {
        return "/com/kinclong/dashboardKasir.fxml";
    }

    public boolean validasiProsesPembayaran(Transaksi transaksi) {
        return transaksi != null && "SUDAH".equalsIgnoreCase(transaksi.getStatusCucian());
    }
}