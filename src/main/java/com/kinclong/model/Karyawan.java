package com.kinclong.model;

public abstract class Karyawan {
    private int idKaryawan;
    private String namaKaryawan;
    private String nomorTelepon;
    private String jabatan;
    private String username;
    private String password;
    private String status;

    public Karyawan() {
        this.status = "AKTIF";
    }

    public Karyawan(int idKaryawan, String namaKaryawan, String nomorTelepon,
                    String jabatan, String username, String password, String status) {
        this.idKaryawan = idKaryawan;
        this.namaKaryawan = namaKaryawan;
        this.nomorTelepon = nomorTelepon;
        this.jabatan = jabatan;
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public boolean validasiKredensial(String inputUsername, String hashedPassword) {
        return this.username != null && this.username.equals(inputUsername)
                && this.password != null && this.password.equals(hashedPassword);
    }

    public abstract String tampilkanDashboard();

    public boolean isAktif() {
        return "AKTIF".equalsIgnoreCase(this.status);
    }

    public int getIdKaryawan() { return idKaryawan; }
    public void setIdKaryawan(int idKaryawan) { this.idKaryawan = idKaryawan; }

    public String getNamaKaryawan() { return namaKaryawan; }
    public void setNamaKaryawan(String namaKaryawan) { this.namaKaryawan = namaKaryawan; }

    public String getNomorTelepon() { return nomorTelepon; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }

    public String getJabatan() { return jabatan; }
    public void setJabatan(String jabatan) { this.jabatan = jabatan; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}