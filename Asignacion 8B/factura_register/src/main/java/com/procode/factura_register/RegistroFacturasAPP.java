package com.procode.factura_register;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.io.File;
import java.sql.*;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class RegistroFacturasAPP extends Application {

    private static final String URL = "jdbc:mysql://localhost:3306/SISTEMA_CONTABLE?useSSL=false";
    private static final String USER = "dward";
    private static final String PASS = "DuM#12345@";

    private TableView<Factura> tabla;
    private TextField txtNoFactura, txtIdCliente, txtMonto, txtCondiciones, txtEstado;
    private DatePicker dtpFecha;
    private Button btnAgregar, btnVerImagen, btnLimpiar;
    private Factura facturaSeleccionada = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Registro de Facturas (Generación Digital)");

        VBox layoutPrincipal = new VBox(10);
        layoutPrincipal.setPadding(new Insets(15));

        GridPane formulario = new GridPane();
        formulario.setPadding(new Insets(10));
        formulario.setHgap(10); formulario.setVgap(10);

        txtNoFactura = new TextField();
        txtIdCliente = new TextField();
        txtMonto = new TextField();
        txtCondiciones = new TextField();
        txtEstado = new TextField();
        dtpFecha = new DatePicker(LocalDate.now());

        formulario.add(new Label("No. Factura:"), 0, 0); formulario.add(txtNoFactura, 1, 0);
        formulario.add(new Label("ID Cliente:"), 0, 1); formulario.add(txtIdCliente, 1, 1);
        formulario.add(new Label("Monto ($):"), 0, 2); formulario.add(txtMonto, 1, 2);
        formulario.add(new Label("Fecha:"), 2, 0); formulario.add(dtpFecha, 3, 0);
        formulario.add(new Label("Condiciones:"), 2, 1); formulario.add(txtCondiciones, 3, 1);
        formulario.add(new Label("Estado:"), 2, 2); formulario.add(txtEstado, 3, 2);

        HBox panelBotones = new HBox(10);
        btnAgregar = new Button("💾 Guardar y Generar Imagen");
        btnAgregar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnVerImagen = new Button("🔍 Abrir Visor (App B)");
        btnLimpiar = new Button("Limpiar");

        panelBotones.getChildren().addAll(btnAgregar, btnVerImagen, btnLimpiar);
        formulario.add(panelBotones, 1, 3, 3, 1);

        tabla = new TableView<>();
        configurarTabla();

        // Eventos
        btnAgregar.setOnAction(e -> procesarFactura());
        btnVerImagen.setOnAction(e -> abrirVisorDocumentos());
        btnLimpiar.setOnAction(e -> limpiarCampos());

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                facturaSeleccionada = newVal;
                mapearFacturaAFormulario(newVal);
            }
        });

        layoutPrincipal.getChildren().addAll(new Label("Gestión de Facturación"), formulario, tabla);
        primaryStage.setScene(new Scene(layoutPrincipal, 850, 600));
        primaryStage.show();
        cargarDatos();
    }

    private void configurarTabla() {
        TableColumn<Factura, String> colNo = new TableColumn<>("No. Factura");
        colNo.setCellValueFactory(new PropertyValueFactory<>("noFactura"));

        TableColumn<Factura, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));

        TableColumn<Factura, Double> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
        colMonto.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                setText(empty || precio == null ? null : formatoMoneda.format(precio));
            }
        });

        TableColumn<Factura, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<Factura, String> colCondicion = new TableColumn<>("Condiciones");
        colCondicion.setCellValueFactory(new PropertyValueFactory<>("condiciones"));
        tabla.getColumns().addAll(colNo, colCliente, colMonto, colEstado, colCondicion);
    }

    private void procesarFactura() {
        if (txtNoFactura.getText().isEmpty()) {
            mostrarAlerta("El número de factura es obligatorio.");
            return;
        }

        String sql = "INSERT INTO facturas (no_factura, condiciones, id_cliente, fecha_factura, monto, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtNoFactura.getText());
            pstmt.setString(2, txtCondiciones.getText());
            pstmt.setString(3, txtIdCliente.getText());
            pstmt.setString(4, dtpFecha.getValue().toString());
            pstmt.setDouble(5, Double.parseDouble(txtMonto.getText()));
            pstmt.setString(6, txtEstado.getText());

            pstmt.executeUpdate();

            generarImagenDesdeDatos(); // Genera el archivo .png

            mostrarAlerta("Éxito: Datos e Imagen guardados.");
            cargarDatos();
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error: " + e.getMessage());
        }
    }

    private void generarImagenDesdeDatos() {
        VBox plantilla = new VBox(20);
        plantilla.setPadding(new Insets(40));
        plantilla.setAlignment(Pos.TOP_CENTER);
        plantilla.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2;");
        plantilla.setPrefSize(450, 550);

        Label titulo = new Label("FACTURA DIGITAL");
        titulo.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: black;");

        VBox detalles = new VBox(12);
        detalles.getChildren().addAll(
                new Label("Factura No: " + txtNoFactura.getText()),
                new Label("Fecha Emisión: " + dtpFecha.getValue()),
                new Label("ID Cliente: " + txtIdCliente.getText()),
                new Label("---------------------------------------"),
                new Label("Monto Total: $" + txtMonto.getText()),
                new Label("Condiciones: " + txtCondiciones.getText()),
                new Label("Estado Actual: " + txtEstado.getText())
        );
        detalles.getChildren().forEach(n -> n.setStyle("-fx-text-fill: black; -fx-font-size: 14;"));

        plantilla.getChildren().addAll(titulo, detalles);

        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(javafx.scene.paint.Color.WHITE);

        new Scene(plantilla);
        WritableImage snapshot = plantilla.snapshot(sp, null);

        String rutaDigitalizaciones = "C:\\Users\\darwi\\Desktop\\Escritorio en el Escritorio\\PROGRAMACION\\Proyectos\\Tareas_ISO815\\Asignacion 8B\\digitalizaciones";

        File carpeta = new File(rutaDigitalizaciones);
        if (!carpeta.exists()) carpeta.mkdir();

        File archivo = new File(carpeta, txtNoFactura.getText() + ".png");

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", archivo);
            System.out.println("Imagen generada: " + archivo.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error al escribir imagen: " + e.getMessage());
        }
    }

    private void mapearFacturaAFormulario(Factura f) {
        txtNoFactura.setText(f.getNoFactura());
        txtIdCliente.setText(f.getIdCliente());
        txtMonto.setText(String.valueOf(f.getMonto()));
        txtCondiciones.setText(f.getCondiciones());
        txtEstado.setText(f.getEstado());
        try {
            dtpFecha.setValue(LocalDate.parse(f.getFechaFactura()));
        } catch (Exception e) {
            dtpFecha.setValue(LocalDate.now());
        }
    }

    private void cargarDatos() {
        ObservableList<Factura> lista = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM facturas")) {
            while (rs.next()) {
                lista.add(new Factura(
                        rs.getString("no_factura"), rs.getString("condiciones"),
                        rs.getString("id_cliente"), rs.getString("fecha_factura"),
                        rs.getDouble("monto"), rs.getString("estado")
                ));
            }
            tabla.setItems(lista);
        } catch (SQLException e) {
            System.out.println("Error DB: " + e.getMessage());
        }
    }

    private void abrirVisorDocumentos() {
        if (facturaSeleccionada == null) {
            mostrarAlerta("Seleccione una factura de la tabla primero.");
            return;
        }

        try {
            String noFactura = facturaSeleccionada.getNoFactura();
            String rutaRaiz = "C:\\Users\\darwi\\Desktop\\Escritorio en el Escritorio\\PROGRAMACION\\Proyectos\\Tareas_ISO815\\Asignacion 8B";

            String rutaJavaFX = "C:\\Users\\darwi\\Desktop\\Escritorio en el Escritorio\\PROGRAMACION\\Proyectos\\Librerias\\javafx-sdk-17.0.18\\lib";

            ProcessBuilder pb = new ProcessBuilder(
                    "java",
                    "--module-path", rutaJavaFX,
                    "--add-modules", "javafx.controls,javafx.graphics,javafx.swing",
                    "-jar", "visor_factura.jar",
                    noFactura
            );

            pb.directory(new File(rutaRaiz));
            pb.start();
            System.out.println("Visor iniciado para factura: " + noFactura);

        } catch (IOException e) {
            mostrarAlerta("Error: No se pudo ejecutar el visor. Revisa que el JAR esté en Asignacion 8B.");
        }
    }

    private void limpiarCampos() {
        txtNoFactura.clear(); txtIdCliente.clear(); txtMonto.clear();
        txtCondiciones.clear(); txtEstado.clear(); dtpFecha.setValue(LocalDate.now());
        facturaSeleccionada = null;
        tabla.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}