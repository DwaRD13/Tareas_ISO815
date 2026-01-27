package com.unapec.nominagenerator;

import com.unapec.db.EmpleadoRepository;
import com.unapec.db.EmpresaRepository;
import com.unapec.entity.Empleado;
import com.unapec.entity.Empresa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.ComboBox;

public class EmpleadosController {

    @FXML private Button btnConsultar;

    @FXML private Button btnRegistrarCuenta;

    @FXML private ComboBox<?> cbTipoDeCuenta;

    @FXML private Label lbNoDeCuenta;

    @FXML private Label lbNombreTab;

    @FXML private Label lbRCedula;

    @FXML private Label lbRNCconsultar;

    @FXML private Label lbSalario;

    @FXML private Label lbTabUsuarioDeseo;

    @FXML private Label lbTipoDeCuenta;

    @FXML private ListView<String> lsVwDatosEmpleadoConsultado;

    @FXML private TextField txtCedulaEmpleado;

    @FXML private TextField txtConsultaCedula;

    @FXML private TextField txtNoCuenta;

    @FXML private TextField txtSalario;

    private Long empresaConsultadaId;

    @FXML
    void registrarEmpleado(ActionEvent event) {

        if (empresaConsultadaId == null) {
            System.out.println("Error: Debes consultar una empresa primero");
            return;
        }

        Empleado empleado = new Empleado();
        empleado.setCedula(txtCedulaEmpleado.getText());
        empleado.setSalario(Double.parseDouble(txtSalario.getText()));
        empleado.setTipoDeCuenta(cbTipoDeCuenta.getValue().toString().substring(0,1));
        empleado.setCuentaBancaria(txtNoCuenta.getText());

        EmpleadoRepository repo = new EmpleadoRepository();

        repo.insertEmpleado(empleado, empresaConsultadaId);
        System.out.println("Empleado guardado para la empresa ID: " + empresaConsultadaId);
    }

    @FXML
    void consultarEmpleado(ActionEvent event) {
        EmpleadoRepository repo = new EmpleadoRepository();
        Empleado empleado = repo.consultarEmpleadoPorCedula(txtConsultaCedula.getText());

        if (empleado == null) {
            lsVwDatosEmpleadoConsultado.getItems().setAll("Empleado no encontrado");
            return;
        }

        EmpresaRepository empresaRepository = new EmpresaRepository();
        Empresa empresa = empresaRepository.buscarEmpresaPorID(empleado.getEmpresaId());

        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("Cédula: " + safe(empleado.getCedula()));
        items.add("Cuenta bancaria: " + safe(empleado.getCuentaBancaria()));
        items.add("Tipo de cuenta: " + safe(empleado.getTipoDeCuenta()));
        items.add("Salario: " + String.format("%,.2f", empleado.getSalario()));
        if (empresa != null) {
            items.add("Empresa: " + safe(empresa.getNombre()));
            items.add("RNC: " + safe(String.valueOf(empresa.getRNC())));
            items.add("Forma de pago: " + safe(empresa.getFormaPago()));
        } else {
            items.add("Empresa: no encontrada");
        }

        lsVwDatosEmpleadoConsultado.setItems(items);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }


    public void recibirDatosUsuario(String nombre) {
        lbNombreTab.setText("Sr(a). " + nombre);
        lbNombreTab.setVisible(true);
        lbTabUsuarioDeseo.setVisible(true);
    }

    public void recibirDatosEmpresa(Long empresaId) {
        this.empresaConsultadaId = empresaId;
        System.out.println("ID recibido en Empleados: " + empresaId);
    }

}
