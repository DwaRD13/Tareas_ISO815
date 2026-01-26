package com.unapec.nominagenerator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class EmpleadosController {

    @FXML
    private Button btnConsultar;

    @FXML
    private Button btnRegistrarCuenta;

    @FXML
    private Label lbNoDeCuenta;

    @FXML
    private Label lbNombreTab;

    @FXML
    private Label lbRCedula;

    @FXML
    private Label lbRNCconsultar;

    @FXML
    private Label lbSalario;

    @FXML
    private Label lbTabUsuarioDeseo;

    @FXML
    private Label lbTipoDeCuenta;

    @FXML
    private TextField txtCedulaEmpleado;

    @FXML
    private TextField txtConsultaRNC;

    @FXML
    private TextField txtNoCuenta;

    @FXML
    private TextField txtSalario;

    @FXML
    private TextField txtTipoCuenta;

    public void recibirDatosUsuario(String nombre) {
        lbNombreTab.setText("Sr(a). " + nombre);
        lbNombreTab.setVisible(true);
        lbTabUsuarioDeseo.setVisible(true);
    }

}
