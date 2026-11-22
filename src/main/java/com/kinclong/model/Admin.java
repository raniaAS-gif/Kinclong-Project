package com.kinclong.model;

import com.kinclong.dao.KaryawanDAO;

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
        return "/com/kinclong/dashboardAdmin.fxml";
    }

    public boolean ubahStatusPegawai(int idKaryawan, String status) {
        if (!"AKTIF".equalsIgnoreCase(status) && !"NONAKTIF".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Status harus bernilai AKTIF atau NONAKTIF");
        }
        if (idKaryawan == this.getIdKaryawan() && "NONAKTIF".equalsIgnoreCase(status)) {
            return false;
        }
        KaryawanDAO dao = new KaryawanDAO();
        return dao.ubahStatusPegawai(idKaryawan, status.toUpperCase());
    }
}