package com.unapec.nominagenerator;

import com.unapec.db.EmpleadoRepository;
import com.unapec.db.EmpresaRepository;
import com.unapec.entity.Empleado;
import com.unapec.entity.Empresa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class EmpleadosController {

    @FXML private Button btnConsultar;
    @FXML private Button btnRegistrarCuenta;
    @FXML private ComboBox<String> cbTipoDeCuenta;
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
    private MenuController mainController;

    @FXML
    void registrarEmpleado(ActionEvent event) {
        if (empresaConsultadaId == null) {
            mostrarAlerta(AlertType.WARNING, "Empresa no seleccionada",
                    "Por favor, consulte y seleccione una empresa antes de registrar un empleado.");
            return;
        }

        if (txtCedulaEmpleado.getText().isEmpty() ||
                txtSalario.getText().isEmpty() ||
                txtNoCuenta.getText().isEmpty() ||
                cbTipoDeCuenta.getValue() == null) {

            mostrarAlerta(AlertType.ERROR, "Campos vacíos",
                    "Todos los campos son obligatorios. Por favor complete la información.");
            return;
        }

        double salario;
        try {
            salario = Double.parseDouble(txtSalario.getText());
            if (salario <= 0) {
                mostrarAlerta(AlertType.WARNING, "Salario inválido", "El salario debe ser mayor a 0.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(AlertType.ERROR, "Formato incorrecto",
                    "El campo Salario solo debe contener números (use punto para decimales).");
            return;
        }

        String cedula = txtCedulaEmpleado.getText().trim();
        if (cedula.length() < 11) {
            mostrarAlerta(AlertType.WARNING, "Cédula inválida",
                    "La cédula parece ser muy corta. Verifique el dato.");
            return;
        }

        try {
            Empleado empleado = new Empleado();
            empleado.setCedula(cedula);
            empleado.setSalario(salario);

            String tipoCuenta = cbTipoDeCuenta.getValue().toString();
            empleado.setTipoDeCuenta(tipoCuenta.length() > 0 ? tipoCuenta.substring(0, 1) : "N");
            empleado.setCuentaBancaria(txtNoCuenta.getText());

            EmpleadoRepository repo = new EmpleadoRepository();
            repo.insertEmpleado(empleado, empresaConsultadaId);

            mostrarAlerta(AlertType.INFORMATION, "Éxito", "Empleado registrado correctamente.");
            limpiarCamposRegistro();

            if (mainController != null) {
                mainController.notificarCambioEnEmpleados(empresaConsultadaId);
            } else {
                System.out.println("Error: mainController es null en EmpleadosController");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(AlertType.ERROR, "Error de Base de Datos", "No se pudo guardar el empleado: " + e.getMessage());
        }
    }

    @FXML
    void consultarEmpleado(ActionEvent event) {
        if (txtConsultaCedula.getText().isEmpty()) {
            mostrarAlerta(AlertType.WARNING, "Campo vacío", "Debe ingresar una cédula para consultar.");
            return;
        }

        EmpleadoRepository repo = new EmpleadoRepository();
        Empleado empleado = repo.consultarEmpleadoPorCedula(txtConsultaCedula.getText());

        if (empleado == null) {
            lsVwDatosEmpleadoConsultado.getItems().setAll("Empleado no encontrado");
            mostrarAlerta(AlertType.INFORMATION, "Sin resultados", "No se encontró ningún empleado con esa cédula.");
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
            items.add("Empresa: no encontrada (ID: " + empleado.getEmpresaId() + ")");
        }

        lsVwDatosEmpleadoConsultado.setItems(items);
    }

    private String safe(String s) {
        return s == null ? "N/A" : s;
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarCamposRegistro() {
        txtCedulaEmpleado.clear();
        txtSalario.clear();
        txtNoCuenta.clear();
        cbTipoDeCuenta.getSelectionModel().clearSelection();
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

    public void setMainController(MenuController mainController) {
        this.mainController = mainController;
    }
}