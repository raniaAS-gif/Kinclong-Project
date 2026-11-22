package com.kinclong.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.kinclong.model.DetailTransaksi;
import com.kinclong.model.Layanan;
import com.kinclong.model.Pelanggan;
import com.kinclong.model.Transaksi;
import com.kinclong.util.DatabaseConnection;

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

    public List<Transaksi> getAllUntukUpdateStatus() {
        List<Transaksi> list = new ArrayList<>();
        String sql ="SELECT t.*, p.nama AS nama_pelanggan, p.nomor_telepon " +
                    "FROM transaksi t " +
                    "JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                    "ORDER BY t.id_transaksi DESC";
        try (Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Transaksi t = mapRow(rs);
                t.setListDetail(getDetailByTransaksi(t.getIdTransaksi()));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
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

    public int getNextIdTransaksi() {
        String sql = "SELECT COALESCE(MAX(id_transaksi), 0) + 1 AS next_id FROM transaksi";
        try (Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    // --- DTO untuk Dashboard Admin & Laporan Pendapatan ---
    public static class LayananTerlarisItem {
        private final String namaLayanan;
        private final int jumlahOrder;
        private final double persentase;

        public LayananTerlarisItem(String namaLayanan, int jumlahOrder, double persentase) {
            this.namaLayanan = namaLayanan;
            this.jumlahOrder = jumlahOrder;
            this.persentase = persentase;
        }
        public String getNamaLayanan() { return namaLayanan; }
        public int getJumlahOrder() { return jumlahOrder; }
        public double getPersentase() { return persentase; }
    }

    public static class LaporanItem {
        private final int no;
        private final String noNota;
        private final java.time.LocalDate tanggal;
        private final String namaKasir;
        private final double totalPendapatan;

        public LaporanItem(int no, String noNota, java.time.LocalDate tanggal, String namaKasir, double totalPendapatan) {
            this.no = no;
            this.noNota = noNota;
            this.tanggal = tanggal;
            this.namaKasir = namaKasir;
            this.totalPendapatan = totalPendapatan;
        }
        public int getNo() { return no; }
        public String getNoNota() { return noNota; }
        public java.time.LocalDate getTanggal() { return tanggal; }
        public String getNamaKasir() { return namaKasir; }
        public double getTotalPendapatan() { return totalPendapatan; }
    }

    // 1. Total omzet bulan berjalan
    public double getOmzetBulanIni() {
        String sql = "SELECT COALESCE(SUM(total_bayar), 0) FROM transaksi " +
                    "WHERE MONTH(tanggal_transaksi) = MONTH(CURDATE()) " +
                    "AND YEAR(tanggal_transaksi) = YEAR(CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // 2. Pendapatan 7 hari terakhir (urut dari 6 hari lalu hingga hari ini)
    public java.util.Map<java.time.LocalDate, Double> getPendapatan7HariTerakhir() {
        java.util.Map<java.time.LocalDate, Double> hasil = new java.util.LinkedHashMap<>();
        java.time.LocalDate hariIni = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            hasil.put(hariIni.minusDays(i), 0.0);
        }

        String sql = "SELECT tanggal_transaksi, COALESCE(SUM(total_bayar), 0) AS total " +
                    "FROM transaksi " +
                    "WHERE tanggal_transaksi BETWEEN ? AND ? " +
                    "GROUP BY tanggal_transaksi";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(hariIni.minusDays(6)));
            ps.setDate(2, Date.valueOf(hariIni));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.time.LocalDate tgl = rs.getDate("tanggal_transaksi").toLocalDate();
                    hasil.put(tgl, rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasil;
    }

    // 3. Hanya 4 layanan terlaris dalam 7 hari terakhir (minggu ini)
    public List<LayananTerlarisItem> getTop4LayananMingguIni() {
        List<LayananTerlarisItem> list = new ArrayList<>();
        java.time.LocalDate hariIni = java.time.LocalDate.now();
        java.time.LocalDate awalMinggu = hariIni.minusDays(6);

        String sqlTotal = "SELECT COUNT(*) FROM detail_transaksi dt " +
                        "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi " +
                        "WHERE t.tanggal_transaksi BETWEEN ? AND ?";

        String sqlTop4 = "SELECT l.nama_layanan, COUNT(dt.id_layanan) AS jml " +
                        "FROM detail_transaksi dt " +
                        "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi " +
                        "JOIN layanan l ON dt.id_layanan = l.id_layanan " +
                        "WHERE t.tanggal_transaksi BETWEEN ? AND ? " +
                        "GROUP BY l.id_layanan, l.nama_layanan " +
                        "ORDER BY jml DESC LIMIT 4";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int totalOrderMingguIni = 0;
            try (PreparedStatement psTot = conn.prepareStatement(sqlTotal)) {
                psTot.setDate(1, Date.valueOf(awalMinggu));
                psTot.setDate(2, Date.valueOf(hariIni));
                try (ResultSet rsTot = psTot.executeQuery()) {
                    if (rsTot.next()) totalOrderMingguIni = rsTot.getInt(1);
                }
            }

            try (PreparedStatement psTop = conn.prepareStatement(sqlTop4)) {
                psTop.setDate(1, Date.valueOf(awalMinggu));
                psTop.setDate(2, Date.valueOf(hariIni));
                try (ResultSet rs = psTop.executeQuery()) {
                    while (rs.next()) {
                        int jml = rs.getInt("jml");
                        double persen = totalOrderMingguIni > 0 ? (jml * 100.0 / totalOrderMingguIni) : 0.0;
                        list.add(new LayananTerlarisItem(rs.getString("nama_layanan"), jml, persen));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 4. Data rekapitulasi laporan pendapatan berdasarkan rentang tanggal
    public List<LaporanItem> getLaporanPendapatan(java.time.LocalDate awal, java.time.LocalDate akhir) {
        List<LaporanItem> list = new ArrayList<>();
        String sql = "SELECT t.id_transaksi, t.tanggal_transaksi, k.nama_karyawan, t.total_bayar " +
                    "FROM transaksi t " +
                    "JOIN karyawan k ON t.id_karyawan = k.id_karyawan " +
                    "WHERE t.tanggal_transaksi BETWEEN ? AND ? " +
                    "ORDER BY t.tanggal_transaksi ASC, t.id_transaksi ASC";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(awal));
            ps.setDate(2, Date.valueOf(akhir));
            try (ResultSet rs = ps.executeQuery()) {
                int no = 1;
                while (rs.next()) {
                    int idTrans = rs.getInt("id_transaksi");
                    list.add(new LaporanItem(
                            no++,
                            String.format("TRX-%04d", idTrans),
                            rs.getDate("tanggal_transaksi").toLocalDate(),
                            rs.getString("nama_karyawan"),
                            rs.getDouble("total_bayar")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Pemasukan berdasarkan selisih hari (0 = hari ini, 1 = kemarin)
    public double getPemasukanHarian(int selisihHari) {
        String sql = "SELECT COALESCE(SUM(total_bayar), 0) FROM transaksi " +
                     "WHERE tanggal_transaksi = DATE_SUB(CURDATE(), INTERVAL ? DAY)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selisihHari);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Pemasukan minggu berjalan (Senin - Minggu)
    public double getPemasukanMingguIni() {
        String sql = "SELECT COALESCE(SUM(total_bayar), 0) FROM transaksi " +
                     "WHERE YEARWEEK(tanggal_transaksi, 1) = YEARWEEK(CURDATE(), 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Jumlah transaksi berdasarkan selisih hari (0 = hari ini, 1 = kemarin)
    public int getJumlahTransaksiHarian(int selisihHari) {
        String sql = "SELECT COUNT(*) FROM transaksi " +
                     "WHERE tanggal_transaksi = DATE_SUB(CURDATE(), INTERVAL ? DAY)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selisihHari);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Jumlah transaksi minggu berjalan
    public int getJumlahTransaksiMingguIni() {
        String sql = "SELECT COUNT(*) FROM transaksi " +
                     "WHERE YEARWEEK(tanggal_transaksi, 1) = YEARWEEK(CURDATE(), 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Jumlah transaksi bulan berjalan
    public int getJumlahTransaksiBulanIni() {
        String sql = "SELECT COUNT(*) FROM transaksi " +
                     "WHERE MONTH(tanggal_transaksi) = MONTH(CURDATE()) " +
                     "AND YEAR(tanggal_transaksi) = YEAR(CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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