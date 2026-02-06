package com.fundapec.payment.management.db;

import com.fundapec.payment.management.entity.DetallePago;
import com.fundapec.payment.management.entity.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

public class PagoRepository {

    private static final String INSERT_ESTUDIANTE_SQL =
            "INSERT INTO ESTUDIANTE (MATRICULA, ESTADO, PERIODO_ACADEMICO, DESCRIPCION_PERIODO) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "ESTADO = VALUES(ESTADO), " +
                    "PERIODO_ACADEMICO = VALUES(PERIODO_ACADEMICO), " +
                    "DESCRIPCION_PERIODO = VALUES(DESCRIPCION_PERIODO)";

    private static final String INSERT_PAGO_SQL =
            "INSERT INTO DETALLE_PAGO (CODIGO_PAGO, MONTO_APROBADO, MONEDA, FECHA_APROBACION, MATRICULA) " +
                    "VALUES (?, ?, ?, ?, ?)";

    public void save(Estudiante estudiante, DetallePago detallePago) throws SQLException {
        Connection conn = null;
        PreparedStatement psEstudiante = null;
        PreparedStatement psPago = null;

        try {
            conn = ConnectionDB.getConnection();

            conn.setAutoCommit(false);

            psEstudiante = conn.prepareStatement(INSERT_ESTUDIANTE_SQL);
            psEstudiante.setString(1, estudiante.getMatricula());
            psEstudiante.setString(2, estudiante.getEstado());
            psEstudiante.setString(3, estudiante.getPeriodoAcademico());
            psEstudiante.setString(4, estudiante.getDescripcionPeriodo());
            psEstudiante.executeUpdate();

            psPago = conn.prepareStatement(INSERT_PAGO_SQL);
            psPago.setString(1, detallePago.getCodigoPago());
            psPago.setDouble(2, detallePago.getMontoAprobado());
            psPago.setString(3, detallePago.getMoneda());

            LocalDate fecha = detallePago.getFechaAprobacion();
            psPago.setDate(4, java.sql.Date.valueOf(fecha));

            psPago.setString(5, estudiante.getMatricula());

            psPago.executeUpdate();

            conn.commit();
            System.out.println("Transacción guardada exitosamente.");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.println("Error en transacción, haciendo Rollback: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (psEstudiante != null) psEstudiante.close();
            if (psPago != null) psPago.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
