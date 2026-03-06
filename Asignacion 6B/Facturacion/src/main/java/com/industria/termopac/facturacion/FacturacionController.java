package com.industria.termopac.facturacion;

import com.industria.termopac.facturacion.entity.DetalleFila;
import com.industria.termopac.facturacion.entity.ProductoCombo;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class FacturacionController {

    private static final String URL = "jdbc:mysql://localhost:3306/TERMOPAC?useSSL=false&serverTimezone=UTC";
    private static final String USER = "dward";
    private static final String PASS = "DuM#12345@";


    @FXML private TextField txtNumeroFactura, txtCliente, txtCantidad, txtPrecio;
    @FXML private ComboBox<ProductoCombo> cmbProductos;
    @FXML private TableView<DetalleFila> tablaDetalles;
    @FXML private TableColumn<DetalleFila, String> colProducto;
    @FXML private TableColumn<DetalleFila, Integer> colCantidad;
    @FXML private TableColumn<DetalleFila, Double> colPrecio, colSubtotal;

    private ObservableList<ProductoCombo> listaProductos = FXCollections.observableArrayList();
    private ObservableList<DetalleFila> listaDetalles = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colProducto.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        colPrecio.setCellValueFactory(cellData -> cellData.getValue().precioProperty().asObject());
        colSubtotal.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty().asObject());

        tablaDetalles.setItems(listaDetalles);

        cargarProductos();
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private void cargarProductos() {
        String sql = "SELECT id_articulo, nombre, existencia FROM inventario WHERE existencia > 0";
        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                listaProductos.add(new ProductoCombo(
                        rs.getInt("id_articulo"),
                        rs.getString("nombre"),
                        rs.getInt("existencia")
                ));
            }
            cmbProductos.setItems(listaProductos);
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar productos", e.getMessage());
        }
    }

    @FXML
    protected void onAgregarDetalleClick() {
        ProductoCombo prodSeleccionado = cmbProductos.getValue();
        if (prodSeleccionado == null || txtCantidad.getText().isEmpty() || txtPrecio.getText().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Llene todos los campos del producto.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(txtCantidad.getText());
            double precio = Double.parseDouble(txtPrecio.getText());

            if (cantidad > prodSeleccionado.getExistencia()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Sin Stock", "Solo hay " + prodSeleccionado.getExistencia() + " unidades disponibles.");
                return;
            }

            listaDetalles.add(new DetalleFila(prodSeleccionado.getId(), prodSeleccionado.getNombre(), cantidad, precio));

            txtCantidad.clear();
            txtPrecio.clear();
            cmbProductos.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Cantidad o Precio inválidos.");
        }
    }

    @FXML
    protected void onProcesarFacturaClick() {
        if (txtNumeroFactura.getText().isEmpty() || txtCliente.getText().isEmpty() || listaDetalles.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Faltan datos de la factura o el carrito está vacío.");
            return;
        }

        String sqlFactura = "INSERT INTO factura (numero_factura, cliente) VALUES (?, ?)";
        String sqlDetalle = "INSERT INTO detalle_factura (id_factura, id_articulo, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtFactura = conn.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstmtDetalle = conn.prepareStatement(sqlDetalle)) {

                pstmtFactura.setString(1, txtNumeroFactura.getText());
                pstmtFactura.setString(2, txtCliente.getText());

                pstmtFactura.executeUpdate();

                ResultSet rsFactura = pstmtFactura.getGeneratedKeys();
                int idFacturaGenerada = 0;
                if (rsFactura.next()) {
                    idFacturaGenerada = rsFactura.getInt(1);
                }

                for (DetalleFila fila : listaDetalles) {
                    pstmtDetalle.setInt(1, idFacturaGenerada);
                    pstmtDetalle.setInt(2, fila.getIdArticulo());
                    pstmtDetalle.setInt(3, fila.getCantidad());
                    pstmtDetalle.setDouble(4, fila.getPrecio());
                    pstmtDetalle.addBatch();
                }

                pstmtDetalle.executeBatch();
                conn.commit();

                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Factura procesada. El inventario se ha actualizado automáticamente.");
                limpiarTodo();

            } catch (SQLException ex) {
                conn.rollback();
                mostrarAlerta(Alert.AlertType.ERROR, "Error de BD", ex.getMessage());
            }
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", e.getMessage());
        }
    }

    private void limpiarTodo() {
        txtNumeroFactura.clear();
        txtCliente.clear();
        listaDetalles.clear();
        listaProductos.clear();
        cargarProductos();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}