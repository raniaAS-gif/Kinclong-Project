package com.kinclong.controller.adminContent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.kinclong.dao.TransaksiDAO;
import com.kinclong.dao.TransaksiDAO.LaporanItem;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class LaporanPendapatanController {

    @FXML private DatePicker dpTanggalAwal;
    @FXML private DatePicker dpTanggalAkhir;
    @FXML private VBox boxRekapitulasi;

    @FXML private TableView<LaporanItem> tblLaporan;
    @FXML private TableColumn<LaporanItem, Integer> colNo;
    @FXML private TableColumn<LaporanItem, String> colNoNota;
    @FXML private TableColumn<LaporanItem, String> colTanggal;
    @FXML private TableColumn<LaporanItem, String> colNamaKasir;
    @FXML private TableColumn<LaporanItem, Double> colTotalPendapatan;

    @FXML private Label lblJumlahTransaksi;
    @FXML private Label lblTotalAkumulasi;

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final ObservableList<LaporanItem> dataLaporan = FXCollections.observableArrayList();
    private final Locale localeID = Locale.forLanguageTag("id-ID");
    private final NumberFormat formatRupiah = NumberFormat.getNumberInstance(localeID);
    private final DateTimeFormatter formatTanggal = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML
    public void initialize() {
        formatRupiah.setMaximumFractionDigits(2);

        // Kolom No bertipe Integer
        colNo.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getNo()));

        colNoNota.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNoNota()));
        colTanggal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTanggal().format(formatTanggal)));
        colNamaKasir.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaKasir()));

        // Kolom Total Pendapatan bertipe Double
        colTotalPendapatan.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getTotalPendapatan()));
        colTotalPendapatan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Rp. " + formatRupiah.format(item));
                }
            }
        });

        dpTanggalAwal.setValue(LocalDate.now().withDayOfMonth(1));
        dpTanggalAkhir.setValue(LocalDate.now());
        handleGenerateLaporan();
    }

    @FXML
    private void handleGenerateLaporan() {
        LocalDate awal = dpTanggalAwal.getValue();
        LocalDate akhir = dpTanggalAkhir.getValue();

        if (awal == null || akhir == null) {
            tampilkanAlert(Alert.AlertType.WARNING, "Validasi", "Silakan pilih Tanggal Awal dan Tanggal Akhir.");
            return;
        }
        if (awal.isAfter(akhir)) {
            tampilkanAlert(Alert.AlertType.WARNING, "Validasi", "Tanggal Awal tidak boleh melebihi Tanggal Akhir.");
            return;
        }

        List<LaporanItem> hasil = transaksiDAO.getLaporanPendapatan(awal, akhir);
        dataLaporan.setAll(hasil);
        tblLaporan.setItems(dataLaporan);

        double totalAkumulasi = hasil.stream().mapToDouble(LaporanItem::getTotalPendapatan).sum();
        lblJumlahTransaksi.setText(String.valueOf(hasil.size()));
        lblTotalAkumulasi.setText("Rp. " + formatRupiah.format(totalAkumulasi));
    }

    @FXML
    private void handleExportCSV() {
        if (dataLaporan.isEmpty()) {
            tampilkanAlert(Alert.AlertType.WARNING, "Kosong", "Tidak ada data laporan untuk diekspor.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Simpan Laporan Pendapatan");
        chooser.setInitialFileName("Laporan_Pendapatan_" + dpTanggalAwal.getValue() + "_sd_" + dpTanggalAkhir.getValue() + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = chooser.showSaveDialog(tblLaporan.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("No,No. Nota,Tanggal,Nama Kasir,Total Pendapatan\n");
                for (LaporanItem item : dataLaporan) {
                    writer.write(String.format(Locale.US, "%d,%s,%s,\"%s\",%.2f\n",
                            item.getNo(),
                            item.getNoNota(),
                            item.getTanggal().format(formatTanggal),
                            item.getNamaKasir(),
                            item.getTotalPendapatan()));
                }
                tampilkanAlert(Alert.AlertType.INFORMATION, "Export Berhasil", "Laporan tersimpan ke:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                tampilkanAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan file CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCetakLaporan() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(boxRekapitulasi.getScene().getWindow())) {
            boolean success = job.printPage(boxRekapitulasi);
            if (success) {
                job.endJob();
            }
        }
    }

    private void tampilkanAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}