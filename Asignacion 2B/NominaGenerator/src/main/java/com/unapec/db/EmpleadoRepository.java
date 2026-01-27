package com.unapec.db;

import com.unapec.entity.Empleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoRepository {

    private static final String SELECT_EMPLEADOS_BY_EMPRESA_ID = "SELECT CEDULA, SALARIO, TIPO_CUENTA, CUENTA_BANCARIA FROM EMPLEADO WHERE EMPRESA_ID = (?)";
    private static final String INSERT_EMPLEADO = "INSERT INTO EMPLEADO (CEDULA, SALARIO, TIPO_CUENTA, CUENTA_BANCARIA, EMPRESA_ID) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_EMPLEADO_BY_CEDULA = "SELECT SALARIO, TIPO_CUENTA, CUENTA_BANCARIA, EMPRESA_ID FROM EMPLEADO WHERE CEDULA = (?) ";

    public int insertEmpleado(Empleado empleado, Long empresaId) {
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_EMPLEADO) ){
            ps.setString(1, empleado.getCedula());
            ps.setDouble(2, empleado.getSalario());
            ps.setString(3, empleado.getTipoDeCuenta());
            ps.setString(4, empleado.getCuentaBancaria());
            ps.setLong(5, empresaId);

            return ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
            return 0;
        }
    }

    public List<Empleado> listaEmpleado(Long empresaId) {
        List<Empleado> empleadosList = new ArrayList<>();
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_EMPLEADOS_BY_EMPRESA_ID)) {
            ps.setLong(1, empresaId);
            try (ResultSet rs2 = ps.executeQuery()) {
                while (rs2.next()) {
                    Empleado empleado = new Empleado();
                    empleado.setCedula(rs2.getString("CEDULA"));
                    empleado.setSalario(rs2.getDouble("SALARIO"));
                    empleado.setTipoDeCuenta(rs2.getString("TIPO_CUENTA"));
                    empleado.setCuentaBancaria(rs2.getString("CUENTA_BANCARIA"));
                    empleadosList.add(empleado);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empleadosList;
    }

    public Empleado consultarEmpleadoPorCedula(String cedula) {
        Empleado empleado = new Empleado();
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_EMPLEADO_BY_CEDULA)) {

            ps.setString(1, cedula);

            try (ResultSet rs2 = ps.executeQuery()) {
                while (rs2.next()) {
                    empleado.setCedula(cedula);
                    empleado.setSalario(rs2.getDouble("SALARIO"));
                    empleado.setTipoDeCuenta(rs2.getString("TIPO_CUENTA"));
                    empleado.setCuentaBancaria(rs2.getString("CUENTA_BANCARIA"));
                    empleado.setEmpresaId(rs2.getLong("EMPRESA_ID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empleado;
    }

}
