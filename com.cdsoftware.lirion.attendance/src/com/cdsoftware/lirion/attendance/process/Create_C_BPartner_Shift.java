package com.cdsoftware.lirion.attendance.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.MHR_C_BPartnerShifts;

@org.adempiere.base.annotation.Process
public class Create_C_BPartner_Shift extends CustomProcess {
    private Timestamp p_DateFrom;
    private Timestamp p_DateTo;
    private String p_C_BPartner_ID;
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
            } else if (name.equals("DateTo")) {
                p_DateTo = para.getParameterAsTimestamp();
            } else if (name.equals("C_BPartner_ID")) {
                p_C_BPartner_ID = para.getParameterAsString();
            } else if (name.equals("GH_Shifts_ID")) {
                p_GH_Shifts_ID = para.getParameterAsInt();
            } else if (name.equals("HR_Department_ID")) {
                p_HR_Department_ID = para.getParameterAsInt();
            } else if (name.equals("GH_Shifts_RG_ID")) {
                p_GH_Shifts_RG_ID = para.getParameterAsInt();
            }
        }

        // Si se proporciona GH_Shifts_RG_ID, obtener la lista de C_BPartner_ID desde GH_Shifts_RG
        if (p_GH_Shifts_RG_ID > 0) {
            p_C_BPartner_ID = getBPartnerIDsFromGHShiftsRG();
        }
    }

    @Override
    protected String doIt() throws Exception {
        // Check Si llega c_BPArtner
        if (p_C_BPartner_ID == null || p_C_BPartner_ID.isEmpty()) {
            return "No se proporcionó trabajador válido.";
        }

        try {
            createBPartnerShiftRecords();
            return "Proceso completado exitosamente.";
        } catch (Exception e) {
            // Verifica errores y devuelve un mensaje en caso de que los de
            String errorMessage = "Error al ejecutar el proceso: " + e.getMessage();
            this.statusUpdate(errorMessage); // Actualización de estado en caso de error
            return errorMessage;
        }
    }

    private String getBPartnerIDsFromGHShiftsRG() {
        // Crea un StringBuilder para construir la lista de C_BPartner_ID
        StringBuilder bpartnerIDs = new StringBuilder();

        // Realiza la consulta a GH_Shifts_RG 
        String sql = "SELECT DISTINCT C_BPartner_ID FROM GH_Shifts_RG_Line WHERE GH_Shifts_RG_ID = ?";
        
        try (PreparedStatement pstmt = DB.prepareStatement(sql, get_TrxName())) {
            pstmt.setInt(1, p_GH_Shifts_RG_ID);  // Establecer el valor del parámetro
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int cBPartnerID = rs.getInt("C_BPartner_ID");
                    String workerName = getBPartnerName(cBPartnerID);

                    if (bpartnerIDs.length() > 0) {
                        bpartnerIDs.append(",");
                    }
                    bpartnerIDs.append(workerName);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener trabajador desde GH_Shifts_RG. SQL: " + sql +
                                       ", GH_Shifts_RG_ID: " + p_GH_Shifts_RG_ID, e);
        }

        return bpartnerIDs.toString();
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
            throw new RuntimeException("Error getting worker name for C_BPartner_ID: " + cBPartnerID, e);
        }

        return "Unknown Worker"; // Default value if the name is not found
    }

    private void createBPartnerShiftRecords() {
        String[] bpartnerIDs = p_C_BPartner_ID.split(",");
        int totalRecords = bpartnerIDs.length;
        boolean hasError = false;

        for (int i = 0; i < totalRecords; i++) {
            int cBPartnerID = Integer.parseInt(bpartnerIDs[i]);
            String workerName = getBPartnerName(cBPartnerID);

            // Verificar si el trabajador ya tiene un turno en el mismo rango de fechas
            if (isBPartnerAssignedInDateRange(cBPartnerID, p_DateFrom, p_DateTo, p_GH_Shifts_ID)) {
                // Agregar un mensaje al registro de procesos
                addLog(0, p_DateFrom, null, "El trabajador '" + workerName + "' ya tiene un turno en este rango de fechas.");
                hasError = true;
            } else {
                // Resto del código para crear el registro en HR_C_BPartnerShifts
                MHR_C_BPartnerShifts bpartnerShifts = new MHR_C_BPartnerShifts(Env.getCtx(), 0, get_TrxName());
                bpartnerShifts.setC_BPartner_ID(cBPartnerID);
                bpartnerShifts.setDateFrom(p_DateFrom);
                bpartnerShifts.setDateTo(p_DateTo);
                bpartnerShifts.setGH_Shifts_ID(p_GH_Shifts_ID);
                bpartnerShifts.saveEx();

                String statusMessage = "Procesando registro " + (i + 1) + " de " + totalRecords;
                this.statusUpdate(statusMessage);
            }
        }

        // Actualizar el mensaje de éxito si no hay errores
        this.statusUpdate("Proceso completado exitosamente. Todos los registros de los trabajadores fueron procesados correctamente.");
    }

    private boolean isBPartnerAssignedInDateRange(int cBPartnerID, Timestamp dateFrom, Timestamp dateTo, int ghShiftsID) {
        // Consulta para verificar si el trabajador ya tiene un turno en el mismo rango de fechas
        
        String sql = "SELECT COUNT(*) FROM HR_C_BPartnerShifts WHERE C_BPartner_ID = ? AND DateFrom <= ? AND DateTo >= ?";
        
        // Si GH_Shifts_ID se proporciona, incluirlo en la condición
        if (ghShiftsID > 0) {
            sql += " AND GH_Shifts_ID = ?";
            // Ejecutar la consulta con los cuatro parámetros
            int count = DB.getSQLValueEx(get_TrxName(), sql, cBPartnerID, dateFrom, dateTo, ghShiftsID);
            return count > 0;
        } else {
            // Ejecutar la consulta con los tres primeros parámetros
            int count = DB.getSQLValueEx(get_TrxName(), sql, cBPartnerID, dateFrom, dateTo);
            return count > 0;
        }
    }
}
