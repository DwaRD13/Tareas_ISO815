package com.unapec.db;

import com.unapec.entity.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpresaRepository {

    private static String CREATE_EMPRESA_QUERY = "INSERT INTO EMPRESA (NOMBRE, RNC, CUENTA_BANCARIA, FORMA_PAGO) VALUES (?, ?, ?, ?)";
    private static String CONSULTAR_EMPRESA_QUERY = "SELECT ID, NOMBRE, RNC, CUENTA_BANCARIA, FORMA_PAGO FROM EMPRESA WHERE RNC = ?";
    private static String CONSULTAR_EMPRESA_POR_ID_QUERY = "SELECT NOMBRE, RNC, CUENTA_BANCARIA, FORMA_PAGO FROM EMPRESA WHERE ID = ?";

    public int insertarEmpresa(Empresa empresa) {
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(CREATE_EMPRESA_QUERY)) {
            ps.setString(1, empresa.getNombre());
            ps.setLong(2, empresa.getRNC());
            ps.setString(3, empresa.getCuentaBancaria());
            ps.setString(4, empresa.getFormaPago());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Empresa buscarEmpresa(String RNC) {
        Empresa empresa = new Empresa();
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(CONSULTAR_EMPRESA_QUERY)) {
            ps.setLong(1, Long.parseLong(RNC));

            try (ResultSet rs2 = ps.executeQuery()) {
                while (rs2.next()) {
                    empresa.setId(rs2.getLong("ID"));
                    empresa.setNombre(rs2.getString("NOMBRE"));
                    empresa.setRNC(rs2.getLong("RNC"));
                    empresa.setCuentaBancaria(rs2.getString("CUENTA_BANCARIA"));
                    empresa.setFormaPago(rs2.getString("FORMA_PAGO"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empresa;
    }

    public Empresa buscarEmpresaPorID(Long id) {
        Empresa empresa = new Empresa();
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(CONSULTAR_EMPRESA_POR_ID_QUERY)) {
            ps.setLong(1, id);

            try (ResultSet rs2 = ps.executeQuery()) {
                while (rs2.next()) {
                    empresa.setNombre(rs2.getString("NOMBRE"));
                    empresa.setRNC(rs2.getLong("RNC"));
                    empresa.setCuentaBancaria(rs2.getString("CUENTA_BANCARIA"));
                    empresa.setFormaPago(rs2.getString("FORMA_PAGO"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empresa;
    }
}