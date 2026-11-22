package com.kinclong.controller.kasirContent;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.kinclong.controller.MainLayoutController;
import com.kinclong.controller.SidebarSwitching;
import com.kinclong.dao.TransaksiDAO;
import com.kinclong.model.DetailTransaksi;
import com.kinclong.model.Transaksi;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class UpdateStatusControlller implements Initializable, SidebarSwitching {

    private MainLayoutController mainLayoutController;

    @FXML private Label labelCountBelum;

    @FXML private Label labelCountProses;

    @FXML private Label labelCountSudah;

    @FXML private Button btnSearch;

    @FXML private MenuButton btnFilterStatusCucian;

    @FXML private TextField searchBarUpdate;

    @FXML private TableView<Transaksi> tableUpdateStatus;

    @FXML private TableColumn<Transaksi, Transaksi> coloumNota;

    @FXML private TableColumn<Transaksi, Transaksi> coloumPelanggan;

    @FXML private TableColumn<Transaksi, String> coloumLayanan;

    @FXML private TableColumn<Transaksi, String> coloumKeterangan;

    @FXML private TableColumn<Transaksi, Transaksi> statusSaatIni;

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final ObservableList<Transaksi> masterData = FXCollections.observableArrayList();
    private FilteredList<Transaksi> daftarTransaksiFilter;

    private String selectedFilterStatus = "Semua Status Cucian";

    private final DateTimeFormatter formatTanggal =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupKolomTabel();
        muatDataTransaksi();
    }

    private void setupKolomTabel() {
        coloumNota.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue()));
        coloumNota.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Transaksi t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setGraphic(null);
                } else {
                    Label lblNota = new Label("#" + t.getIdTransaksi());
                    lblNota.setStyle("-fx-text-fill: #1E3A5F; -fx-font-weight: bold; -fx-font-size: 13px;");

                    String tgl = t.getTanggalMasuk() != null ? t.getTanggalMasuk().format(formatTanggal) : "-";
                    Label lblTanggal = new Label(tgl);
                    lblTanggal.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

                    VBox box = new VBox(2, lblNota, lblTanggal);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        coloumPelanggan.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue()));
        coloumPelanggan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Transaksi t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setGraphic(null);
                } else {
                    Label lblNama = new Label(t.getNamaPelanggan());
                    lblNama.setStyle("-fx-text-fill: #1E3A5F; -fx-font-weight: bold; -fx-font-size: 13px;");

                    Label lblTelp = new Label(t.getNomorTeleponPelanggan());
                    lblTelp.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

                    VBox box = new VBox(2, lblNama, lblTelp);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        coloumLayanan.setCellValueFactory(cell -> {
            String ringkasanLayanan = cell.getValue().getListDetail().stream()
                    .map(this::formatItemLayanan)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(ringkasanLayanan.isEmpty() ? "-" : ringkasanLayanan);
        });
        coloumLayanan.setCellFactory(col -> createWrappingTextCell());

        coloumKeterangan.setCellValueFactory(cell -> {
            String ket = cell.getValue().getListDetail().stream()
                    .map(DetailTransaksi::getKeterangan)
                    .filter(k -> k != null && !k.isBlank())
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(ket.isEmpty() ? "-" : ket);
        });
        coloumKeterangan.setCellFactory(col -> createWrappingTextCell());

        statusSaatIni.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue()));
        statusSaatIni.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cmbStatus = new ComboBox<>(
                    FXCollections.observableArrayList("Sudah", "Dalam_Proses", "Belum")
            );
            private boolean updatingUI = false;

            {
                cmbStatus.setPrefWidth(135);
                cmbStatus.setOnAction(event -> {
                    if (updatingUI) return;
                    Transaksi t = getTableView().getItems().get(getIndex());
                    String pilihanUI = cmbStatus.getValue();
                    if (t != null && pilihanUI != null) {
                        String statusDb = pilihanUI.toUpperCase();
                        if (transaksiDAO.updateStatusCucian(t.getIdTransaksi(), statusDb)) {
                            t.setStatusCucian(statusDb);
                            terapkanStyleStatus(cmbStatus, statusDb);
                            updateKartuRingkasan();
                            terapkanFilter();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Transaksi t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setGraphic(null);
                } else {
                    updatingUI = true;
                    String statusDb = t.getStatusCucian() != null ? t.getStatusCucian() : "BELUM";
                    cmbStatus.setValue(formatStatusKeUI(statusDb));
                    terapkanStyleStatus(cmbStatus, statusDb);
                    updatingUI = false;

                    setAlignment(Pos.CENTER);
                    setGraphic(cmbStatus);
                }
            }
        });
    }

    private TableCell<Transaksi, String> createWrappingTextCell() {
        return new TableCell<>() {
            private final Label lbl = new Label();
            {
                lbl.setWrapText(true);
                lbl.setStyle("-fx-text-fill: #1E3A5F; -fx-font-size: 13px;");
            }

            @Override
            protected void updateItem(String text, boolean empty) {
                super.updateItem(text, empty);
                if (empty || text == null) {
                    setGraphic(null);
                } else {
                    lbl.setText(text);
                    lbl.setMaxWidth(getTableColumn().getWidth() - 24);
                    setGraphic(lbl);
                }
            }
        };
    }

    private String formatItemLayanan(DetailTransaksi d) {
        double qty = d.getJumlah();
        String qtyStr = (qty == (long) qty) ? String.format("%d", (long) qty) : String.valueOf(qty);
        return d.getNamaLayanan() + " " + qtyStr + " " + d.getJenisSatuan().toLowerCase();
    }

    private String formatStatusKeUI(String statusDb) {
        return switch (statusDb.toUpperCase()) {
            case "SUDAH" -> "Sudah";
            case "DALAM_PROSES" -> "Dalam_Proses";
            default -> "Belum";
        };
    }

    private void terapkanStyleStatus(ComboBox<String> cmb, String statusDb) {
        String warna = switch (statusDb.toUpperCase()) {
            case "SUDAH" -> "-fx-background-color: #DCFCE7; -fx-border-color: #22C55E;";
            case "DALAM_PROSES" -> "-fx-background-color: #FEF3C7; -fx-border-color: #F59E0B;";
            default -> "-fx-background-color: #FEE2E2; -fx-border-color: #EF4444;";
        };
        cmb.setStyle(warna + " -fx-background-radius: 15; -fx-border-radius: 15; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
    }

    private void muatDataTransaksi() {
        masterData.setAll(transaksiDAO.getAllUntukUpdateStatus());
        daftarTransaksiFilter = new FilteredList<>(masterData, p -> true);
        tableUpdateStatus.setItems(daftarTransaksiFilter);
        updateKartuRingkasan();
    }

    private void updateKartuRingkasan() {
        long sudah = masterData.stream().filter(t -> "SUDAH".equalsIgnoreCase(t.getStatusCucian())).count();
        long proses = masterData.stream().filter(t -> "DALAM_PROSES".equalsIgnoreCase(t.getStatusCucian())).count();
        long belum = masterData.stream().filter(t -> "BELUM".equalsIgnoreCase(t.getStatusCucian())).count();

        labelCountSudah.setText(String.valueOf(sudah));
        labelCountProses.setText(String.valueOf(proses));
        labelCountBelum.setText(String.valueOf(belum));
    }

    private void terapkanFilter() {
        if (daftarTransaksiFilter == null) return;

        String keyword = searchBarUpdate.getText() == null
                ? ""
                : searchBarUpdate.getText().toLowerCase().trim().replace("#", "");

        daftarTransaksiFilter.setPredicate(t -> {
            boolean cocokStatus = true;
            if (selectedFilterStatus != null && !"Semua Status Cucian".equalsIgnoreCase(selectedFilterStatus)) {
                cocokStatus = selectedFilterStatus.equalsIgnoreCase(t.getStatusCucian());
            }

            boolean cocokCari = true;
            if (!keyword.isEmpty()) {
                String idStr = String.valueOf(t.getIdTransaksi());
                String nama = t.getNamaPelanggan().toLowerCase();
                cocokCari = idStr.contains(keyword) || nama.contains(keyword);
            }

            return cocokStatus && cocokCari;
        });
    }

    @FXML
    void filteredData(KeyEvent event) {
        terapkanFilter();
    }

    @FXML
    void handleFilterStatus(ActionEvent event) {
        if (event.getSource() instanceof MenuItem item) {
            selectedFilterStatus = item.getText();
            btnFilterStatusCucian.setText(selectedFilterStatus);
            terapkanFilter();
        }
    }

    @FXML
    void handleSearch(ActionEvent event) {
        terapkanFilter();
    }
}