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


}
