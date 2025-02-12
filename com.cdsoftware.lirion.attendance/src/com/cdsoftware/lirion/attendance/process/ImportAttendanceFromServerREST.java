package com.cdsoftware.lirion.attendance.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

import org.adempiere.base.annotation.Process;
import org.compiere.model.MSysConfig;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cdsoftware.lirion.attendance.model.X_HR_AttendanceDevices;
import com.cdsoftware.lirion.attendance.model.X_I_Attendance;
import com.cdsoftware.lirion.payroll.model.MHRProcess;

/**
 * Proceso para importar marcaciones desde la tabla I_Attendance de un servidor central 
 * a la tabla I_Attendance de un servidor local.
 * La dirección del servidor Remoto esta indicada por la variable de sistema CDS_AT_BASE_URL
 * 
 * 
 * 
 * 
 * @author Carlo
 *
 */

@org.adempiere.base.annotation.Process
public class ImportAttendanceFromServerREST extends SvrProcess {
    private String DEVICE_SN;
    private String BASE_URL;
    private String p_BASE_URL;
    private static final String DEFAULT_DEVICE_SN = "0000000000366";
    private static final String DEFAULT_BASE_URL = "https://desarrollo.casadelsoftware.com:4988";
    private String token;
    private int p_Attendance_Device = 0;
   
    @Override
    protected void prepare() {
    	ProcessInfoParameter[] para = getParameter();
    	for (int i = 0; i < para.length; i++)
		{
			if (log.isLoggable(Level.FINE)) log.fine("prepare - " + para[i]);
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null && para[i].getParameter_To() == null)
				;
			else if (name.equals("HR_AttendanceDevices_ID"))
				p_Attendance_Device = para[i].getParameterAsInt();
			System.out.println("Obtained Parameter: " + p_Attendance_Device);
		}
        p_BASE_URL = MSysConfig.getValue("CDS_AT_BASE_URL", DEFAULT_BASE_URL, getAD_Client_ID());
        
        if (p_BASE_URL == null || p_BASE_URL.isEmpty()) {
        	p_BASE_URL = DEFAULT_BASE_URL ;
        }
        if (p_BASE_URL.endsWith("/") || p_BASE_URL.endsWith("\\")) {
        	p_BASE_URL = p_BASE_URL.replaceFirst("[/\\\\]$", "");
        }
        BASE_URL = p_BASE_URL + "/api/v1";
    }

    @Override
    protected String doIt() throws Exception {
        // Borrar todos los datos existentes en la tabla i_attendance
        //clearAttendanceTable();
    	// Obtener el DEVICE_SN
    	if (p_Attendance_Device > 0) {
    	    X_HR_AttendanceDevices attendanceDevice = new X_HR_AttendanceDevices(Env.getCtx(), p_Attendance_Device, get_TrxName());
    	    DEVICE_SN = attendanceDevice.getDevice_SN();
    	    System.out.println("Obtained DEVICE_SN: " + DEVICE_SN);
    	} else {
    	    DEVICE_SN = DEFAULT_DEVICE_SN;
    	    System.out.println("Using default DEVICE_SN: " + DEVICE_SN);
    	}
        // Autenticación
        authenticate();

        // Leer datos desde el servidor remoto
        JSONArray remoteData = queryRemoteAttendanceData(DEVICE_SN);

        if (remoteData != null) {
            // Guardar datos en la tabla i_attendance local
            processAndSaveData(remoteData);
        }

        return "Proceso completado satisfactoriamente.";
    }

    private void clearAttendanceTable() {
        String sql = "DELETE FROM i_attendance";
        DB.executeUpdate(sql, get_TrxName());
    }

    private void authenticate() throws Exception {
        String authUrl = BASE_URL + "/auth/tokens";
        HttpURLConnection connection = (HttpURLConnection) new URL(authUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject authRequest = new JSONObject();
        authRequest.put("userName", "SuperUser");
        authRequest.put("password", "uZHNCpaXYZ76Md8");

        JSONObject parameters = new JSONObject();
        parameters.put("clientId", 1000001);
        parameters.put("roleId", 1000003);
        parameters.put("organizationId", 0);
        parameters.put("warehouseId", 0);
        parameters.put("language", "en_US");

        authRequest.put("parameters", parameters);

        OutputStream os = connection.getOutputStream();
        os.write(authRequest.toString().getBytes());
        os.flush();

        int responseCode = connection.getResponseCode();
        System.out.println("Auth response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            JSONObject responseJson = new JSONObject(response.toString());
            token = responseJson.getString("token");
            System.out.println("Token: " + token);
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            System.err.println("Authentication failed: " + response.toString());
            throw new Exception("Authentication failed");
        }
    }

    private JSONArray queryRemoteAttendanceData(String deviceSn) {
        try {
        	String url = BASE_URL + "/models/I_Attendance?$filter=Device_SN+eq+'" + DEVICE_SN + "'";
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("Authorization", "Bearer " + token);

            // Agregar más mensajes de depuración
            System.out.println("Request URL: " + url);
            System.out.println("Authorization Header: Bearer " + token);

            int responseCode = httpConnection.getResponseCode();
            System.out.println("Query response code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                httpConnection.disconnect();

                String jsonResponse = response.toString();
                System.out.println("Response from server: " + jsonResponse); // Imprimir respuesta completa del servidor

                // Parsear el JSONObject y obtener el JSONArray de "records"
                JSONObject responseObject = new JSONObject(jsonResponse);
                if (responseObject.has("records")) {
                    return responseObject.getJSONArray("records");
                } else {
                    System.err.println("No 'records' field found in the response");
                    return null;
                }
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                System.err.println("Error querying remote attendance data: " + response.toString());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error querying remote attendance data");
            e.printStackTrace();
            return null;
        }
    }

    private void processAndSaveData(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                saveAttendanceData(jsonObject);
            }
        } catch (Exception e) {
            System.err.println("Error processing and saving data");
            e.printStackTrace();
        }
    }

    private void saveAttendanceData(JSONObject jsonObject) {
        try {
            System.out.println("Processing JSONObject: " + jsonObject.toString()); // Depurar contenido de JSONObject
            X_I_Attendance attendance = new X_I_Attendance(Env.getCtx(), 0, get_TrxName());

            // Comprobación de campos presentes
            attendance.setHR_ClockCode(jsonObject.optString("HR_ClockCode", null));
            attendance.setFull_Name(jsonObject.optString("Full_Name", null));
            attendance.setDepartment(jsonObject.optString("Department", null));
            attendance.setDate_Stamp(parseTimestamp(jsonObject.optString("Date_Stamp", null)));
            attendance.setTime_Stamp(parseTimestamp(jsonObject.optString("Time_Stamp", null)));
            attendance.setAttendance_Status(jsonObject.optString("Attendance_Status", null));
            attendance.setDevice_Name(jsonObject.optString("Device_Name", null));
            attendance.setDevice_SN(jsonObject.optString("Device_SN", null));
            attendance.setAuth_Method(jsonObject.optString("Auth_Method", null));
            attendance.setTemperature(jsonObject.optString("Temperature", null));
            attendance.setSkin_Temperature(jsonObject.optString("Skin_Temperature", null));
            attendance.setFace_Mask(jsonObject.optString("Face_Mask", null));
            attendance.setAuth_Result(jsonObject.optString("Auth_Result", null));
            attendance.setID_Card(jsonObject.optString("ID_Card", null));
            attendance.setCard_Reader(jsonObject.optString("Card_Reader", null));
            attendance.setDirection(jsonObject.optString("Direction", null));
            attendance.setI_Attendance_UU(jsonObject.optString("uid", null));

            attendance.saveEx();
        } catch (Exception e) {
            System.err.println("Error saving attendance data");
            e.printStackTrace();
        }
    }

   
    private Timestamp parseTimestamp(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsedDate = dateFormat.parse(dateStr);
            return new Timestamp(parsedDate.getTime());
        } catch (Exception e) {
            System.err.println("Error parsing date and time: " + dateStr);
            e.printStackTrace();
            return null;
        }
    }
}
