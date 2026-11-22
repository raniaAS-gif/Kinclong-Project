package com.kinclong.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.kinclong.model.Pembayaran;
import com.kinclong.model.Transaksi;
import com.kinclong.util.DatabaseConnection;

public class PembayaranDAO {

    public boolean prosesPelunasan(Transaksi transaksi, Pembayaran pembayaran) {
        String sql = "INSERT INTO pembayaran (id_transaksi, metode_bayar, tanggal_bayar, nominal_uang) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Pastikan metode non-tunai selalu tercatat pas sesuai total tagihan
            double nominalFinal = pembayaran.getNominalUang();
            if ("QRIS".equalsIgnoreCase(pembayaran.getMetodeBayar()) ||
                "TRANSFER".equalsIgnoreCase(pembayaran.getMetodeBayar())) {
                nominalFinal = transaksi.getTotalBayar();
                pembayaran.setNominalUang(nominalFinal);
            }

            ps.setInt(1, transaksi.getIdTransaksi());
            ps.setString(2, pembayaran.getMetodeBayar());
            ps.setDate(3, Date.valueOf(pembayaran.getTanggalBayar()));
            ps.setDouble(4, nominalFinal);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        pembayaran.setIdPembayaran(keys.getInt(1));
                    }
                }
                transaksi.setPembayaran(pembayaran);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

        // 1. Jumlah transaksi siap bayar (status SUDAH) yang belum lunas
        public int getJumlahBelumLunas() {
            String sql = "SELECT COUNT(*) FROM transaksi t " +
                        "LEFT JOIN pembayaran pb ON t.id_transaksi = pb.id_transaksi " +
                        "WHERE t.status_cucian = 'SUDAH' AND pb.id_pembayaran IS NULL";
            try (Connection conn = DatabaseConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        // 2. Jumlah transaksi yang sudah dilunasi hari ini
        public int getJumlahLunasHariIni() {
            String sql = "SELECT COUNT(*) FROM pembayaran WHERE tanggal_bayar = CURDATE()";
            try (Connection conn = DatabaseConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException e) {
            e.printStackTrace();
            }
            return 0;
        }

        // 3. Total nominal tagihan yang belum lunas
        public double getTotalSisaTagihan() {
            String sql = "SELECT COALESCE(SUM(t.total_bayar), 0) FROM transaksi t " +
                        "LEFT JOIN pembayaran pb ON t.id_transaksi = pb.id_transaksi " +
                        "WHERE t.status_cucian = 'SUDAH' AND pb.id_pembayaran IS NULL";
            try (Connection conn = DatabaseConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
        }

        // 4. Mengambil nama kasir pencatat transaksi untuk ditampilkan di nota pelunasan
        public String getNamaKasirByTransaksi(int idTransaksi) {
            String sql = "SELECT k.nama_karyawan FROM transaksi t " +
                        "JOIN karyawan k ON t.id_karyawan = k.id_karyawan " +
                        "WHERE t.id_transaksi = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idTransaksi);
                try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("nama_karyawan");
                }
            } catch (SQLException e) {
            e.printStackTrace();
            }
            return "-";
        }
}