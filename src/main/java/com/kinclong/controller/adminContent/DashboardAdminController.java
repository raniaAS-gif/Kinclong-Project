package com.kinclong.controller.adminContent;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.kinclong.controller.MainLayoutController;
import com.kinclong.controller.SidebarSwitching;
import com.kinclong.dao.KaryawanDAO;
import com.kinclong.dao.LayananDAO;
import com.kinclong.dao.PelangganDAO;
import com.kinclong.dao.TransaksiDAO;
import com.kinclong.dao.TransaksiDAO.LayananTerlarisItem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class DashboardAdminController implements SidebarSwitching {

    private MainLayoutController mainLayoutController;

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @FXML private Label lblTotalPelanggan;
    @FXML private Label lblJenisLayanan;
    @FXML private Label lblKasirAktif;
    @FXML private Label lblOmzetBulanIni;
    @FXML private Label lblRentangTanggal;

    @FXML private BarChart<String, Number> barChartPendapatan;
    @FXML private CategoryAxis xAxisHari;
    @FXML private NumberAxis yAxisNominal;

    @FXML private VBox vboxLayananTerlaris;

    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final LayananDAO layananDAO = new LayananDAO();
    private final KaryawanDAO karyawanDAO = new KaryawanDAO();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    private final Locale localeID = Locale.forLanguageTag("id-ID");

    @FXML
    public void initialize() {
        updateEmpatKartuKPI();
        muatGrafik7Hari();
        muatTop4LayananMingguIni();
    }

    public void updateEmpatKartuKPI() {
        lblTotalPelanggan.setText(String.valueOf(pelangganDAO.getAll().size()));
        lblJenisLayanan.setText(String.valueOf(layananDAO.getAll().size()));
        lblKasirAktif.setText(String.valueOf(karyawanDAO.getTotalKasirAktif()));

        NumberFormat formatRupiah = NumberFormat.getNumberInstance(localeID);
        formatRupiah.setMaximumFractionDigits(0);
        lblOmzetBulanIni.setText("Rp. " + formatRupiah.format(transaksiDAO.getOmzetBulanIni()));
    }

    private void muatGrafik7Hari() {
        Map<LocalDate, Double> data7Hari = transaksiDAO.getPendapatan7HariTerakhir();

        LocalDate akhir = LocalDate.now();
        LocalDate awal = akhir.minusDays(6);
        DateTimeFormatter fmtBulanTahun = DateTimeFormatter.ofPattern("MMMM yyyy", localeID);
        lblRentangTanggal.setText(awal.getDayOfMonth() + "–" + akhir.getDayOfMonth() + " " + akhir.format(fmtBulanTahun));

        // Konfigurasi struktural BarChart & Axis di Controller
        barChartPendapatan.setAnimated(false);
        barChartPendapatan.setLegendVisible(false);
        barChartPendapatan.setBarGap(4);
        barChartPendapatan.setCategoryGap(24);
        barChartPendapatan.setVerticalGridLinesVisible(false);
        barChartPendapatan.setAlternativeRowFillVisible(false);

        xAxisHari.setAnimated(false);
        xAxisHari.setTickMarkVisible(false);
        xAxisHari.setTickLabelFill(Color.web("#475569"));

        yAxisNominal.setAnimated(false);
        yAxisNominal.setAutoRanging(false);
        yAxisNominal.setLowerBound(0.0);
        yAxisNominal.setMinorTickVisible(false);
        yAxisNominal.setTickMarkVisible(false);
        yAxisNominal.setTickLabelFill(Color.web("#475569"));

        yAxisNominal.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number object) {
                double val = object.doubleValue();
                if (val == 0) return "0";
                return String.format(localeID, "%.1f", val);
            }
            @Override
            public Number fromString(String string) {
                return Double.parseDouble(string.replace(",", "."));
            }
        });

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        double maxJuta = 0.0;
        String hariPuncak = "";

        for (Map.Entry<LocalDate, Double> entry : data7Hari.entrySet()) {
            String labelHari = getNamaHariSingkat(entry.getKey().getDayOfWeek());
            double dalamJuta = entry.getValue() / 1_000_000.0;
            if (dalamJuta >= maxJuta) {
                maxJuta = dalamJuta;
                hariPuncak = labelHari;
            }
            series.getData().add(new XYChart.Data<>(labelHari, dalamJuta));
        }

        double upper = Math.max(1.5, Math.ceil(maxJuta * 2.0) / 2.0);
        yAxisNominal.setUpperBound(upper);
        yAxisNominal.setTickUnit(upper / 3.0);

        barChartPendapatan.getData().clear();
        barChartPendapatan.getData().add(series);

        // Terapkan styling visual BarChart langsung di Controller
        terapkanStylingBarChart(series, hariPuncak, maxJuta);
    }

    private void terapkanStylingBarChart(XYChart.Series<String, Number> series, String hariPuncak, double maxJuta) {
        Runnable applyStyles = () -> {
            Node plotBg = barChartPendapatan.lookup(".chart-plot-background");
            if (plotBg != null) {
                plotBg.setStyle("-fx-background-color: transparent;");
            }

            Node hGrid = barChartPendapatan.lookup(".chart-horizontal-grid-lines");
            if (hGrid != null) {
                hGrid.setStyle("-fx-stroke: #E2E8F0; -fx-stroke-width: 1;");
            }

            Node xAxisLine = xAxisHari.lookup(".axis");
            if (xAxisLine != null) {
                xAxisLine.setStyle("-fx-border-color: transparent;");
            }

            Node yAxisLine = yAxisNominal.lookup(".axis");
            if (yAxisLine != null) {
                yAxisLine.setStyle("-fx-border-color: transparent;");
            }

            // Warna batang: biru muda (#B8D6F8) dan biru tua (#11559C) untuk hari tertinggi
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node barNode = data.getNode();
                if (barNode != null) {
                    boolean isPuncak = maxJuta > 0 && data.getXValue().equals(hariPuncak);
                    String warna = isPuncak ? "#11559C" : "#B8D6F8";
                    barNode.setStyle("-fx-bar-fill: " + warna + "; -fx-background-radius: 6 6 0 0;");
                }
            }

            // Tebalkan label hari tertinggi pada sumbu X
            for (Node node : xAxisHari.lookupAll("Text")) {
                if (node instanceof Text textNode) {
                    if (maxJuta > 0 && textNode.getText().equals(hariPuncak)) {
                        textNode.setStyle("-fx-fill: #11559C; -fx-font-weight: bold;");
                    } else {
                        textNode.setStyle("-fx-fill: #475569;");
                    }
                }
            }
        };

        Platform.runLater(applyStyles);
        barChartPendapatan.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(applyStyles);
            }
        });
    }

    private void muatTop4LayananMingguIni() {
        vboxLayananTerlaris.getChildren().clear();
        List<LayananTerlarisItem> top4 = transaksiDAO.getTop4LayananMingguIni();

        if (top4.isEmpty()) {
            Label kosong = new Label("Belum ada transaksi minggu ini.");
            kosong.setStyle("-fx-text-fill: #64748B;");
            vboxLayananTerlaris.getChildren().add(kosong);
            return;
        }

        // Hanya 4 item dengan Vgrow ALWAYS agar jaraknya mengisi seluruh tinggi kotak
        for (LayananTerlarisItem item : top4) {
            VBox rowBox = new VBox(6);
            rowBox.setAlignment(Pos.CENTER_LEFT);
            VBox.setVgrow(rowBox, Priority.ALWAYS);

            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);

            Label lblNama = new Label(item.getNamaLayanan());
            lblNama.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: bold; -fx-font-size: 13px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblPersen = new Label(String.format(localeID, "%.0f%% (%dx)", item.getPersentase(), item.getJumlahOrder()));
            lblPersen.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

            header.getChildren().addAll(lblNama, spacer, lblPersen);

            ProgressBar pb = new ProgressBar(item.getPersentase() / 100.0);
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.setPrefHeight(8);
            pb.setStyle("-fx-accent: #11559C;");

            rowBox.getChildren().addAll(header, pb);
            vboxLayananTerlaris.getChildren().add(rowBox);
        }
    }

    // --- NAVIGASI AKSI CEPAT ---
    @FXML
    private void handleAksiTambahKaryawan() {
        pindahKeMasterData(KelolaMasterDataController::bukaTabKaryawan);
    }

    @FXML
    private void handleAksiTambahLayanan() {
        pindahKeMasterData(KelolaMasterDataController::bukaTabLayanan);
    }

    @FXML
    private void handleAksiLihatLaporanKeuangan() {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/adminContent/LaporanPendapatan.fxml");
        }
    }

    private void pindahKeMasterData(Consumer<KelolaMasterDataController> tabSelector) {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/adminContent/KelolaMasterData.fxml", controller -> {
                if (controller instanceof KelolaMasterDataController masterCtrl && tabSelector != null) {
                    tabSelector.accept(masterCtrl);
                }
            });
        }
    }

    private void gantiHalamanTengah(String fxmlPath, Consumer<Object> onLoaded) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            if (onLoaded != null) {
                onLoaded.accept(loader.getController());
            }
            Node root = vboxLayananTerlaris.getScene().getRoot();
            if (root instanceof BorderPane borderPane) {
                borderPane.setCenter(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getNamaHariSingkat(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Sen";
            case TUESDAY -> "Sel";
            case WEDNESDAY -> "Rab";
            case THURSDAY -> "Kam";
            case FRIDAY -> "Jum";
            case SATURDAY -> "Sab";
            case SUNDAY -> "Min";
        };
    }
}