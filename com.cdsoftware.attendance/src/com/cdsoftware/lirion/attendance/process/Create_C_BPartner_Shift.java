/**********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Casa del Software                                                 *
 **********************************************************************/
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

/**
 * Server process to assign work shifts to Business Partners within a specific date range.
 * This process can take a single Business Partner or a group of them from a 
 * Shift Group (GH_Shifts_RG). It creates records in MHR_C_BPartnerShifts while 
 * preventing duplicate assignments for overlapping date ranges.
 * 
 * @author Casa del Software
 */
@org.adempiere.base.annotation.Process
public class Create_C_BPartner_Shift extends CustomProcess {
    private Timestamp p_DateFrom;
    private Timestamp p_DateTo;
    private int[] p_C_BPartner_IDs;
    private int p_GH_Shifts_ID;
    private int p_HR_Department_ID;
    private int p_GH_Shifts_RG_ID;

    /**
     * Reads the process parameters required for shift assignment and 
     * identifies the target Business Partners (either single or from a group).
     * 
     * Parameters:
     * - DateFrom: Start date of the shift assignment.
     * - DateTo: End date of the shift assignment.
     * - C_BPartner_ID: Specific Business Partner to assign (if not using a group).
     * - GH_Shifts_ID: The work shift (MGH_Shifts) to assign.
     * - HR_Department_ID: Optional department filter (not directly used for selection).
     * - GH_Shifts_RG_ID: Shift Group ID to process multiple Business Partners at once.
     */
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

        // Get worker IDs from GH_Shifts_RG using the model
        if (p_GH_Shifts_RG_ID > 0) {
            p_C_BPartner_IDs = getBPartnerIDsFromGHShiftsRG();
            System.out.println("Obtained " + p_C_BPartner_IDs.length + " C_BPartner_IDs from GH_Shifts_RG.");
        }
    }

    /**
     * Executes the shift assignment logic for the identified Business Partners.
     * It validates that a list of IDs exists and triggers the record creation.
     * 
     * @return Result message indicating success or failure.
     * @throws Exception if an error occurs during processing.
     */
    @Override
    protected String doIt() throws Exception {
        if (p_C_BPartner_IDs == null || p_C_BPartner_IDs.length == 0) {
            System.out.println("No valid worker was provided.");
            return "No valid worker was provided.";
        }

        try {
            createBPartnerShiftRecords();
            return "Process completed successfully.";
        } catch (Exception e) {
            String errorMessage = "Error executing the process: " + e.getMessage();
            System.out.println(errorMessage);
            this.statusUpdate(errorMessage);
            return errorMessage;
        }
    }

    /**
     * Retrieves a unique list of Business Partner IDs from a Shift Group.
     * 
     * @return Array of unique Business Partner IDs.
     */
    private int[] getBPartnerIDsFromGH_Shifts_RG() {
        // Use the correct model X_GH_Shifts_RG_Line
        List<X_GH_Shifts_RG_Line> shiftsRGList = new Query(Env.getCtx(), X_GH_Shifts_RG_Line.Table_Name, "GH_Shifts_RG_ID = ?", get_TrxName())
                .setParameters(p_GH_Shifts_RG_ID)
                .list();

        // Use a Set to store worker IDs and automatically remove duplicates
        Set<Integer> uniqueBPartnerIDs = new HashSet<>();
        
        for (X_GH_Shifts_RG_Line shift : shiftsRGList) {
            uniqueBPartnerIDs.add(shift.getC_BPartner_ID()); // Add unique IDs only
        }

        // Convert the Set to an integer array
        return uniqueBPartnerIDs.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Iterates over the list of Business Partners and creates shift assignment records
     * if they don't already have one in the specified date range.
     */
    private void createBPartnerShiftRecords() {
        int totalRecords = p_C_BPartner_IDs.length;
        boolean hasError = false;

        for (int i = 0; i < totalRecords; i++) {
            int cBPartnerID = p_C_BPartner_IDs[i];
            String workerName = getBPartnerName(cBPartnerID);

            // Check if the worker already has a shift in the same date range
            if (isBPartnerAssignedInDateRange(cBPartnerID, p_DateFrom, p_DateTo, p_GH_Shifts_ID)) {
                System.out.println("The worker '" + workerName + "' already has a shift in this date range.");
                addLog(0, p_DateFrom, null, "The worker '" + workerName + "' already has a shift in this date range.");
                hasError = true;
            } else {
                MHR_C_BPartnerShifts bpartnerShifts = new MHR_C_BPartnerShifts(Env.getCtx(), 0, get_TrxName());
                bpartnerShifts.setC_BPartner_ID(cBPartnerID);
                bpartnerShifts.setDateFrom(p_DateFrom);
                bpartnerShifts.setDateTo(p_DateTo);
                bpartnerShifts.setGH_Shifts_ID(p_GH_Shifts_ID);
                bpartnerShifts.saveEx();
                System.out.println("Record created for worker: " + workerName);

                String statusMessage = "Processing record " + (i + 1) + " of " + totalRecords;
                System.out.println(statusMessage);
                this.statusUpdate(statusMessage);
            }
        }

        if (!hasError) {
            this.statusUpdate("Process completed successfully. All records were processed.");
            System.out.println("Process completed successfully.");
        }
    }

    /**
     * Gets the name of a Business Partner by its ID.
     * 
     * @param cBPartnerID ID of the Business Partner.
     * @return Name of the Business Partner or "Unknown Worker".
     */
    private String getBPartnerName(int cBPartnerID) {
        // Use MBPartner model to get worker name
        MBPartner partner = new MBPartner(Env.getCtx(), cBPartnerID, get_TrxName());
        if (partner != null) {
            return partner.getName();
        }
        return "Unknown Worker";
    }

    /**
     * Checks if a Business Partner already has a shift assigned in the given date range.
     * 
     * @param cBPartnerID ID of the Business Partner.
     * @param dateFrom Start date of the range.
     * @param dateTo End date of the range.
     * @param ghShiftsID ID of the shift to check (if 0, checks for any shift).
     * @return true if already assigned, false otherwise.
     */
    private boolean isBPartnerAssignedInDateRange(int cBPartnerID, Timestamp dateFrom, Timestamp dateTo, int ghShiftsID) {
        // Check if a shift is already assigned using the MHR_C_BPartnerShifts model
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
