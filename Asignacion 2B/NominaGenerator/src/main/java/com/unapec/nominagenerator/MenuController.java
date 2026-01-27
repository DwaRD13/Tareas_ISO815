package com.unapec.nominagenerator;

import com.unapec.entity.Empleado;
import com.unapec.entity.Empresa;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.util.List;

public class MenuController {

    @FXML private Button btnEnvioNombre;

    @FXML private Label lbInputName;

    @FXML private Label lbMensajeBienvenida;

    @FXML private Label lbMensajeDeNombre;

    @FXML private Label lbSaludos;

    @FXML private TextField textFieldNombre;

    @FXML private EmpresasController empresasViewController;

    @FXML private NominaController nominaViewController;

    @FXML private EmpleadosController empleadosViewController;

    @FXML
    public void initialize() {
        if (empresasViewController != null) {
            empresasViewController.setMainController(this);
        }
    }

    @FXML
    private void guardarNombre(ActionEvent event) {
        textFieldNombre.setVisible(false);
        btnEnvioNombre.setVisible(false);
        lbMensajeDeNombre.setVisible(true);
        lbInputName.setVisible(false);

        if(textFieldNombre.getText().equalsIgnoreCase("Aguilucho") || textFieldNombre.getText().equalsIgnoreCase("Aguilas") ){
            lbMensajeDeNombre.setText("¡No creo que tengas un nombre tan feo! \n ¡Prueba con Licey Campeon \uD83D\uDE0E!");
        }else{
            lbSaludos.setText("Saludos Sr(a). " + textFieldNombre.getText());
            String name = textFieldNombre.getText();
            lbMensajeBienvenida.setVisible(true);
            lbSaludos.setVisible(true);

            if (empresasViewController != null) {
                empresasViewController.recibirDatosUsuario(name);
            }

            if (empleadosViewController != null) {
                empleadosViewController.recibirDatosUsuario(name);
            }

            if (nominaViewController != null) {
                nominaViewController.recibirDatosUsuario(name);
            }
        }
    }

    public void compartirIdEmpresa(Long idEmpresa) {
        System.out.println("MenuController recibió el ID: " + idEmpresa); // Debug
        if (empleadosViewController != null) {
            empleadosViewController.recibirDatosEmpresa(idEmpresa);
        } else {
            System.out.println("Error: No puedo pasar el ID a empleados porque el controlador es null");
        }
    }

    public void enviarDatosANomina(Empresa empresa, List<Empleado> listaEmpleados) {
        if (nominaViewController != null) {
            nominaViewController.cargarDatosNomina(empresa, listaEmpleados);
        } else {
            System.out.println("Error: Controlador de Nómina no iniciado.");
        }
    }

}