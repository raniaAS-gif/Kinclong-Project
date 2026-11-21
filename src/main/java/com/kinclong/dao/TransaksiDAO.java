package com.kinclong.dao;

import com.kinclong.model.*;
import com.kinclong.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {

    public boolean simpanTransaksiBaru(Transaksi t) {
        String sqlTransaksi = "INSERT INTO transaksi (tanggal_masuk, tanggal_transaksi, total_bayar, status_cucian, id_pelanggan, id_karyawan) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO detail_transaksi (id_transaksi, nomor_urut, id_layanan, jumlah_kuantitas, keterangan_pakaian, total_harga) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psTrans = conn.prepareStatement(sqlTransaksi, Statement.RETURN_GENERATED_KEYS)) {
                psTrans.setDate(1, Date.valueOf(t.getTanggalMasuk()));
                psTrans.setDate(2, Date.valueOf(t.getTanggalTransaksi()));
                psTrans.setDouble(3, t.getTotalBayar());
                psTrans.setString(4, t.getStatusCucian());
                psTrans.setInt(5, t.getPelanggan().getIdPelanggan());
                psTrans.setInt(6, t.getKasir().getIdKaryawan());
                psTrans.executeUpdate();

                try (ResultSet keys = psTrans.getGeneratedKeys()) {
                    if (keys.next()) {
                        t.setIdTransaksi(keys.getInt(1));
                    }
                }

                try (PreparedStatement psDet = conn.prepareStatement(sqlDetail)) {
                    for (DetailTransaksi d : t.getListDetail()) {
                        psDet.setInt(1, t.getIdTransaksi());
                        psDet.setInt(2, d.getNomorUrut());
                        psDet.setInt(3, d.getLayanan().getIdLayanan());
                        psDet.setDouble(4, d.getJumlah());
                        psDet.setString(5, d.getKeterangan());
                        psDet.setDouble(6, d.getTotalHarga());
                        psDet.addBatch();
                    }
                    psDet.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaksi> getCucianAktif(String keyword) {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT t.*, p.nama AS nama_pelanggan, p.nomor_telepon " +
                     "FROM transaksi t " +
                     "JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                     "WHERE t.status_cucian IN ('BELUM', 'DALAM_PROSES') " +
                     "AND (p.nama LIKE ? OR p.nomor_telepon LIKE ? OR CAST(t.id_transaksi AS CHAR) LIKE ?) " +
                     "ORDER BY t.tanggal_masuk ASC";
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

    public boolean updateStatusCucian(int idTransaksi, String statusBaru) {
        String sql = "UPDATE transaksi SET status_cucian = ? WHERE id_transaksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusBaru);
            ps.setInt(2, idTransaksi);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaksi> getTagihanSiapBayar(String keyword) {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT t.*, p.nama AS nama_pelanggan, p.nomor_telepon " +
                     "FROM transaksi t " +
                     "JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                     "LEFT JOIN pembayaran pb ON t.id_transaksi = pb.id_transaksi " +
                     "WHERE t.status_cucian = 'SUDAH' AND pb.id_pembayaran IS NULL " +
                     "AND (p.nama LIKE ? OR p.nomor_telepon LIKE ? OR CAST(t.id_transaksi AS CHAR) LIKE ?) " +
                     "ORDER BY t.id_transaksi DESC";
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

    public List<DetailTransaksi> getDetailByTransaksi(int idTransaksi) {
        List<DetailTransaksi> list = new ArrayList<>();
        String sql = "SELECT d.*, l.nama_layanan, l.jenis_satuan, l.harga_per_unit " +
                     "FROM detail_transaksi d " +
                     "JOIN layanan l ON d.id_layanan = l.id_layanan " +
                     "WHERE d.id_transaksi = ? ORDER BY d.nomor_urut ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTransaksi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Layanan l = new Layanan(
                            rs.getInt("id_layanan"),
                            rs.getString("nama_layanan"),
                            rs.getString("jenis_satuan"),
                            rs.getDouble("harga_per_unit")
                    );
                    DetailTransaksi d = new DetailTransaksi(
                            rs.getInt("nomor_urut"),
                            l,
                            rs.getDouble("jumlah_kuantitas"),
                            rs.getString("keterangan_pakaian")
                    );
                    d.setTotalHarga(rs.getDouble("total_harga"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Transaksi mapRow(ResultSet rs) throws SQLException {
        Transaksi t = new Transaksi();
        t.setIdTransaksi(rs.getInt("id_transaksi"));
        t.setTanggalMasuk(rs.getDate("tanggal_masuk").toLocalDate());
        t.setTanggalTransaksi(rs.getDate("tanggal_transaksi").toLocalDate());
        t.setTotalBayar(rs.getDouble("total_bayar"));
        t.setStatusCucian(rs.getString("status_cucian"));

        Pelanggan p = new Pelanggan();
        p.setIdPelanggan(rs.getInt("id_pelanggan"));
        p.setNama(rs.getString("nama_pelanggan"));
        p.setNomorTelepon(rs.getString("nomor_telepon"));
        t.setPelanggan(p);
        return t;
    }
}