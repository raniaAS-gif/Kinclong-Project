package com.kinclong.model;

import com.kinclong.dao.PelangganDAO;
import com.kinclong.dao.PembayaranDAO;
import com.kinclong.dao.TransaksiDAO;
import java.util.List;

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

    public boolean tambahPelanggan(Pelanggan pelanggan) {
        return new PelangganDAO().insert(pelanggan);
    }

    public boolean updatePelanggan(Pelanggan pelanggan) {
        return new PelangganDAO().update(pelanggan);
    }

    public List<Pelanggan> cariPelanggan(String keyword) {
        return new PelangganDAO().cariPelanggan(keyword);
    }

    public boolean buatTransaksiBaru(Transaksi transaksi) {
        if (transaksi == null) {
            return false;
        }
        if (transaksi.getKasir() == null) {
            transaksi.setKasir(this);
        }
        return new TransaksiDAO().simpanTransaksiBaru(transaksi);
    }

    public boolean updateStatusCucian(int idTransaksi, String statusBaru) {
        return new TransaksiDAO().updateStatusCucian(idTransaksi, statusBaru);
    }

    public boolean validasiProsesPembayaran(Transaksi transaksi) {
        return transaksi != null
                && "SUDAH".equalsIgnoreCase(transaksi.getStatusCucian())
                && !transaksi.isLunas();
    }

    public boolean prosesPembayaran(Transaksi transaksi, Pembayaran pembayaran) {
        if (!validasiProsesPembayaran(transaksi) || pembayaran == null) {
            return false;
        }
        return new PembayaranDAO().prosesPelunasan(transaksi, pembayaran);
    }
}