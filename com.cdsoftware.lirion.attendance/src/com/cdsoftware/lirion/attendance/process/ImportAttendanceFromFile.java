package com.cdsoftware.lirion.attendance.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.compiere.model.MProcessPara;
import org.compiere.process.ProcessInfoParameter;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.X_I_Attendance;


@org.adempiere.base.annotation.Process
public class ImportAttendanceFromFile extends CustomProcess {

    private String ATTENDANCE_FILE_LOCATION = "";

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

    private void processFile(File file) throws IOException {
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // Discard the first line (headers)
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(cvsSplitBy);

                log.info("Procesando linea: " + line);

                if (fields.length < 16) {
                    String[] paddedFields = new String[16];
                    System.arraycopy(fields, 0, paddedFields, 0, fields.length);
                    for (int i = fields.length; i < 16; i++) {
                        paddedFields[i] = "";
                    }
                    fields = paddedFields;
                }

                // Log the fields being processed
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

                    // Save the record
                    if (!attendance.save()) {
                        throw new Exception("No se Pudo guardar el registro: " + attendance.toString());
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error procesando linea: " + line, e);
                }
            }

            // Move the file to the processed directory
            moveFileToProcessedDirectory(file);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error leyendo el archivo CSV ", e);
            throw new IOException("Error leyendo el archivo CSV ", e);
        }
    }

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

    private void moveFileToProcessedDirectory(File file) throws IOException {
        File processedDir = new File(ATTENDANCE_FILE_LOCATION + "/processed/");
        if (!processedDir.exists()) {
            processedDir.mkdirs();
        }

        // Add timestamp to the file name to make it unique
        String newFileName = getUniqueFileName(processedDir, file.getName());
        File processedFile = new File(processedDir, newFileName);

        Files.move(file.toPath(), processedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("File moved to processed directory: " + processedFile.getPath());
    }

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
