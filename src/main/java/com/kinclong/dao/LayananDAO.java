package com.kinclong.dao;

import com.kinclong.model.Layanan;
import com.kinclong.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LayananDAO {

    public List<Layanan> getAll() {
        List<Layanan> list = new ArrayList<>();
        String sql = "SELECT * FROM layanan ORDER BY id_layanan ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Layanan(
                        rs.getInt("id_layanan"),
                        rs.getString("nama_layanan"),
                        rs.getString("jenis_satuan"),
                        rs.getDouble("harga_per_unit")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Layanan l) {
        String sql = "INSERT INTO layanan (nama_layanan, jenis_satuan, harga_per_unit) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getNamaLayanan());
            ps.setString(2, l.getJenisSatuan());
            ps.setDouble(3, l.getHargaPerUnit());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Layanan l) {
        String sql = "UPDATE layanan SET nama_layanan = ?, jenis_satuan = ?, harga_per_unit = ? WHERE id_layanan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getNamaLayanan());
            ps.setString(2, l.getJenisSatuan());
            ps.setDouble(3, l.getHargaPerUnit());
            ps.setInt(4, l.getIdLayanan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idLayanan) {
        String sql = "DELETE FROM layanan WHERE id_layanan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLayanan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}