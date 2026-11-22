package com.kinclong.controller.kasirContent.penerimaanCucian;

import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import com.kinclong.controller.MainLayoutController;
import com.kinclong.controller.SidebarSwitching;
import com.kinclong.dao.PelangganDAO;
import com.kinclong.model.Pelanggan;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class EditPelangganController implements Initializable, SidebarSwitching {

    private MainLayoutController mainLayoutController;

    @FXML private Button btnBatalkan;
    @FXML private Button btnSearch;
    @FXML private Button btnSimpan;

    @FXML private TableView<Pelanggan> tableEditPelanggan;
    @FXML private TableColumn<Pelanggan, Integer> coloumID;
    @FXML private TableColumn<Pelanggan, String> coloumNamaPelanggan;
    @FXML private TableColumn<Pelanggan, String> coloumTelepon;
    @FXML private TableColumn<Pelanggan, String> coloumAlamat;
    @FXML private TableColumn<Pelanggan, Pelanggan> coloumHapus;

    @FXML private TextField textSearchPelanggan;

    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final ObservableList<Pelanggan> masterPelanggan = FXCollections.observableArrayList();
    private FilteredList<Pelanggan> filteredPelanggan;
    private final Set<Pelanggan> pelangganDiubah = new HashSet<>();

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tableEditPelanggan.setEditable(true);

        coloumID.setCellValueFactory(new PropertyValueFactory<>("idPelanggan"));

        // Kolom Nama Editable (klik 2x pada sel untuk mengedit)
        coloumNamaPelanggan.setCellValueFactory(new PropertyValueFactory<>("nama"));
        coloumNamaPelanggan.setCellFactory(TextFieldTableCell.forTableColumn());
        coloumNamaPelanggan.setOnEditCommit(event -> {
            Pelanggan p = event.getRowValue();
            p.setNama(event.getNewValue().trim());
            pelangganDiubah.add(p);
        });

        // Kolom No Telepon Editable
        coloumTelepon.setCellValueFactory(new PropertyValueFactory<>("nomorTelepon"));
        coloumTelepon.setCellFactory(TextFieldTableCell.forTableColumn());
        coloumTelepon.setOnEditCommit(event -> {
            Pelanggan p = event.getRowValue();
            p.setNomorTelepon(event.getNewValue().trim());
            pelangganDiubah.add(p);
        });

        // Kolom Alamat Editable
        coloumAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        coloumAlamat.setCellFactory(TextFieldTableCell.forTableColumn());
        coloumAlamat.setOnEditCommit(event -> {
            Pelanggan p = event.getRowValue();
            p.setAlamat(event.getNewValue().trim());
            pelangganDiubah.add(p);
        });

        // Kolom Kosong Kanan: Ikon Tong Sampah (Hapus)
        coloumHapus.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue()));
        coloumHapus.setCellFactory(col -> new TableCell<>() {
            private final Button btnHapus = buatTombolHapus();

            {
                btnHapus.setOnAction(e -> {
                    Pelanggan target = getTableView().getItems().get(getIndex());
                    if (target != null) {
                        konfirmasiHapus(target);
                    }
                });
            }

            @Override
            protected void updateItem(Pelanggan item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setAlignment(Pos.CENTER);
                    setGraphic(btnHapus);
                }
            }
        });

        muatData();
    }

    private Button buatTombolHapus() {
        SVGPath ikonSampah = new SVGPath();
        ikonSampah.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        ikonSampah.setFill(Color.web("#EF4444"));
        ikonSampah.setScaleX(0.85);
        ikonSampah.setScaleY(0.85);

        Button btn = new Button();
        btn.setGraphic(ikonSampah);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        return btn;
    }

    private void muatData() {
        masterPelanggan.setAll(pelangganDAO.getAll());
        filteredPelanggan = new FilteredList<>(masterPelanggan, p -> true);
        tableEditPelanggan.setItems(filteredPelanggan);
    }

    private void konfirmasiHapus(Pelanggan target) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Pelanggan");
        confirm.setHeaderText(null);
        confirm.setContentText("Yakin ingin menghapus pelanggan \"" + target.getNama() + "\"?");

        Optional<ButtonType> hasil = confirm.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            if (pelangganDAO.delete(target.getIdPelanggan())) {
                masterPelanggan.remove(target);
                pelangganDiubah.remove(target);
            } else {
                Alert gagal = new Alert(Alert.AlertType.WARNING);
                gagal.setTitle("Tidak Dapat Menghapus");
                gagal.setHeaderText(null);
                gagal.setContentText("Pelanggan \"" + target.getNama() + "\" tidak dapat dihapus karena sudah memiliki riwayat transaksi.");
                gagal.showAndWait();
            }
        }
    }

    // Tanpa parameter agar kompatibel dengan On Key Released (TextField) dan On Action (Button)
    @FXML
    void handleSearch() {
        if (filteredPelanggan == null) return;
        String keyword = textSearchPelanggan.getText() == null ? "" : textSearchPelanggan.getText().toLowerCase().trim();
        filteredPelanggan.setPredicate(p -> keyword.isEmpty()
                || String.valueOf(p.getIdPelanggan()).contains(keyword)
                || p.getNama().toLowerCase().contains(keyword)
                || p.getNomorTelepon().toLowerCase().contains(keyword));
    }

    @FXML
    void klikBatalkan(ActionEvent event) {
        kembaliKePilihPelanggan();
    }

    @FXML
    void klikSimpan(ActionEvent event) {
        for (Pelanggan p : pelangganDiubah) {
            pelangganDAO.update(p);
        }
        pelangganDiubah.clear();

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Berhasil");
        info.setHeaderText(null);
        info.setContentText("Perubahan data pelanggan berhasil disimpan.");
        info.showAndWait();

        kembaliKePilihPelanggan();
    }

    private void kembaliKePilihPelanggan() {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/PenerimaanCucian.fxml");
        }
    }
}