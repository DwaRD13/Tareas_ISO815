package com.unapec.nominagenerator;

import com.google.gson.*;
import com.unapec.entity.Empleado;
import com.unapec.entity.Empresa;

import com.unapec.entity.NominaDTO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.lang.reflect.Type;
import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NominaController {

    @FXML private Button btnPagarConApap;

    @FXML private Label lbNombreTab;
    @FXML private Label lbTabUsuarioDeseo;
    @FXML private Label lbFrecuenciaPago;
    @FXML private Label lbTotalNomina;

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

    public void recibirDatosUsuario(String nombre) {
        lbNombreTab.setText("Sr(a). " + nombre);
        lbNombreTab.setVisible(true);
    }

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @FXML
    void enviarDatosAPython(ActionEvent event) {
        if (dpPeriodoNomina.getValue() == null) {
            mostrarAlerta("Atención", "Por favor seleccione la FECHA DE PAGO en el calendario.");
            return;
        }

        if (listaDetalles == null || listaDetalles.isEmpty()) {
            mostrarAlerta("Error", "No hay datos de nómina para enviar.");
            return;
        }

        btnPagarConApap.setDisable(true);
        String fechaFormateada = dpPeriodoNomina.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String jsonPayload = buildBody(this.empresaActual, fechaFormateada);
        System.out.println(jsonPayload);

        String urlApi = "http://localhost:8080/api/nomina/recibirDatos";

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, java.nio.charset.StandardCharsets.UTF_8))
                .uri(URI.create(urlApi))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(respuesta -> {
                    Platform.runLater(() -> {
                        if (respuesta.statusCode() == 200 || respuesta.statusCode() == 201) {
                            mostrarAlerta("Éxito", "Nómina enviada correctamente a APAP vía Python.");
                        } else {
                            mostrarAlerta("Error API (" + respuesta.statusCode() + ")",
                                    "Detalle del servidor: \n" + respuesta.body());
                        }
                        btnPagarConApap.setDisable(false);
                    });
                });
    }

    public String buildBody(Empresa empresaActual, String fechaFormateada) {
        double totalPagado = 0;
        int cantidadRegistros = 0;

        for (DetalleNomina detalle : listaDetalles) {
            totalPagado += detalle.getSueldoNeto();
            cantidadRegistros++;
        }

        NominaDTO nominaDTO = new NominaDTO();
        nominaDTO.setEmpresa(empresaActual);
        nominaDTO.setFechaPago(fechaFormateada);
        nominaDTO.setTotalPagado(totalPagado);
        nominaDTO.setCantidadRegistros(cantidadRegistros);
        nominaDTO.setDetalles(listaDetalles);

        JsonSerializer<LocalDate> localDateSerializer = new JsonSerializer<LocalDate>() {
            @Override
            public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        };

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateSerializer)
                .create();

        return gson.toJson(nominaDTO);
    }


    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}