package com.unapec.nominagenerator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class NominaController {

    @FXML
    private Button btnExportarNomina;

    @FXML
    private Button btnSeleccionaRuta;

    @FXML
    private Label lbNombreTab;

    @FXML
    private Label lbTabUsuarioDeseo;

    @FXML
    private TableView<?> tbNomina;

    public void recibirDatosUsuario(String nombre) {
        lbNombreTab.setText("Sr(a). " + nombre);
        lbNombreTab.setVisible(true);
        lbTabUsuarioDeseo.setVisible(true);
    }
}
