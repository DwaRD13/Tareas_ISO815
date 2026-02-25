package com.fundapec.payment.management;

import com.fundapec.payment.management.db.PagoRepository;
import com.fundapec.payment.management.entity.DetallePago;
import com.fundapec.payment.management.entity.Estudiante;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegistroPagoController {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnExportar;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnSeleccionaRuta;

    @FXML
    private DatePicker dpFechaAprobacion;

    @FXML
    private Label lbRutaSeleccionada;

    @FXML
    private TextField txtCodigoPago;

    @FXML
    private TextField txtDescPeriodo;

    @FXML
    private TextField txtEstado;

    @FXML
    private TextField txtMatricula;

    @FXML
    private TextField txtMoneda;

    @FXML
    private TextField txtMonto;

    @FXML
    private TextField txtPeriodo;

    @FXML private Label lbErrorEstudiante;
    @FXML private Label lbErrorPago;

    private File directorioSeleccionado;

    private PagoRepository pagoRepository = new PagoRepository();

    @FXML
    public void initialize() {
        setupValidators();

        lbErrorEstudiante.setVisible(false);
        lbErrorPago.setVisible(false);
    }

    private void setupValidators() {
        txtMatricula.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z0-9]*")) {
                txtMatricula.setText(newVal.replaceAll("[^a-zA-Z0-9]", ""));
            }
            if (txtMatricula.getText().length() > 9) {
                txtMatricula.setText(oldVal);
            }
        });

        txtEstado.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 14) {
                txtEstado.setText(oldVal);
            }
        });

        txtPeriodo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 7) {
                txtPeriodo.setText(oldVal);
            }
        });

        txtDescPeriodo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 17) {
                txtDescPeriodo.setText(oldVal);
            }
        });

        txtCodigoPago.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 20) {
                txtCodigoPago.setText(oldVal);
            }
        });

        txtMonto.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 9) {
                txtMonto.setText(oldVal);
            }
        });

        txtMoneda.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 3) {
                txtMoneda.setText(oldVal);
            }
        });

        dpFechaAprobacion.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 10) {
                dpFechaAprobacion.getEditor().setText(oldVal);
            }
        });
    }

    @FXML
    void generarArchivo(ActionEvent event) {
        // 1. Validar que se haya seleccionado una ruta
        if (directorioSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ruta no seleccionada",
                    "Por favor, use el botón 'Seleccionar Carpeta' antes de exportar.");
            return;
        }

        // 2. Validar datos mínimos antes de escribir
        if (txtMatricula.getText().isEmpty() || txtMonto.getText().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos",
                    "No se puede generar el archivo sin Matrícula ni Monto.");
            return;
        }

        try {
            String jsonContent = construirJsonString();

            String nombreArchivo = "pago_" + txtMatricula.getText() + ".json";
            File archivo = new File(directorioSeleccionado, nombreArchivo);

            try (FileWriter writer = new FileWriter(archivo)) {
                writer.write(jsonContent);
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación Exitosa",
                    "Se ha generado el archivo correctamente en:\n" + archivo.getAbsolutePath());

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato",
                    "El monto debe ser numérico para generar el JSON.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al Exportar",
                    "No se pudo crear el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void guardarRegistro(ActionEvent event) {
        try {
            boolean valido = true;
            StringBuilder erroresEstudiante = new StringBuilder();
            StringBuilder erroresPago = new StringBuilder();

            if (txtMatricula.getText().isEmpty()) {
                erroresEstudiante.append("• La matrícula es obligatoria.\n");
                valido = false;
            }

            if (!txtPeriodo.getText().matches("\\d{4}-C[123]")) {
                erroresEstudiante.append("• Formato periodo: YYYY-C# (ej: 2026-C1).\n");
                valido = false;
            }

            if (txtDescPeriodo.getText().length() > 17) {
                erroresEstudiante.append("• La descripción no puede exceder 17 caracteres.\n");
                valido = false;
            }

            if (!txtCodigoPago.getText().matches("FUNDAPEC-CR-\\d{1,6}")) {
                erroresPago.append("• Formato código: FUNDAPEC-CR-#### (máx 6 dígitos).\n");
                valido = false;
            }

            if (txtMonto.getText().isEmpty()) {
                erroresPago.append("• El monto es obligatorio.\n");
                valido = false;
            } else {
                try {
                    Double.parseDouble(txtMonto.getText());
                } catch (NumberFormatException e) {
                    erroresPago.append("• El monto debe ser numérico.\n");
                    valido = false;
                }
            }

//            String fechaTexto = dpFechaAprobacion.getEditor().getText();
//            if (!fechaTexto.matches("\\d{2}/\\d{2}/\\d{4}")) {
//                erroresPago.append("• Formato fecha: dd/mm/yyyy.\n");
//                valido = false;
//            }

            lbErrorEstudiante.setText(erroresEstudiante.toString());
            lbErrorEstudiante.setVisible(erroresEstudiante.length() > 0);

            lbErrorPago.setText(erroresPago.toString());
            lbErrorPago.setVisible(erroresPago.length() > 0);

            if (!valido) {
                mostrarAlerta(Alert.AlertType.ERROR, "Errores de validación",
                        "Corrige los campos antes de continuar.");
                return;

            }

            Estudiante est = new Estudiante(
                    txtMatricula.getText(),
                    txtEstado.getText(),
                    txtPeriodo.getText(),
                    txtDescPeriodo.getText()
            );

            LocalDate fecha = dpFechaAprobacion.getValue();
            if (fecha == null) fecha = LocalDate.now();

            DetallePago pago = new DetallePago(
                    txtCodigoPago.getText(),
                    Double.parseDouble(txtMonto.getText()),
                    txtMoneda.getText(),
                    fecha,
                    est.getMatricula()
            );

            pagoRepository.save(est, pago);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Pago registrado correctamente en BD.");

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error SQL", "El código de pago ya existe.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error SQL", "No se pudo conectar a la BD: " + e.getMessage());
            }
            e.printStackTrace();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void seleccionarRuta(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta para Exportación");

        Stage stage = (Stage) btnSeleccionaRuta.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            this.directorioSeleccionado = selectedDirectory;
            lbRutaSeleccionada.setText("Ruta: " + selectedDirectory.getAbsolutePath());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private String construirJsonString() {
        LocalDate fecha = dpFechaAprobacion.getValue();
        String fechaStr = (fecha != null) ? fecha.toString() : LocalDate.now().toString();

        DateTimeFormatter fmtPeriodo = DateTimeFormatter.ofPattern("yyyyMM");
        String periodoActual = LocalDate.now().format(fmtPeriodo);

        long sufijo = System.currentTimeMillis() % 1000000;
        String transaccionId = "CRE-" + periodoActual + "-" + String.format("%06d", sufijo);

        String timestamp = Instant.now().toString();

        double monto = Double.parseDouble(txtMonto.getText());

        return "{\n" +
                "  \"metadata\": {\n" +
                "    \"transacion_id\": \"" + transaccionId + "\",\n" +
                "    \"timestamp\": \"" + timestamp + "\",\n" +
                "    \"sistema_origen\": \"FUNDAPEC\"\n" +
                "  },\n" +
                "  \"estudiante\": {\n" +
                "    \"matricula\": \"" + txtMatricula.getText() + "\",\n" +
                "    \"estado\": \"" + txtEstado.getText() + "\",\n" +
                "    \"periodo_academico\": \"" + txtPeriodo.getText() + "\",\n" +
                "    \"descripcion_periodo\": \"" + txtDescPeriodo.getText() + "\"\n" +
                "  },\n" +
                "  \"detalle_pago\": {\n" +
                "    \"codigo_pago\": \"" + txtCodigoPago.getText() + "\",\n" +
                "    \"monto_aprobado\": " + monto + ",\n" +
                "    \"moneda\": \"" + txtMoneda.getText() + "\",\n" +
                "    \"fecha_aprobacion\": \"" + fechaStr + "\"\n" +
                "  }\n" +
                "}";
    }
}
