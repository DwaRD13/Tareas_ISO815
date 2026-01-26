package com.unapec.nominagenerator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class MenuController {

    @FXML
    private Button btnEnvioNombre;

    @FXML
    private Label lbInputName;

    @FXML
    private Label lbMensajeBienvenida;

    @FXML
    private Label lbMensajeDeNombre;

    @FXML
    private Label lbSaludos;

    @FXML
    private TextField textFieldNombre;

    @FXML
    private EmpresasController empresasViewController;

    @FXML
    private NominaController nominaViewController;

    @FXML
    private EmpleadosController empleadosViewController;

    @FXML
    private void guardarNombre(ActionEvent event) {
        textFieldNombre.setVisible(false);
        btnEnvioNombre.setVisible(false);
        lbMensajeDeNombre.setVisible(true);
        lbInputName.setVisible(false);

        if(textFieldNombre.getText().equals("Aguilucho") || textFieldNombre.getText().equals("Aguilas") ){
            lbMensajeDeNombre.setText("¡No creo que tengas un nombre tan feo! \n ¡Prueba con Licey Campeon \uD83D\uDE0E!");
        }else{
            lbSaludos.setText("Saludos Sr(a). " + textFieldNombre.getText());
            String name = textFieldNombre.getText();
            lbMensajeBienvenida.setVisible(true);
            lbSaludos.setVisible(true);

            if (empresasViewController != null) {
                empresasViewController.recibirDatosUsuario(name);
            } else {
                System.out.println("Error: El controlador de empresas no se ha cargado.");
            }

            if (empleadosViewController != null) {
                empleadosViewController.recibirDatosUsuario(name);
            } else {
                System.out.println("Aviso: El controlador de empleados no se ha cargado.");
            }

            if (nominaViewController != null) {
                nominaViewController.recibirDatosUsuario(name);
            } else {
                System.out.println("Error: El controlador de Nomina no se ha cargado.");
            }
        }
    }

}
