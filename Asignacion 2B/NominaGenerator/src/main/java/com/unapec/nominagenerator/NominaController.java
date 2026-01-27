package com.unapec.nominagenerator;

import com.unapec.entity.Empleado;
import com.unapec.entity.Empresa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class NominaController {

    @FXML private Button btnExportarNomina;
    @FXML private Button btnSeleccionaRuta;

    @FXML private Label lbNombreTab;
    @FXML private Label lbTabUsuarioDeseo;
    @FXML private Label lbFrecuenciaPago;
    @FXML private Label lbTotalNomina;
    @FXML private Label lbRutaSeleccionada;

    @FXML private DatePicker dpPeriodoNomina;

    @FXML private TableView<DetalleNomina> tbNomina;
    @FXML private TableColumn<DetalleNomina, String> colCedula;
    @FXML private TableColumn<DetalleNomina, String> colCuenta;
    @FXML private TableColumn<DetalleNomina, String> colTipoCuenta;
    @FXML private TableColumn<DetalleNomina, Double> colBruto;
    @FXML private TableColumn<DetalleNomina, Double> colTSS;
    @FXML private TableColumn<DetalleNomina, Double> colNeto;

    private static final double TASA_SFS = 0.0304;
    private static final double TASA_AFP = 0.0287;

    private ObservableList<DetalleNomina> listaDetalles;
    private Empresa empresaActual;
    private File directorioDestino;

    @FXML
    public void initialize() {
        configurarColumnas();
    }

    private void configurarColumnas() {
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colCuenta.setCellValueFactory(new PropertyValueFactory<>("cuentaDestino"));
        colTipoCuenta.setCellValueFactory(new PropertyValueFactory<>("tipoCuenta"));
        colBruto.setCellValueFactory(new PropertyValueFactory<>("sueldoBrutoPeriodo"));
        colTSS.setCellValueFactory(new PropertyValueFactory<>("descuentoTSS"));
        colNeto.setCellValueFactory(new PropertyValueFactory<>("sueldoNeto"));
    }

    public void cargarDatosNomina(Empresa empresa, List<Empleado> empleados) {
        this.empresaActual = empresa;
        listaDetalles = FXCollections.observableArrayList();

        lbFrecuenciaPago.setText("Frecuencia: " + empresa.getFormaPago());

        int divisor = 1;
        if (empresa.getFormaPago().equalsIgnoreCase("Quincenal")) {
            divisor = 2;
        } else if (empresa.getFormaPago().equalsIgnoreCase("Semanal")) {
            divisor = 4;
        }

        double totalAcumulado = 0;

        for (Empleado emp : empleados) {
            double salarioMensual = emp.getSalario();
            double brutoPeriodo = salarioMensual / divisor;

            double descuentoSFS = brutoPeriodo * TASA_SFS;
            double descuentoAFP = brutoPeriodo * TASA_AFP;
            double totalTSS = descuentoSFS + descuentoAFP;

            double neto = brutoPeriodo - totalTSS;

            totalAcumulado += neto;

            DetalleNomina detalle = new DetalleNomina(
                    emp.getCedula(),
                    emp.getTipoDeCuenta(),
                    emp.getCuentaBancaria(),
                    Math.round(brutoPeriodo * 100.0) / 100.0,
                    Math.round(totalTSS * 100.0) / 100.0,
                    Math.round(neto * 100.0) / 100.0
            );
            listaDetalles.add(detalle);
        }

        tbNomina.setItems(listaDetalles);
        lbTotalNomina.setText("Total a Pagar: RD$ " + String.format("%,.2f", totalAcumulado));
    }

    @FXML
    private void seleccionarRuta(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta para Guardar Nómina");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            this.directorioDestino = selectedDirectory;
            lbRutaSeleccionada.setText("Ruta: " + selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void exportarTxt(ActionEvent event) {
        if (listaDetalles == null || listaDetalles.isEmpty()) {
            mostrarAlerta("Error", "No hay datos para exportar.");
            return;
        }

        // --- VALIDACIÓN DEL DATEPICKER ---
        if (dpPeriodoNomina.getValue() == null) {
            mostrarAlerta("Atención", "Por favor seleccione la FECHA DE PAGO en el calendario.");
            return;
        }

        String fechaFormateada = dpPeriodoNomina.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        generarArchivo(fechaFormateada);
    }

    private void generarArchivo(String fechaPago) {
        String nombreArchivo = "Nomina_" + empresaActual.getNombre() + ".txt";

        File file;
        if (directorioDestino != null) {
            file = new File(directorioDestino, nombreArchivo);
        } else {
            file = new File(nombreArchivo);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            // --- 1. ENCABEZADO ---
            StringBuilder header = new StringBuilder();
            String fechaTransmision = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            String fechaPeriodo = formatearTexto(fechaPago, 10);

            header.append("E");
            header.append("NOM");
            header.append(formatearNumerico(empresaActual.getRNC(), 9));
            header.append(formatearNumerico(empresaActual.getCuentaBancaria(), 12));
            header.append(fechaPeriodo);
            header.append(fechaTransmision);

            writer.write(header.toString());
            writer.newLine();

            // --- 2. DETALLES ---
            double totalPagado = 0;
            int cantidadRegistros = 0;

            for (DetalleNomina det : listaDetalles) {
                StringBuilder detalle = new StringBuilder();

                String tipoCtaCorto = "NO";
                if(det.getTipoCuenta() != null && det.getTipoCuenta().length() >= 2){
                    tipoCtaCorto = det.getTipoCuenta().substring(0, 2).toUpperCase();
                }

                detalle.append("D");
                detalle.append(formatearNumerico(det.getCedula(), 11));
                detalle.append(formatearNumerico(det.getCuentaDestino(), 20));
                detalle.append(formatearMonto(det.getSueldoNeto(), 10));
                detalle.append(formatearTexto(tipoCtaCorto, 2));

                writer.write(detalle.toString());
                writer.newLine();

                totalPagado += det.getSueldoNeto();
                cantidadRegistros++;
            }

            // --- 3. SUMARIO ---
            StringBuilder sumario = new StringBuilder();
            sumario.append("S");
            sumario.append(formatearNumerico(cantidadRegistros, 9));
            sumario.append(formatearMonto(totalPagado, 14));

            writer.write(sumario.toString());

            mostrarAlerta("Éxito", "Archivo generado correctamente en:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    private String formatearNumerico(Object valor, int longitud) {
        String str = (valor == null) ? "" : String.valueOf(valor);
        str = str.replaceAll("[^0-9]", "");
        if (str.length() > longitud) {
            return str.substring(0, longitud);
        }
        return String.format("%" + longitud + "s", str).replace(' ', '0');
    }

    private String formatearMonto(double valor, int longitud) {
        String str = String.format(Locale.US, "%.2f", valor);
        if (str.length() > longitud) {
            return str.substring(0, longitud);
        }
        return String.format("%" + longitud + "s", str).replace(' ', '0');
    }

    private String formatearTexto(String valor, int longitud) {
        String str = (valor == null) ? "" : valor;
        if (str.length() > longitud) {
            return str.substring(0, longitud);
        }
        return String.format("%-" + longitud + "s", str);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void recibirDatosUsuario(String nombre) {
        lbNombreTab.setText("Sr(a). " + nombre);
        lbNombreTab.setVisible(true);
    }
}