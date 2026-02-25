package com.unapec.nominagenerator;

import com.unapec.db.EmpleadoRepository;
import com.unapec.db.EmpresaRepository;
import com.unapec.entity.Empleado;
import com.unapec.entity.Empresa;
import com.unapec.util.Validaciones;
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
    @FXML private Label lbErrorCedula;
    @FXML private Label lbErrorSalario;
    @FXML private Label lbErrorCuenta;

    private Long empresaConsultadaId;
    private MenuController mainController;

    @FXML
    public void initialize() {
        lbErrorCedula.setVisible(false);
        lbErrorSalario.setVisible(false);
        lbErrorCuenta.setVisible(false);

        setupValidators();
    }

    private void setupValidators() {
        txtCedulaEmpleado.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtCedulaEmpleado.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 11) {
                txtCedulaEmpleado.setText(oldValue);
            }
        });

        txtConsultaCedula.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtConsultaCedula.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 11) {
                txtConsultaCedula.setText(oldValue);
            }
        });

        txtNoCuenta.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtNoCuenta.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 20) {
                txtNoCuenta.setText(oldValue);
            }
        });

        txtSalario.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 10) {
                txtSalario.setText(oldValue);
            }
        });
    }

    @FXML
    void registrarEmpleado(ActionEvent event) {
        if (empresaConsultadaId == null) {
            mostrarAlerta(AlertType.WARNING, "Empresa no seleccionada",
                    "Por favor, consulte y seleccione una empresa antes de registrar un empleado.");
            return;
        }

        String cedula = txtCedulaEmpleado.getText().trim();
        if (cedula.isEmpty()) {
            lbErrorCedula.setText("• La cédula es obligatoria.");
            lbErrorCedula.setVisible(true);
            txtCedulaEmpleado.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return;
        } else if (cedula.length() != 11) {
            lbErrorCedula.setText("• La cédula debe tener 11 dígitos.");
            lbErrorCedula.setVisible(true);
            txtCedulaEmpleado.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return;
        } else if (!Validaciones.esCedulaValida(cedula)) { // 🔹 NUEVO: algoritmo oficial
            lbErrorCedula.setText("• La cédula ingresada no es válida.");
            lbErrorCedula.setVisible(true);
            txtCedulaEmpleado.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return;
        } else {
            lbErrorCedula.setVisible(false);
            txtCedulaEmpleado.setStyle("");
        }

        double salario;
        try {
            salario = Double.parseDouble(txtSalario.getText().trim());
            if (salario <= 0) {
                lbErrorSalario.setText("• El salario debe ser mayor a 0.");
                lbErrorSalario.setVisible(true);
                txtSalario.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                return;
            } else {
                lbErrorSalario.setVisible(false);
                txtSalario.setStyle("");
            }
        } catch (NumberFormatException e) {
            lbErrorSalario.setText("• El salario debe ser numérico (use punto para decimales).");
            lbErrorSalario.setVisible(true);
            txtSalario.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return;
        }

        String cuenta = txtNoCuenta.getText().trim();
        if (cuenta.isEmpty()) {
            lbErrorCuenta.setText("• La cuenta bancaria es obligatoria.");
            lbErrorCuenta.setVisible(true);
            txtNoCuenta.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return;
        } else if (cuenta.length() > 20) {
            lbErrorCuenta.setText("• La cuenta bancaria no puede exceder 20 dígitos.");
            lbErrorCuenta.setVisible(true);
            txtNoCuenta.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return;
        } else {
            lbErrorCuenta.setVisible(false);
            txtNoCuenta.setStyle("");
        }

        if (cbTipoDeCuenta.getValue() == null) {
            mostrarAlerta(AlertType.ERROR, "Tipo de cuenta faltante",
                    "Debe seleccionar un tipo de cuenta.");
            return;
        }

        try {
            Empleado empleado = new Empleado();
            empleado.setCedula(cedula);
            empleado.setSalario(salario);

            String tipoCuenta = cbTipoDeCuenta.getValue().toString();
            empleado.setTipoDeCuenta(tipoCuenta.length() > 0 ? tipoCuenta.substring(0, 1) : "N");
            empleado.setCuentaBancaria(cuenta);

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