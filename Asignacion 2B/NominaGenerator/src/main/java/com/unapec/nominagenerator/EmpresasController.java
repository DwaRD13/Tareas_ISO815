package com.unapec.nominagenerator;

import com.unapec.db.EmpleadoRepository;
import com.unapec.db.EmpresaRepository;
import com.unapec.entity.Empleado;
import com.unapec.entity.Empresa;
import com.unapec.util.Validaciones;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class EmpresasController {

    @FXML private Button btnConsultar;

    @FXML private Button btnRegistrarCuenta;

    @FXML private ComboBox<String> cbFormaDePago;

    @FXML private Label lbCantidadEmpleados;

    @FXML private Label lbCuentaBancaria;

    @FXML private Label lbFormaDePago;

    @FXML private Label lbFormaPago;

    @FXML private Label lbNoDeCuenta;

    @FXML private Label lbNombreEmpresa;

    @FXML private Label lbNombreTab;

    @FXML private Label lbRNCEmpresa;

    @FXML private Label lbRNCconsultar;

    @FXML private Label lbRNombreEmpresa;

    @FXML private Label lbTabUsuarioDeseo;

    @FXML private TextField txtConsultaRNC;

    @FXML private TextField txtNoCuenta;

    @FXML private TextField txtNombreEmpresa;

    @FXML private TextField txtRNC;

    @FXML private TextArea txtAreaDetalles;

    @FXML private Label lbErrorRegistro;

    @FXML private Label lbErrorConsulta;

    @FXML private MenuController mainController;

    private static final Pattern RNC_PATTERN = Pattern.compile("^\\d{9}$");
    private static final Pattern CUENTA_PATTERN = Pattern.compile("^\\d{10,20}$");
    private static final Pattern NOMBRE_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{3,100}$");

    public void setMainController(MenuController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        cbFormaDePago.setItems(FXCollections.observableArrayList(
                "Mensual", "Quincenal", "Semanal"
        ));

        lbErrorRegistro.setTextFill(Color.RED);
        lbErrorRegistro.setVisible(false);
        lbErrorConsulta.setTextFill(Color.RED);
        lbErrorConsulta.setVisible(false);

        setupValidators();
    }

    private void setupValidators() {
        txtRNC.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtRNC.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 9) {
                txtRNC.setText(oldValue);
            }
        });

        txtConsultaRNC.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtConsultaRNC.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 9) {
                txtConsultaRNC.setText(oldValue);
            }
        });

        // Validador para cuenta bancaria (solo números)
        txtNoCuenta.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtNoCuenta.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 20) {
                txtNoCuenta.setText(oldValue);
            }
        });
    }

    private boolean validarCamposRegistro() {
        StringBuilder errores = new StringBuilder();

        // Validar nombre
        if (txtNombreEmpresa.getText().trim().isEmpty()) {
            errores.append("• El nombre de la empresa es obligatorio.\n");
            txtNombreEmpresa.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        } else if (!NOMBRE_PATTERN.matcher(txtNombreEmpresa.getText().trim()).matches()) {
            errores.append("• El nombre debe tener entre 3 y 100 caracteres y solo letras.\n");
            txtNombreEmpresa.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        } else {
            txtNombreEmpresa.setStyle("");
        }

        // Validar RNC
        if (txtRNC.getText().trim().isEmpty()) {
            errores.append("• El RNC es obligatorio.\n");
            txtRNC.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        } else if (!RNC_PATTERN.matcher(txtRNC.getText().trim()).matches()) {
            errores.append("• El RNC debe tener exactamente 9 dígitos.\n");
            txtRNC.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        } else if (!Validaciones.esRNCValido(txtRNC.getText().trim())) {
            errores.append("• El RNC ingresado no es válido.\n");
            txtRNC.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        } else {
            txtRNC.setStyle("");
        }

        // Validar cuenta bancaria
        if (txtNoCuenta.getText().trim().isEmpty()) {
            errores.append("• La cuenta bancaria es obligatoria.\n");
            txtNoCuenta.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        } else if (!CUENTA_PATTERN.matcher(txtNoCuenta.getText().trim()).matches()) {
            errores.append("• La cuenta bancaria debe tener entre 10 y 20 dígitos.\n");
            txtNoCuenta.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        } else {
            txtNoCuenta.setStyle("");
        }

        if (cbFormaDePago.getValue() == null || cbFormaDePago.getValue().isEmpty()) {
            errores.append("• Debe seleccionar una forma de pago.\n");
            cbFormaDePago.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        } else {
            cbFormaDePago.setStyle("");
        }

        if (errores.length() > 0) {
            lbErrorRegistro.setText(errores.toString());
            lbErrorRegistro.setVisible(true);
            return false;
        }

        lbErrorRegistro.setVisible(false);
        return true;
    }

    @FXML
    void registrarEmpresa(ActionEvent event) {
        if (!validarCamposRegistro()) {
            return;
        }

        try {
            EmpresaRepository repository = new EmpresaRepository();
            String rncIngresado = txtRNC.getText().trim();
            Empresa empresaExistente = repository.buscarEmpresa(rncIngresado);

            if (Objects.nonNull(empresaExistente.getNombre())) {
                mostrarAlerta("Error de Registro",
                        "Ya existe una empresa registrada con el RNC: " + rncIngresado + "\n\n" +
                                "Nombre de la empresa existente: " + empresaExistente.getNombre(),
                        Alert.AlertType.WARNING);
                return;
            }

            Empresa nuevaEmpresa = new Empresa();
            nuevaEmpresa.setNombre(txtNombreEmpresa.getText().trim());
            nuevaEmpresa.setRNC(Long.parseLong(rncIngresado));
            nuevaEmpresa.setCuentaBancaria(txtNoCuenta.getText().trim());
            nuevaEmpresa.setFormaPago(cbFormaDePago.getValue().toString());

            boolean registrado = repository.insertarEmpresa(nuevaEmpresa) == 1;

            if (registrado) {
                mostrarAlerta("Registro Exitoso",
                        "¡Empresa registrada correctamente!\n\n" +
                                "Nombre: " + nuevaEmpresa.getNombre() + "\n" +
                                "RNC: " + nuevaEmpresa.getRNC() + "\n" +
                                "Cuenta: " + nuevaEmpresa.getCuentaBancaria() + "\n" +
                                "Forma de Pago: " + nuevaEmpresa.getFormaPago(),
                        Alert.AlertType.INFORMATION);

                limpiarCamposRegistro();

            } else {
                mostrarAlerta("Error de Registro",
                        "No se pudo registrar la empresa. Por favor, intente nuevamente.",
                        Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato",
                    "El RNC debe contener solo números.\nPor favor, verifique los datos ingresados.",
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error del Sistema",
                    "Ocurrió un error inesperado: " + e.getMessage() + "\n\n" +
                            "Por favor, contacte al administrador del sistema.",
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void refrescarDatosPorId(Long idEmpresa) {
        if (idEmpresa == null) return;

        try {
            EmpresaRepository repository = new EmpresaRepository();
            EmpleadoRepository empleadoRepository = new EmpleadoRepository();

            Empresa empresaActualizada = repository.buscarEmpresaPorID(idEmpresa);

            if (empresaActualizada != null) {
                List<Empleado> listaEmpleadosNueva = empleadoRepository.listaEmpleado(idEmpresa);

                mostrarInformacionEmpresa(empresaActualizada, listaEmpleadosNueva);
                if (mainController != null) {
                    mainController.enviarDatosANomina(empresaActualizada, listaEmpleadosNueva);
                }

                System.out.println("Vista de Empresa y Nómina actualizada correctamente.");
            }
        } catch (Exception e) {
            System.err.println("Error al auto-actualizar empresa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limpiarCamposRegistro() {
        txtNombreEmpresa.clear();
        txtRNC.clear();
        txtNoCuenta.clear();
        cbFormaDePago.setValue(null);

        // Limpiar estilos
        txtNombreEmpresa.setStyle("");
        txtRNC.setStyle("");
        txtNoCuenta.setStyle("");
        cbFormaDePago.setStyle("");
        lbErrorRegistro.setVisible(false);
    }

    @FXML
    void consultarEmpresa(ActionEvent event) {
        // Validar campo de consulta
        String rncConsulta = txtConsultaRNC.getText().trim();

        if (rncConsulta.isEmpty()) {
            lbErrorConsulta.setText("• Por favor ingrese un RNC para consultar.");
            txtConsultaRNC.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return;
        }

        if (!RNC_PATTERN.matcher(rncConsulta).matches()) {
            lbErrorConsulta.setText("• El RNC debe tener exactamente 9 dígitos.");
            lbErrorConsulta.setVisible(true);
            txtConsultaRNC.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return;
        } else if (!Validaciones.esRNCValido(rncConsulta)) {
            lbErrorConsulta.setText("• El RNC ingresado no es válido.");
            lbErrorConsulta.setVisible(true);
            txtConsultaRNC.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return;
        }

        // Limpiar estilos de error
        txtConsultaRNC.setStyle("");
        lbErrorConsulta.setVisible(false);

        try {
            EmpresaRepository repository = new EmpresaRepository();
            EmpleadoRepository empleadoRepository = new EmpleadoRepository();

            Empresa empresaConsultada = repository.buscarEmpresa(rncConsulta);

            if (Objects.nonNull(empresaConsultada)) {
                List<Empleado> empleadoList = empleadoRepository.listaEmpleado(empresaConsultada.getId());

                if (mainController != null) {
                    mainController.compartirIdEmpresa(empresaConsultada.getId());
                    mainController.enviarDatosANomina(empresaConsultada, empleadoList);
                }

                mostrarInformacionEmpresa(empresaConsultada, empleadoList);

                mostrarAlerta("Consulta Exitosa",
                        "Empresa encontrada:\n" + empresaConsultada.getNombre(),
                        Alert.AlertType.INFORMATION);

            } else {
                mostrarAlerta("Empresa No Encontrada",
                        "No se encontró ninguna empresa registrada con el RNC: " + rncConsulta + "\n\n" +
                                "Por favor, verifique el RNC e intente nuevamente.",
                        Alert.AlertType.WARNING);

                limpiarCamposInformacion();
            }

        } catch (Exception e) {
            mostrarAlerta("Error de Consulta",
                    "Ocurrió un error al consultar la empresa: " + e.getMessage() + "\n\n" +
                            "Por favor, intente nuevamente o contacte al administrador.",
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void mostrarInformacionEmpresa(Empresa empresa, List<Empleado> empleados) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(50)).append("\n");
        sb.append("REPORTE DE EMPRESA - CONSULTA ACTUAL").append("\n");
        sb.append("=".repeat(50)).append("\n\n");

        sb.append("INFORMACIÓN GENERAL DE LA EMPRESA:\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format("%-15s: %s\n", "Nombre", safe(empresa.getNombre())));
        sb.append(String.format("%-15s: %s\n", "RNC", empresa.getRNC()));
        sb.append(String.format("%-15s: %s\n", "Cuenta Bco", safe(empresa.getCuentaBancaria())));
        sb.append(String.format("%-15s: %s\n", "Forma Pago", safe(empresa.getFormaPago())));
        sb.append(String.format("%-15s: %d\n", "Total Empleados", empleados.size()));
        sb.append("\n");

        sb.append("LISTADO DE EMPLEADOS REGISTRADOS:\n");
        sb.append("-".repeat(40)).append("\n");

        if (empleados.isEmpty()) {
            sb.append(" No hay empleados registrados en esta empresa.\n");
            sb.append("   Puede agregar empleados desde el menú 'Empleados'.\n");
        } else {
            double totalSalarios = 0;

            String formatoCabecera = "%-5s %-16s %-22s %15s\n";
            String formatoFila     = "%-5d %-16s %-22s RD$ %11s\n";

            sb.append(String.format(formatoCabecera, "No.", "Cédula", "Cuenta Bancaria", "Salario"));
            sb.append("-".repeat(65)).append("\n");
            int i = 1;
            for (Empleado emp : empleados) {
                String salarioStr = String.format("%,.2f", emp.getSalario());

                sb.append(String.format(formatoFila,
                        i,
                        safe(emp.getCedula()),
                        safe(emp.getCuentaBancaria()),
                        salarioStr));

                totalSalarios += emp.getSalario();
                i++;
            }

            sb.append("\n");
            sb.append("RESUMEN FINANCIERO:\n");
            sb.append("-".repeat(40)).append("\n");
            sb.append(String.format("Total en nómina mensual: RD$ %,.2f\n", totalSalarios));

            String formaPago = empresa.getFormaPago().toLowerCase();
            switch (formaPago) {
                case "quincenal":
                    sb.append(String.format("Pago quincenal estimado: RD$ %,.2f\n", totalSalarios / 2));
                    break;
                case "semanal":
                    sb.append(String.format("Pago semanal estimado: RD$ %,.2f\n", totalSalarios / 4));
                    break;
                case "mensual":
                    sb.append(String.format("Pago mensual: RD$ %,.2f\n", totalSalarios));
                    break;
            }
        }

        sb.append("\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("Consulta realizada: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        txtAreaDetalles.setText(sb.toString());
        txtAreaDetalles.setScrollTop(0); // Ir al inicio del texto
    }

    private void limpiarCamposInformacion() {
        lbNombreEmpresa.setVisible(false);
        lbCuentaBancaria.setVisible(false);
        lbFormaPago.setVisible(false);
        lbCantidadEmpleados.setVisible(false);
        txtAreaDetalles.clear();
    }

    @FXML
    void limpiarCampos(ActionEvent event) {
        limpiarCamposRegistro();
        txtConsultaRNC.clear();
        txtConsultaRNC.setStyle("");
        lbErrorConsulta.setVisible(false);
        limpiarCamposInformacion();

        mostrarAlerta("Campos Limpiados",
                "Todos los campos han sido limpiados.\nPuede comenzar una nueva operación.",
                Alert.AlertType.INFORMATION);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Personalizar botones según el tipo de alerta
        if (tipo == Alert.AlertType.CONFIRMATION) {
            ButtonType botonSi = new ButtonType("Sí", ButtonBar.ButtonData.YES);
            ButtonType botonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(botonSi, botonNo);
        }

        alert.showAndWait();
    }

    private String safe(String s) {
        return s == null ? "No especificado" : s;
    }

    public void recibirDatosUsuario(String nombre) {
        lbNombreTab.setText("Sr(a). " + nombre);
        lbNombreTab.setVisible(true);
        lbTabUsuarioDeseo.setVisible(true);
    }
}