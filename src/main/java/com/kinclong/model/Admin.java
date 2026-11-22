package com.kinclong.model;

import com.kinclong.dao.KaryawanDAO;
import com.kinclong.dao.LayananDAO;
import com.kinclong.dao.TransaksiDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Admin extends Karyawan {

    public Admin() {
        setJabatan("ADMIN");
    }

    public Admin(int idKaryawan, String namaKaryawan, String nomorTelepon,
                 String username, String password, String status) {
        super(idKaryawan, namaKaryawan, nomorTelepon, "ADMIN", username, password, status);
    }

    @Override
    public String tampilkanDashboard() {
        return "com/kinclong/adminContent/DashboardAdmin.fxml";
    }
    
    public boolean tambahLayanan(Layanan layanan) {
        return new LayananDAO().insert(layanan);
    }

    public boolean updateLayanan(Layanan layanan) {
        return new LayananDAO().update(layanan);
    }

    public boolean hapusLayanan(int idLayanan) {
        return new LayananDAO().delete(idLayanan);
    }

    public boolean tambahPegawai(Karyawan karyawanBaru, String plainPassword) {
        return new KaryawanDAO().insert(karyawanBaru, plainPassword);
    }

    public boolean updatePegawai(Karyawan karyawan) {
        return new KaryawanDAO().update(karyawan);
    }

    public boolean updatePegawai(Karyawan karyawan, String plainPasswordBaru) {
        return new KaryawanDAO().update(karyawan, plainPasswordBaru);
    }

    public boolean ubahStatusPegawai(int idKaryawan, String status) {
        if (!"AKTIF".equalsIgnoreCase(status) && !"NONAKTIF".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Status harus bernilai AKTIF atau NONAKTIF");
        }
        if (idKaryawan == this.getIdKaryawan() && "NONAKTIF".equalsIgnoreCase(status)) {
            return false;
        }
        return new KaryawanDAO().ubahStatusPegawai(idKaryawan, status.toUpperCase());
    }
    public List<Transaksi> generateLaporan() {
        return new TransaksiDAO().getAllUntukUpdateStatus();
    }

    public List<Transaksi> generateLaporan(LocalDate dariTanggal, LocalDate sampaiTanggal) {
        return generateLaporan().stream()
                .filter(t -> t.getTanggalTransaksi() != null
                        && !t.getTanggalTransaksi().isBefore(dariTanggal)
                        && !t.getTanggalTransaksi().isAfter(sampaiTanggal))
                .collect(Collectors.toList());
    }
}