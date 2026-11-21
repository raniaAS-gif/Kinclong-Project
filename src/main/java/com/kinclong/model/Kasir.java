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

    // Validasi aturan baru: pembayaran hanya bisa dilakukan jika cucian sudah 'SUDAH'
    public boolean validasiProsesPembayaran(Transaksi transaksi) {
        return transaksi != null && "SUDAH".equalsIgnoreCase(transaksi.getStatusCucian());
    }
}