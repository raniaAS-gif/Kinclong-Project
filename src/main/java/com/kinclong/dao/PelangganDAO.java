package com.kinclong.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.kinclong.model.Pelanggan;
import com.kinclong.util.DatabaseConnection;

public class PelangganDAO {

    public List<Pelanggan> getAll() {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT * FROM pelanggan ORDER BY id_pelanggan DESC";
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

    public List<Pelanggan> cariPelanggan(String keyword) {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT * FROM pelanggan WHERE nama LIKE ? OR nomor_telepon LIKE ? OR CAST(id_pelanggan AS CHAR) LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pola = "%" + keyword + "%";
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

    public boolean insert(Pelanggan p) {
        String sql = "INSERT INTO pelanggan (nama, nomor_telepon, alamat) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNama());
            ps.setString(2, p.getNomorTelepon());
            ps.setString(3, p.getAlamat());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setIdPelanggan(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Pelanggan p) {
        String sql = "UPDATE pelanggan SET nama = ?, nomor_telepon = ?, alamat = ? WHERE id_pelanggan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNama());
            ps.setString(2, p.getNomorTelepon());
            ps.setString(3, p.getAlamat());
            ps.setInt(4, p.getIdPelanggan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idPelanggan) {
        String sql = "DELETE FROM pelanggan WHERE id_pelanggan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            // Terjadi jika id_pelanggan masih digunakan sebagai foreign key di tabel transaksi
            System.err.println("Pelanggan ID " + idPelanggan + " gagal dihapus karena sudah memiliki riwayat transaksi.");
            return false;
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                System.err.println("Pelanggan ID " + idPelanggan + " gagal dihapus karena sudah memiliki riwayat transaksi.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    private Pelanggan mapRow(ResultSet rs) throws SQLException {
        String alamat = rs.getString("alamat");
        return new Pelanggan(
                rs.getInt("id_pelanggan"),
                rs.getString("nama"),
                rs.getString("nomor_telepon"),
                alamat != null ? alamat : ""
        );
    }
}