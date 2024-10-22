package com.cdsoftware.lirion.attendance.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.MHR_C_BPartnerShifts;

@org.adempiere.base.annotation.Process
public class Create_C_BPartner_Shift extends CustomProcess {
    private Timestamp p_DateFrom;
    private Timestamp p_DateTo;
    private int[] p_C_BPartner_IDs;
    private int p_GH_Shifts_ID;
    private int p_HR_Department_ID;
    private int p_GH_Shifts_RG_ID;

    @Override
    protected void prepare() {
        ProcessInfoParameter[] parameters = getParameter();

        for (ProcessInfoParameter para : parameters) {
            String name = para.getParameterName();
            if (para.getParameter() == null)
                continue;

            if (name.equals("DateFrom")) {
                p_DateFrom = para.getParameterAsTimestamp();
                System.out.println("DateFrom: " + p_DateFrom);  // Mensaje de depuración
            } else if (name.equals("DateTo")) {
                p_DateTo = para.getParameterAsTimestamp();
                System.out.println("DateTo: " + p_DateTo);  // Mensaje de depuración
            } else if (name.equals("C_BPartner_ID")) {
                // Almacenamos como una lista para manejar múltiples IDs
                p_C_BPartner_IDs = new int[] {para.getParameterAsInt()};
                System.out.println("C_BPartner_ID: " + p_C_BPartner_IDs[0]);  // Mensaje de depuración
            } else if (name.equals("GH_Shifts_ID")) {
                p_GH_Shifts_ID = para.getParameterAsInt();
                System.out.println("GH_Shifts_ID: " + p_GH_Shifts_ID);  // Mensaje de depuración
            } else if (name.equals("HR_Department_ID")) {
                p_HR_Department_ID = para.getParameterAsInt();
                System.out.println("HR_Department_ID: " + p_HR_Department_ID);  // Mensaje de depuración
            } else if (name.equals("GH_Shifts_RG_ID")) {
                p_GH_Shifts_RG_ID = para.getParameterAsInt();
                System.out.println("GH_Shifts_RG_ID: " + p_GH_Shifts_RG_ID);  // Mensaje de depuración
            }
        }

        // Si se proporciona GH_Shifts_RG_ID, obtener la lista de C_BPartner_ID desde GH_Shifts_RG
        if (p_GH_Shifts_RG_ID > 0) {
            p_C_BPartner_IDs = getBPartnerIDsFromGHShiftsRG();
            System.out.println("Obtenidos " + p_C_BPartner_IDs.length + " C_BPartner_IDs desde GH_Shifts_RG.");  // Mensaje de depuración
        }
    }

    @Override
    protected String doIt() throws Exception {
        if (p_C_BPartner_IDs == null || p_C_BPartner_IDs.length == 0) {
            System.out.println("No se proporcionó un trabajador válido.");  // Mensaje de depuración
            return "No se proporcionó trabajador válido.";
        }

        try {
            createBPartnerShiftRecords();
            return "Proceso completado exitosamente.";
        } catch (Exception e) {
            String errorMessage = "Error al ejecutar el proceso: " + e.getMessage();
            System.out.println(errorMessage);  // Mensaje de depuración en caso de error
            this.statusUpdate(errorMessage);
            return errorMessage;
        }
    }

    private int[] getBPartnerIDsFromGHShiftsRG() {
        List<Integer> bpartnerIDList = new ArrayList<>();
        String sql = "SELECT DISTINCT C_BPartner_ID FROM GH_Shifts_RG_Line WHERE GH_Shifts_RG_ID = ?";

        try (PreparedStatement pstmt = DB.prepareStatement(sql, get_TrxName())) {
            pstmt.setInt(1, p_GH_Shifts_RG_ID);
            System.out.println("Ejecutando consulta para obtener C_BPartner_IDs desde GH_Shifts_RG_Line.");  // Mensaje de depuración
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int cBPartnerID = rs.getInt("C_BPartner_ID");
                    System.out.println("Encontrado C_BPartner_ID: " + cBPartnerID);  // Mensaje de depuración
                    bpartnerIDList.add(cBPartnerID);
                }
            }
        } catch (SQLException e) {
            String error = "Error al obtener trabajador desde GH_Shifts_RG. SQL: " + sql +
                           ", GH_Shifts_RG_ID: " + p_GH_Shifts_RG_ID;
            System.out.println(error);  // Mensaje de depuración
            throw new RuntimeException(error, e);
        }

        // Convertir la lista a un array de enteros
        return bpartnerIDList.stream().mapToInt(i -> i).toArray();
    }

    private void createBPartnerShiftRecords() {
        int totalRecords = p_C_BPartner_IDs.length;
        boolean hasError = false;

        for (int i = 0; i < totalRecords; i++) {
            int cBPartnerID = p_C_BPartner_IDs[i];
            String workerName = getBPartnerName(cBPartnerID);

            // Verificar si el trabajador ya tiene un turno en el mismo rango de fechas
            if (isBPartnerAssignedInDateRange(cBPartnerID, p_DateFrom, p_DateTo, p_GH_Shifts_ID)) {
                System.out.println("El trabajador '" + workerName + "' ya tiene un turno en este rango de fechas.");  // Mensaje de depuración
                addLog(0, p_DateFrom, null, "El trabajador '" + workerName + "' ya tiene un turno en este rango de fechas.");
                hasError = true;
            } else {
                MHR_C_BPartnerShifts bpartnerShifts = new MHR_C_BPartnerShifts(Env.getCtx(), 0, get_TrxName());
                bpartnerShifts.setC_BPartner_ID(cBPartnerID);
                bpartnerShifts.setDateFrom(p_DateFrom);
                bpartnerShifts.setDateTo(p_DateTo);
                bpartnerShifts.setGH_Shifts_ID(p_GH_Shifts_ID);
                bpartnerShifts.saveEx();
                System.out.println("Registro creado para trabajador: " + workerName);  // Mensaje de depuración

                String statusMessage = "Procesando registro " + (i + 1) + " de " + totalRecords;
                System.out.println(statusMessage);  // Mensaje de depuración
                this.statusUpdate(statusMessage);
            }
        }

        if (!hasError) {
            this.statusUpdate("Proceso completado exitosamente. Todos los registros fueron procesados.");
            System.out.println("Proceso completado exitosamente.");  // Mensaje de depuración
        }
    }

    private String getBPartnerName(int cBPartnerID) {
        String sql = "SELECT Name FROM C_BPartner WHERE C_BPartner_ID = ?";
    
        try (PreparedStatement pstmt = DB.prepareStatement(sql, get_TrxName())) {
            pstmt.setInt(1, cBPartnerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo nombre del trabajador para C_BPartner_ID: " + cBPartnerID);  // Mensaje de depuración
            throw new RuntimeException("Error obteniendo nombre del trabajador para C_BPartner_ID: " + cBPartnerID, e);
        }

        return "Unknown Worker";
    }

    private boolean isBPartnerAssignedInDateRange(int cBPartnerID, Timestamp dateFrom, Timestamp dateTo, int ghShiftsID) {
        String sql = "SELECT COUNT(*) FROM HR_C_BPartnerShifts WHERE C_BPartner_ID = ? AND DateFrom <= ? AND DateTo >= ?";
        if (ghShiftsID > 0) {
            sql += " AND GH_Shifts_ID = ?";
            return DB.getSQLValueEx(get_TrxName(), sql, cBPartnerID, dateFrom, dateTo, ghShiftsID) > 0;
        } else {
            return DB.getSQLValueEx(get_TrxName(), sql, cBPartnerID, dateFrom, dateTo) > 0;
        }
    }
}
