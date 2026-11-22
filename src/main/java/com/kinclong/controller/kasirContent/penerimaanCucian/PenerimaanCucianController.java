package com.kinclong.controller.kasirContent.penerimaanCucian;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.kinclong.controller.MainLayoutController;
import com.kinclong.controller.SidebarSwitching;
import com.kinclong.dao.PelangganDAO;
import com.kinclong.model.Kasir;
import com.kinclong.model.Pelanggan;
import com.kinclong.model.Transaksi;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PenerimaanCucianController implements Initializable, SidebarSwitching {

    private MainLayoutController mainLayoutController;

    @FXML
    private HBox boxTambahPelanggan;

    @FXML
    private Button btnBatalkan;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnLanjut;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnTambahPelanggan;

    @FXML
    private TableView<Pelanggan> tablePelanggan;

    @FXML
    private TableColumn<Pelanggan, Integer> coloumID;

    @FXML
    private TableColumn<Pelanggan, String> coloumNamaPelanggan;

    @FXML
    private TableColumn<Pelanggan, String> coloumTelepon;

    @FXML
    private TableColumn<Pelanggan, String> coloumAlamat;

    @FXML
    private TextField textSearchPelanggan;

    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final ObservableList<Pelanggan> masterPelanggan = FXCollections.observableArrayList();
    private FilteredList<Pelanggan> filteredPelanggan;

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;

        if (mainLayoutController.getTransaksiAktif() != null
                && mainLayoutController.getTransaksiAktif().getPelanggan() != null) {
            int idTerpilih = mainLayoutController.getTransaksiAktif().getPelanggan().getIdPelanggan();
            masterPelanggan.stream()
                    .filter(p -> p.getIdPelanggan() == idTerpilih)
                    .findFirst()
                    .ifPresent(p -> tablePelanggan.getSelectionModel().select(p));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coloumID.setCellValueFactory(new PropertyValueFactory<>("idPelanggan"));
        coloumNamaPelanggan.setCellValueFactory(new PropertyValueFactory<>("nama"));
        coloumTelepon.setCellValueFactory(new PropertyValueFactory<>("nomorTelepon"));
        coloumAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));

        muatDataPelanggan();
    }

    private void muatDataPelanggan() {
        masterPelanggan.setAll(pelangganDAO.getAll());
        filteredPelanggan = new FilteredList<>(masterPelanggan, p -> true);
        tablePelanggan.setItems(filteredPelanggan);
    }

    @FXML
    void handleSearch(ActionEvent event) {
        if (filteredPelanggan == null) return;
        String keyword = textSearchPelanggan.getText() == null ? "" : textSearchPelanggan.getText().toLowerCase().trim();

        filteredPelanggan.setPredicate(p -> {
            if (keyword.isEmpty()) return true;
            return String.valueOf(p.getIdPelanggan()).contains(keyword)
                    || p.getNama().toLowerCase().contains(keyword)
                    || p.getNomorTelepon().toLowerCase().contains(keyword);
        });
    }

    @FXML
    void klikBatalkan(ActionEvent event) {
        tablePelanggan.getSelectionModel().clearSelection();
        textSearchPelanggan.clear();
        if (mainLayoutController != null) {
            mainLayoutController.setTransaksiAktif(null);
        }
    }

    @FXML
    void klikLanjut(ActionEvent event) {
        Pelanggan terpilih = tablePelanggan.getSelectionModel().getSelectedItem();
        if (terpilih == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Pilih Pelanggan");
            alert.setHeaderText(null);
            alert.setContentText("Silakan pilih pelanggan dari tabel terlebih dahulu.");
            alert.showAndWait();
            return;
        }

        if (mainLayoutController != null) {
            Transaksi transaksi = mainLayoutController.getTransaksiAktif();
            if (transaksi == null) {
                transaksi = new Transaksi();
            }
            transaksi.setPelanggan(terpilih);
            if (mainLayoutController.getCurrentUser() instanceof Kasir kasir) {
                transaksi.setKasir(kasir);
            }
            mainLayoutController.setTransaksiAktif(transaksi);
            mainLayoutController.loadPage("/com/kinclong/kasirContent/penerimaanCucian/PenerimaanCucianLayanan.fxml");
        }
    }

    @FXML
    void openEditPelanggan(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/kasirContent/penerimaanCucian/EditPelanggan.fxml");
        }
    }

    @FXML
    void openPopupTambah(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kinclong/kasirContent/penerimaanCucian/TambahPelanggan.fxml"));
            Parent popupRoot = loader.load();

            TambahPelangganController popupController = loader.getController();

            Stage popupStage = new Stage();
            popupStage.setTitle("Tambah Pelanggan");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(tablePelanggan.getScene().getWindow());
            popupStage.setScene(new Scene(popupRoot));
            popupStage.setResizable(false);
            popupStage.showAndWait();

            Pelanggan baru = popupController.getPelangganBaru();
            if (baru != null) {
                muatDataPelanggan();
                tablePelanggan.getSelectionModel().select(baru);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
