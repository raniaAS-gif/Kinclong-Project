package com.kinclong.model;

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
}