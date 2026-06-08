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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.compiere.model.MProcessPara;
import org.compiere.process.ProcessInfoParameter;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.X_I_Attendance;

/**
 * Server process to import attendance records from CSV files located in a specific 
 * server directory. It scans the directory, processes each CSV file found, 
 * populates the intermediate table X_I_Attendance, and moves processed files 
 * to a "processed" subdirectory.
 * 
 * @author Casa del Software
 * @version 1.0
 */
@org.adempiere.base.annotation.Process
public class ImportAttendanceFromFile extends CustomProcess {

    private String ATTENDANCE_FILE_LOCATION = "";

    /**
     * Reads the process parameters.
     * 
     * Parameters:
     * - File_Directory: Absolute path to the directory containing CSV files.
     */
    @Override
    protected void prepare() {
        // Parameter
        ProcessInfoParameter[] para = getParameter();
        for (int i = 0; i < para.length; i++) {
            String name = para[i].getParameterName();
            if (para[i].getParameter() == null && para[i].getParameter_To() == null) {
                continue;
            } else if (name.equals("File_Directory")) {
                ATTENDANCE_FILE_LOCATION = para[i].getParameterAsString();
                // Remove trailing slash or backslash
                if (ATTENDANCE_FILE_LOCATION.endsWith("/") || ATTENDANCE_FILE_LOCATION.endsWith("\\")) {
                    ATTENDANCE_FILE_LOCATION = ATTENDANCE_FILE_LOCATION.replaceFirst("[/\\\\]$", "");
                }
            } else {
                MProcessPara.validateUnknownParameter(getProcessInfo().getAD_Process_ID(), para[i]);
            }
        }
    }

    /**
     * Main execution loop. Scans the configured directory for .csv files and 
     * invokes processFile for each.
     * 
     * @return summary message of the import.
     * @throws Exception if directory access fails.
     */
    @Override
    protected String doIt() throws Exception {
        File directory = new File(ATTENDANCE_FILE_LOCATION);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".csv")) {
                        processFile(file);
                    }
                }
            } else {
                System.out.println("El directorio está vacío.");
            }
        } else {
            System.out.println("La ubicación del archivo no es un directorio válido.");
        }

        return "Datos importados correctamente.";
    }

    /**
     * Parses a single CSV file, reading its lines and creating X_I_Attendance records.
     * After successful processing, the file is moved to the processed directory.
     * 
     * @param file the CSV file to process.
     * @throws Exception if an error occurs during parsing or database persistence.
     */
    private void processFile(File file) throws Exception {
        String line;
        String cvsSplitBy = ",";

        // Usamos try-with-resources para asegurar que el archivo se cierre correctamente
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // Descartar la primera línea (encabezados)
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(cvsSplitBy);

                log.info("Procesando línea: " + line);

                if (fields.length < 16) {
                    String[] paddedFields = new String[16];
                    System.arraycopy(fields, 0, paddedFields, 0, fields.length);
                    for (int i = fields.length; i < 16; i++) {
                        paddedFields[i] = "";
                    }
                    fields = paddedFields;
                }

                // Loguear los campos procesados
                for (int i = 0; i < fields.length; i++) {
                    log.info("Field " + i + ": " + fields[i]);
                }

                X_I_Attendance attendance = new X_I_Attendance(getCtx(), 0, get_TrxName());

                try {
                    attendance.setHR_ClockCode(fields[0].trim());
                    attendance.setFull_Name(fields[1].trim());
                    attendance.setDepartment(fields[2].trim());

                    Timestamp dateMark = parseDate(fields[3].trim(), fields[4].trim());
                    attendance.setDate_Stamp(dateMark);
                    attendance.setTime_Stamp(new Timestamp(dateMark.getTime()));

                    attendance.setAttendance_Status(fields[5].trim());
                    attendance.setDevice_Name(fields[6].trim());
                    attendance.setDevice_SN(fields[7].trim());
                    attendance.setAuth_Method(fields[8].trim());
                    attendance.setTemperature(fields[9].trim());
                    attendance.setSkin_Temperature(fields[10].trim());
                    attendance.setFace_Mask(fields[11].trim());
                    attendance.setAuth_Result(fields[12].trim());
                    attendance.setID_Card(fields[13].trim());
                    attendance.setCard_Reader(fields[14].trim());
                    attendance.setDirection(fields[15].trim());

                    // Guardar el registro
                    if (!attendance.save()) {
                        throw new Exception("No se pudo guardar el registro: " + attendance.toString());
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error procesando línea: " + line, e);
                    throw e; // Si hay un error al guardar, lanzar excepción para manejar rollback
                }
            }

            // Commit de la transacción tras procesar todo el archivo
            
            br.close();
            // Ahora mover el archivo solo si el commit fue exitoso
            moveFileToProcessedDirectory(file);
            
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error leyendo el archivo CSV", e);
            throw new IOException("Error leyendo el archivo CSV", e);
        } catch (Exception e) {
            // Si hay algún error procesando el archivo o guardando datos, hacer rollback
            rollback(); // Asegurar rollback en caso de fallo
            log.log(Level.SEVERE, "Error en el procesamiento", e);
            throw e;
        }
    }

    /**
     * Combines date and time strings into a single Timestamp.
     * 
     * @param dateStr date string in yyyy-MM-dd format.
     * @param timeStr time string in HH:mm:ss format.
     * @return the resulting Timestamp or null if parsing fails.
     */
    private Timestamp parseDate(String dateStr, String timeStr) {
        try {
            String dateTimeStr = dateStr + " " + timeStr;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = dateFormat.parse(dateTimeStr);
            return new Timestamp(parsedDate.getTime());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error parsing date and time", e);
            return null;
        }
    }

    /**
     * Moves the processed file to a "processed" subdirectory within the 
     * source directory.
     * 
     * @param file the file to move.
     * @throws IOException if the move operation fails.
     */
    private void moveFileToProcessedDirectory(File file) throws IOException {
        File processedDir = new File(ATTENDANCE_FILE_LOCATION + "/processed/");
        if (!processedDir.exists()) {
            processedDir.mkdirs();
        }

        // Añadir timestamp al nombre del archivo para hacerlo único
        String newFileName = getUniqueFileName(processedDir, file.getName());
        File processedFile = new File(processedDir, newFileName);

        Files.move(file.toPath(), processedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("Archivo movido al directorio procesado: " + processedFile.getPath());
    }

    /**
     * Generates a unique filename for the processed directory by appending 
     * a timestamp to the original filename.
     * 
     * @param directory destination directory.
     * @param fileName original filename.
     * @return a unique filename string.
     */
    private String getUniqueFileName(File directory, String fileName) {
        String name = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            name = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());

        String newFileName = name + "_" + timestamp + extension;
        File file = new File(directory, newFileName); 
        while (file.exists()) {
            timestamp = sdf.format(new Date());
            newFileName = name + "_" + timestamp + extension;
            file = new File(directory, newFileName);
            
        }
        return newFileName;
    }
}
