package com.cdsoftware.lirion.attendance.process;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.compiere.model.MBPartner;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.MHR_C_BPartnerShifts;
import com.cdsoftware.lirion.attendance.model.X_GH_Shifts_RG_Line;

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
                System.out.println("DateFrom: " + p_DateFrom);
            } else if (name.equals("DateTo")) {
                p_DateTo = para.getParameterAsTimestamp();
                System.out.println("DateTo: " + p_DateTo);
            } else if (name.equals("C_BPartner_ID")) {
                p_C_BPartner_IDs = new int[] {para.getParameterAsInt()};
                System.out.println("C_BPartner_ID: " + p_C_BPartner_IDs[0]);
            } else if (name.equals("GH_Shifts_ID")) {
                p_GH_Shifts_ID = para.getParameterAsInt();
                System.out.println("GH_Shifts_ID: " + p_GH_Shifts_ID);
            } else if (name.equals("HR_Department_ID")) {
                p_HR_Department_ID = para.getParameterAsInt();
                System.out.println("HR_Department_ID: " + p_HR_Department_ID);
            } else if (name.equals("GH_Shifts_RG_ID")) {
                p_GH_Shifts_RG_ID = para.getParameterAsInt();
                System.out.println("GH_Shifts_RG_ID: " + p_GH_Shifts_RG_ID);
            }
        }

        // Obtener los IDs de los trabajadores desde GH_Shifts_RG usando el modelo
        if (p_GH_Shifts_RG_ID > 0) {
            p_C_BPartner_IDs = getBPartnerIDsFromGHShiftsRG();
            System.out.println("Obtenidos " + p_C_BPartner_IDs.length + " C_BPartner_IDs desde GH_Shifts_RG.");
        }
    }

    @Override
    protected String doIt() throws Exception {
        if (p_C_BPartner_IDs == null || p_C_BPartner_IDs.length == 0) {
            System.out.println("No se proporcionó un trabajador válido.");
            return "No se proporcionó trabajador válido.";
        }

        try {
            createBPartnerShiftRecords();
            return "Proceso completado exitosamente.";
        } catch (Exception e) {
            String errorMessage = "Error al ejecutar el proceso: " + e.getMessage();
            System.out.println(errorMessage);
            this.statusUpdate(errorMessage);
            return errorMessage;
        }
    }

    private int[] getBPartnerIDsFromGHShiftsRG() {
        // Usar el modelo correcto X_GH_Shifts_RG_Line
        List<X_GH_Shifts_RG_Line> shiftsRGList = new Query(Env.getCtx(), X_GH_Shifts_RG_Line.Table_Name, "GH_Shifts_RG_ID = ?", get_TrxName())
                .setParameters(p_GH_Shifts_RG_ID)
                .list();

        // Utilizar un conjunto (Set) para almacenar los IDs de los trabajadores y eliminar duplicados automáticamente
        Set<Integer> uniqueBPartnerIDs = new HashSet<>();
        
        for (X_GH_Shifts_RG_Line shift : shiftsRGList) {
            uniqueBPartnerIDs.add(shift.getC_BPartner_ID()); // Añadir solo IDs únicos
        }

        // Convertir el conjunto (Set) a un array de enteros
        return uniqueBPartnerIDs.stream().mapToInt(Integer::intValue).toArray();
    }

    private void createBPartnerShiftRecords() {
        int totalRecords = p_C_BPartner_IDs.length;
        boolean hasError = false;

        for (int i = 0; i < totalRecords; i++) {
            int cBPartnerID = p_C_BPartner_IDs[i];
            String workerName = getBPartnerName(cBPartnerID);

            // Verificar si el trabajador ya tiene un turno en el mismo rango de fechas
            if (isBPartnerAssignedInDateRange(cBPartnerID, p_DateFrom, p_DateTo, p_GH_Shifts_ID)) {
                System.out.println("El trabajador '" + workerName + "' ya tiene un turno en este rango de fechas.");
                addLog(0, p_DateFrom, null, "El trabajador '" + workerName + "' ya tiene un turno en este rango de fechas.");
                hasError = true;
            } else {
                MHR_C_BPartnerShifts bpartnerShifts = new MHR_C_BPartnerShifts(Env.getCtx(), 0, get_TrxName());
                bpartnerShifts.setC_BPartner_ID(cBPartnerID);
                bpartnerShifts.setDateFrom(p_DateFrom);
                bpartnerShifts.setDateTo(p_DateTo);
                bpartnerShifts.setGH_Shifts_ID(p_GH_Shifts_ID);
                bpartnerShifts.saveEx();
                System.out.println("Registro creado para trabajador: " + workerName);

                String statusMessage = "Procesando registro " + (i + 1) + " de " + totalRecords;
                System.out.println(statusMessage);
                this.statusUpdate(statusMessage);
            }
        }

        if (!hasError) {
            this.statusUpdate("Proceso completado exitosamente. Todos los registros fueron procesados.");
            System.out.println("Proceso completado exitosamente.");
        }
    }

    private String getBPartnerName(int cBPartnerID) {
        // Utilizar el modelo MBPartner para obtener el nombre del trabajador
        MBPartner partner = new MBPartner(Env.getCtx(), cBPartnerID, get_TrxName());
        if (partner != null) {
            return partner.getName();
        }
        return "Unknown Worker";
    }

    private boolean isBPartnerAssignedInDateRange(int cBPartnerID, Timestamp dateFrom, Timestamp dateTo, int ghShiftsID) {
        // Verificar utilizando el modelo MHR_C_BPartnerShifts si existe un turno asignado
        String whereClause = "C_BPartner_ID = ? AND DateFrom <= ? AND DateTo >= ?";
        List<MHR_C_BPartnerShifts> shiftsList;

        if (ghShiftsID > 0) {
            whereClause += " AND GH_Shifts_ID = ?";
            shiftsList = new Query(Env.getCtx(), MHR_C_BPartnerShifts.Table_Name, whereClause, get_TrxName())
                    .setParameters(cBPartnerID, dateFrom, dateTo, ghShiftsID)
                    .list();
        } else {
            shiftsList = new Query(Env.getCtx(), MHR_C_BPartnerShifts.Table_Name, whereClause, get_TrxName())
                    .setParameters(cBPartnerID, dateFrom, dateTo)
                    .list();
        }

        return !shiftsList.isEmpty();  // Si la lista no está vacía, ya tiene un turno asignado
    }
}
