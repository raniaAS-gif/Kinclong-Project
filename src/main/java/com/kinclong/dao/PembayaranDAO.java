package com.kinclong.dao;

import com.kinclong.model.Pembayaran;
import com.kinclong.model.Transaksi;
import com.kinclong.util.DatabaseConnection;

import java.sql.*;

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
}