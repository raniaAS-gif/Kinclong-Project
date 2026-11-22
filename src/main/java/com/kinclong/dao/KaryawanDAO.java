package com.kinclong.dao;

import com.kinclong.model.Admin;
import com.kinclong.model.Karyawan;
import com.kinclong.model.Kasir;
import com.kinclong.util.DatabaseConnection;
import com.kinclong.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KaryawanDAO {

    public Karyawan login(String username, String plainPassword) throws Exception {
        String sql = "SELECT * FROM karyawan WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Karyawan k = mapRow(rs);
                    String hashedInput = PasswordUtil.hashSHA256(plainPassword);
                    if (!k.validasiKredensial(username, hashedInput)) {
                        return null;
                    }
                    if (!k.isAktif()) {
                        throw new Exception("Akun Anda telah dinonaktifkan oleh Admin.");
                    }
                    return k;
                }
            }
        }
        return null;
    }

    public List<Karyawan> getAll() {
        List<Karyawan> list = new ArrayList<>();
        String sql = "SELECT * FROM karyawan ORDER BY id_karyawan ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Karyawan> cariKaryawan(String keyword) {
        List<Karyawan> list = new ArrayList<>();
        String sql = "SELECT * FROM karyawan WHERE nama_karyawan LIKE ? OR username LIKE ? OR CAST(id_karyawan AS CHAR) LIKE ? ORDER BY id_karyawan ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pola = "%" + (keyword == null ? "" : keyword) + "%";
            ps.setString(1, pola);
            ps.setString(2, pola);
            ps.setString(3, pola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Karyawan k, String plainPassword) {
        String sql = "INSERT INTO karyawan (nama_karyawan, nomor_telepon, jabatan, username, password, status) VALUES (?, ?, ?, ?, ?, 'AKTIF')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k.getNamaKaryawan());
            ps.setString(2, k.getNomorTelepon());
            ps.setString(3, k.getJabatan());
            ps.setString(4, k.getUsername());
            ps.setString(5, PasswordUtil.hashSHA256(plainPassword));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Karyawan k) {
        String sql = "UPDATE karyawan SET nama_karyawan = ?, nomor_telepon = ?, jabatan = ?, username = ? WHERE id_karyawan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k.getNamaKaryawan());
            ps.setString(2, k.getNomorTelepon());
            ps.setString(3, k.getJabatan());
            ps.setString(4, k.getUsername());
            ps.setInt(5, k.getIdKaryawan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Karyawan k, String plainPasswordBaru) {
        String sql = "UPDATE karyawan SET nama_karyawan = ?, nomor_telepon = ?, jabatan = ?, username = ?, password = ? WHERE id_karyawan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k.getNamaKaryawan());
            ps.setString(2, k.getNomorTelepon());
            ps.setString(3, k.getJabatan());
            ps.setString(4, k.getUsername());
            ps.setString(5, PasswordUtil.hashSHA256(plainPasswordBaru));
            ps.setInt(6, k.getIdKaryawan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean ubahStatusPegawai(int idKaryawan, String status) {
        if (!"AKTIF".equalsIgnoreCase(status) && !"NONAKTIF".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Status harus bernilai AKTIF atau NONAKTIF");
        }

        String sql = "UPDATE karyawan SET status = ? WHERE id_karyawan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.toUpperCase());
            ps.setInt(2, idKaryawan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Karyawan mapRow(ResultSet rs) throws SQLException {
        String jabatan = rs.getString("jabatan");
        Karyawan k = "ADMIN".equalsIgnoreCase(jabatan) ? new Admin() : new Kasir();
        k.setIdKaryawan(rs.getInt("id_karyawan"));
        k.setNamaKaryawan(rs.getString("nama_karyawan"));
        k.setNomorTelepon(rs.getString("nomor_telepon"));
        k.setJabatan(jabatan);
        k.setUsername(rs.getString("username"));
        k.setPassword(rs.getString("password"));
        k.setStatus(rs.getString("status"));
        return k;
    }
}