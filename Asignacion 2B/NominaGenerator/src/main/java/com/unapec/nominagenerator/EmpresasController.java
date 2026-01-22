package com.unapec.nominagenerator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class EmpresasController {

    @FXML
    private Button btnConsultar;

    @FXML
    private Button btnRegistrarCuenta;

    @FXML
    private Label lbNoDeCuenta;

    @FXML
    private Label lbNombreTab;

    @FXML
    private Label lbRNCEmpresa;

    @FXML
    private Label lbRNCconsultar;

    @FXML
    private Label lbRNombreEmpresa;

    @FXML
    private Label lbTabUsuarioDeseo;

    @FXML
    private TextField txtConsultaRNC;

    @FXML
    private TextField txtNoCuenta;

    @FXML
    private TextField txtNombreEmpresa;

    @FXML
    private TextField txtRNC;

    public void recibirDatosUsuario(String nombre) {
        lbNombreTab.setText("Sr(a). " + nombre);
        lbNombreTab.setVisible(true);
        lbTabUsuarioDeseo.setVisible(true);
    }
}
