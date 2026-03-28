package com.industria.termopac.inventario;

import com.industria.termopac.entity.Articulo;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class InventarioTermopacApp extends Application {

    private static final String URL = "jdbc:mysql://localhost:3306/TERMOPAC?useSSL=false&serverTimezone=UTC";
    private static final String USER = "dward";
    private static final String PASS = "12345@";

    private TableView<Articulo> tabla;
    private TextField txtCodigo, txtNombre, txtExistencia;
    private Button btnAgregar, btnModificar, btnEliminar, btnLimpiar;
    private Articulo articuloSeleccionado = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Termopac - Gestión de Inventarios");

        HBox cabecera = new HBox(15);
        cabecera.setPadding(new Insets(10));
        cabecera.setAlignment(Pos.CENTER_LEFT);

        try {
            Image logo = new Image("file:logo.png", 150, 0, true, true);
            ImageView imageView = new ImageView(logo);
            cabecera.getChildren().add(imageView);
        } catch (Exception e) {
            System.out.println("No se encontró el archivo logo.png");
        }

        Label lblTitulo = new Label("Módulo de Inventarios");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        cabecera.getChildren().add(lblTitulo);

        GridPane formulario = new GridPane();
        formulario.setPadding(new Insets(10));
        formulario.setHgap(10);
        formulario.setVgap(10);

        txtCodigo = new TextField();
        txtNombre = new TextField();
        txtExistencia = new TextField();

        formulario.add(new Label("Código:"), 0, 0);
        formulario.add(txtCodigo, 1, 0);
        formulario.add(new Label("Nombre:"), 0, 1);
        formulario.add(txtNombre, 1, 1);
        formulario.add(new Label("Existencia:"), 0, 2);
        formulario.add(txtExistencia, 1, 2);

        HBox panelBotones = new HBox(10);
        btnAgregar = new Button("Agregar");
        btnModificar = new Button("Modificar");
        btnEliminar = new Button("Eliminar");
        btnLimpiar = new Button("Limpiar Campos");
        panelBotones.getChildren().addAll(btnAgregar, btnModificar, btnEliminar, btnLimpiar);
        formulario.add(panelBotones, 1, 3);

        tabla = new TableView<>();
        TableColumn<Articulo, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Articulo, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<Articulo, Integer> colExistencia = new TableColumn<>("Existencia");
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));

        tabla.getColumns().addAll(colCodigo, colNombre, colExistencia);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                articuloSeleccionado = newSelection;
                txtCodigo.setText(newSelection.getCodigo());
                txtNombre.setText(newSelection.getNombre());
                txtExistencia.setText(String.valueOf(newSelection.getExistencia()));
            }
        });

        btnAgregar.setOnAction(e -> agregarArticulo());
        btnModificar.setOnAction(e -> modificarArticulo());
        btnEliminar.setOnAction(e -> eliminarArticulo());
        btnLimpiar.setOnAction(e -> limpiarCampos());

        VBox layoutPrincipal = new VBox(10);
        layoutPrincipal.setPadding(new Insets(15));
        layoutPrincipal.getChildren().addAll(cabecera, formulario, tabla);

        Scene scene = new Scene(layoutPrincipal, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        cargarDatos();
    }


    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private void cargarDatos() {
        ObservableList<Articulo> lista = FXCollections.observableArrayList();
        String sql = "SELECT id_articulo, codigo_producto, nombre, existencia FROM inventario";

        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Articulo(
                        rs.getInt("id_articulo"),
                        rs.getString("codigo_producto"),
                        rs.getString("nombre"),
                        rs.getInt("existencia")
                ));
            }
            tabla.setItems(lista);
        } catch (SQLException e) {
            mostrarAlerta("Error al cargar datos: " + e.getMessage());
        }
    }

    private void agregarArticulo() {
        String sql = "INSERT INTO inventario (codigo_producto, nombre, existencia) VALUES (?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtCodigo.getText());
            pstmt.setString(2, txtNombre.getText());
            pstmt.setInt(3, Integer.parseInt(txtExistencia.getText()));
            pstmt.executeUpdate();
            cargarDatos();
            limpiarCampos();
        } catch (SQLException | NumberFormatException e) {
            mostrarAlerta("Error al agregar: Verifique los datos. " + e.getMessage());
        }
    }

    private void modificarArticulo() {
        if (articuloSeleccionado == null) {
            mostrarAlerta("Seleccione un artículo de la tabla para modificar.");
            return;
        }
        String sql = "UPDATE inventario SET codigo_producto = ?, nombre = ?, existencia = ? WHERE id_articulo = ?";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtCodigo.getText());
            pstmt.setString(2, txtNombre.getText());
            pstmt.setInt(3, Integer.parseInt(txtExistencia.getText()));
            pstmt.setInt(4, articuloSeleccionado.getId());
            pstmt.executeUpdate();
            cargarDatos();
            limpiarCampos();
        } catch (SQLException | NumberFormatException e) {
            mostrarAlerta("Error al modificar: " + e.getMessage());
        }
    }

    private void eliminarArticulo() {
        if (articuloSeleccionado == null) {
            mostrarAlerta("Seleccione un artículo de la tabla para eliminar.");
            return;
        }
        String sql = "DELETE FROM inventario WHERE id_articulo = ?";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, articuloSeleccionado.getId());
            pstmt.executeUpdate();
            cargarDatos();
            limpiarCampos();
        } catch (SQLException e) {
            mostrarAlerta("Error al eliminar: " + e.getMessage());
        }
    }

    private void limpiarCampos() {
        txtCodigo.clear();
        txtNombre.clear();
        txtExistencia.clear();
        articuloSeleccionado = null;
        tabla.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}