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

import java.util.List;
import java.util.Objects;

public class EmpresasController {

    @FXML
    private Button btnConsultar;

    @FXML
    private Button btnRegistrarCuenta;

    @FXML
    private ComboBox<?> cbFormaDePago;

    @FXML
    private Label lbCantidadEmpleados;

    @FXML
    private Label lbColaboradores;

    @FXML
    private Label lbCuentaBancaria;

    @FXML
    private Label lbFormaDePago;

    @FXML
    private Label lbFormaPago;

    @FXML
    private Label lbNoDeCuenta;

    @FXML
    private Label lbNombreEmpresa;

    @FXML
    private Label lbNombreTab;

    @FXML
    private Label lbRNCEmpresa;

    @FXML
    private Label lbRNCconsultar;

    @FXML
    private Label lbRNombreEmpresa;

    @FXML
    private Label lbTabUsuarioDeseo;

    @FXML
    private TableView<Empleado> tbVwEmpleados;

    @FXML
    private TableColumn<Empleado, String> colCedula;

    @FXML
    private TableColumn<Empleado, String> colCuentaBancaria;

    @FXML
    private TableColumn<Empleado, String> colTipoCuenta;

    @FXML
    private TableColumn<Empleado, Double> colSalario;

    @FXML
    private TextField txtConsultaRNC;

    @FXML
    private TextField txtNoCuenta;

    @FXML
    private TextField txtNombreEmpresa;

    @FXML
    private TextField txtRNC;

    @FXML
    void registrarEmpresa(ActionEvent event) { // Agregar Validaciones
        Empresa nuevaEmpresa = new Empresa();
        nuevaEmpresa.setNombre(txtNombreEmpresa.getText());
        nuevaEmpresa.setRNC(Long.valueOf(txtRNC.getText()));
        nuevaEmpresa.setCuentaBancaria(txtNoCuenta.getText());
        nuevaEmpresa.setFormaPago(cbFormaDePago.getValue().toString());

        EmpresaRepository repository = new EmpresaRepository();
        repository.insertarEmpresa(nuevaEmpresa);
    }

    @FXML
    void consultarEmpresa(ActionEvent event) {
        EmpresaRepository repository = new EmpresaRepository();
        EmpleadoRepository empleadoRepository = new EmpleadoRepository();
        Empresa empresaConsultada = repository.buscarEmpresa(Long.valueOf(txtConsultaRNC.getText()));

        if(Objects.nonNull(empresaConsultada)) {
            List<Empleado> empleadoList = empleadoRepository.listaEmpleado(empresaConsultada.getId());

            lbNombreEmpresa.setVisible(true);
            lbNombreEmpresa.setText("Nombre: " + empresaConsultada.getNombre());

            lbCuentaBancaria.setVisible(true);
            lbCuentaBancaria.setText("Cuenta: " + empresaConsultada.getCuentaBancaria());

            lbFormaPago.setVisible(true);
            lbFormaPago.setText("Forma de Pago: " + empresaConsultada.getFormaPago());

            lbCantidadEmpleados.setVisible(true);
            lbCantidadEmpleados.setText("Cantidad de Empleados: " + empleadoList.size());

            if(empleadoList.isEmpty()) {
                tbVwEmpleados.setAccessibleText("Aun no tienes empleados registrados.");
            }else{
                ObservableList<Empleado> obs = FXCollections.observableArrayList(empleadoList);
                tbVwEmpleados.setItems(obs);
            }
        }
    }


    public void recibirDatosUsuario(String nombre) {
        lbNombreTab.setText("Sr(a). " + nombre);
        lbNombreTab.setVisible(true);
        lbTabUsuarioDeseo.setVisible(true);
    }
}
