# com.cdsoftware.attendance
- Copyright: 2026 https://www.casadelsoftware.com
- Repository: https://bitbucket.org/cdsoftware/com.cdsoftware.attendance.git
- License: GPL 2

## Description
The `com.cdsoftware.attendance` plugin is a custom extension for iDempiere. It extends standard system capabilities by providing custom server processes, column callouts, database models, and Application Dictionary configurations (2Pack) to track employee clock-in/out logs, shifts, and biometric time card integrations.

## Contributors
- 2024 Carlo Gonzalez <carlogonzalez@casadelsoftware.com>.
- 2024 Eduardo Gil <egil@ghintech.com>.
- 2024 Josian Ascanio <josianascanio@casadelsoftware.com>.
- 2026 Angel Lara <angel@casadelsoftware.com>.

## Components
- iDempiere Plugin [com.cdsoftware.attendance](com.cdsoftware.attendance)
- iDempiere Unit Test Fragment [com.cdsoftware.attendance.test](com.cdsoftware.attendance.test)

## Prerequisites
- Java 11, commands `java` and `javac`.
- iDempiere 12
- Dependencies: joda-time, json, com.cdsoftware.payroll, com.cdsoftware.pluginconfig, 13.0.0)"

## Features/Documentation
### Source Structure
```
├── com/
        ├── cdsoftware/
            ├── lirion/
                ├── attendance/
                    ├── util/
                        ├── FileTemplateBuilder.java
                        ├── KeyValueLogger.java
                        ├── SqlBuilder.java
                        ├── TimestampUtil.java
                    ├── model/
                        ├── I_GH_Shifts.java
                        ├── I_GH_ShiftsLine.java
                        ├── I_GH_Shifts_RG.java
                        ├── I_GH_Shifts_RG_Line.java
                        ├── I_HR_Attendance.java
                        ├── I_HR_AttendanceDevices.java
                        ├── I_HR_AttendanceLine.java
                        ├── I_HR_C_BPartnerShifts.java
                        ├── I_I_Attendance.java
                        ├── I_I_Marking.java
                        ├── MGH_Shifts.java
                        ├── MGH_ShiftsLine.java
                        ├── MHR_Attendance.java
                        ├── MHR_AttendanceLine.java
                        ├── MHR_C_BPartnerShifts.java
                        ├── MIAttendance.java
                        ├── MMarking.java
                        ├── X_GH_Shifts.java
                        ├── X_GH_ShiftsLine.java
                        ├── X_GH_Shifts_RG.java
                        ├── X_GH_Shifts_RG_Line.java
                        ├── X_HR_Attendance.java
                        ├── X_HR_AttendanceDevices.java
                        ├── X_HR_AttendanceLine.java
                        ├── X_HR_C_BPartnerShifts.java
                        ├── X_I_Attendance.java
                        ├── X_I_Marking.java
                    ├── base/
                        ├── BundleInfo.java
                        ├── CustomCallout.java
                        ├── CustomEvent.java
                        ├── CustomForm.java
                        ├── CustomProcess.java
                    ├── callout/
                        ├── CheckBpPermission.java
                        ├── SetQtyOfHours.java
                    ├── component/
                        ├── CalloutFactory.java
                        ├── EventFactory.java
                        ├── FormFactory.java
                        ├── ModelFactory.java
                        ├── ProcessFactory.java
                    ├── process/
                        ├── AddMissingDates.java
                        ├── CalculateExtraHour.java
                        ├── CompleteAttendance.java
                        ├── Create_C_BPartner_Shift.java
                        ├── ImportAttendance.java
                        ├── ImportAttendanceBioadmin.java
                        ├── ImportAttendanceBioadminClkCode.java
                        ├── ImportAttendanceFromAttachment.java
                        ├── ImportAttendanceFromAttachmentBioadmin.java
                        ├── ImportAttendanceFromAttachmentITAS.java
                        ├── ImportAttendanceFromAttachmentZKTeco.java
                        ├── ImportAttendanceFromAttachmentzk.java
                        ├── ImportAttendanceFromFile.java
                        ├── ImportAttendanceFromI_Attendance.java
                        ├── ImportAttendanceFromServerREST.java
                        ├── ProcessAttendance.java
                        ├── ProcessAttendanceBioadmin.java
```

### Processes

| Class Name | Purpose | Main Parameters | Key Logic & Results |
| --- | --- | --- | --- |
| `CalculateExtraHour` | Server process. | `AttendanceDate`, `AttendanceDate2`, `C_BPartner_ID`, `HR_Attendance_ID`, `HR_Process_ID`, `Time1`, `Time2` | Re-calculates totals, hours, or costs based on transaction context. |
| `ImportAttendanceFromAttachmentzk` | Server process. | None | Executes core logic and updates database records. |
| `ImportAttendanceBioadmin` | Server process. | None | Executes core logic and updates database records. |
| `ImportAttendanceBioadminClkCode` | Server process. | None | Executes core logic and updates database records. |
| `ProcessAttendanceBioadmin` | Server process. | `C_BPartner_ID` | Updates approval status and logs the approver's user ID. |
| `ImportAttendanceFromFile` | Server process. | `File_Directory` | Executes core logic and updates database records. |
| `ImportAttendanceFromI_Attendance` | Server process. | `Device_Name` | Executes core logic and updates database records. |
| `ImportAttendanceFromAttachmentITAS` | Server process. | None | Executes core logic and updates database records. |
| `CompleteAttendance` | Server process. | `Description` | Executes core logic and updates database records. |
| `AddMissingDates` | Server process. | `DateFrom`, `HR_Attendance_ID` | Executes core logic and updates database records. |
| `ProcessAttendance` | Server process. | `C_BPartner_ID` | Updates approval status and logs the approver's user ID. |
| `ImportAttendanceFromAttachmentZKTeco` | Server process. | `DateFormat`, `DateTimeFormat`, `HasHeader` | Executes core logic and updates database records. |
| `ImportAttendanceFromAttachmentBioadmin` | Server process. | `DateTimeFormat`, `FormatType`, `HasHeader`, `HasHoursColumns`, `Hour1Index`, `Hour2Index`, `Hour3Index`, `Hour4Index` | Executes core logic and updates database records. |
| `ImportAttendance` | Server process. | `C_BPartner_ID`, `MarkingDate` | Executes core logic and updates database records. |
| `ImportAttendanceFromAttachment` | Server process. | None | Executes core logic and updates database records. |
| `Create_C_BPartner_Shift` | Server process. | `C_BPartner_ID`, `DateFrom`, `DateTo`, `GH_Shifts_ID`, `GH_Shifts_RG_ID`, `HR_Department_ID` | Creates new records and links them to the active context. |
| `ImportAttendanceFromServerREST` | Server process. | `HR_AttendanceDevices_ID` | Executes core logic and updates database records. |



### Callouts

| Callout Class | Target Field / Column | Business Validation & UI Impact |
| --- | --- | --- |
| `SetQtyOfHours` | Calculated fields | Validates UI inputs or auto-populates dependent fields. |
| `CheckBpPermission` | Calculated fields | Validates UI inputs or auto-populates dependent fields. |


### Generated Models

| Model | Table | Functional role |
| --- | --- | --- |
| `X_HR_Attendance` | `X_HR_Attendance` | Represents database records and implements custom business logic. |
| `X_HR_C_BPartnerShifts` | `X_HR_C_BPartnerShifts` | Represents database records and implements custom business logic. |
| `I_GH_Shifts_RG` | `I_GH_Shifts_RG` | Represents database records and implements custom business logic. |
| `X_GH_Shifts_RG_Line` | `X_GH_Shifts_RG_Line` | Represents database records and implements custom business logic. |
| `I_HR_C_BPartnerShifts` | `I_HR_C_BPartnerShifts` | Represents database records and implements custom business logic. |
| `MHR_AttendanceLine` | `HR_AttendanceLine` | This file is part of iDempiere ERP Open Source * http://www.idempiere.org * * Copyright (C) Contributors * * This program is free software; you can redistribute it and/or * modify it under the terms of the GNU General Public License * as published by the Free Software Foundation; either version 2 * of the License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; if not, write to the Free Software * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, * MA 02110-1301, USA. * * Contributors: * - Casa del Software * / package com.cdsoftware.lirion.attendance.model; import java.sql.ResultSet; import java.util.Properties; /** Model class for Attendance Lines (HR_AttendanceLine). Represents specific daily attendance records, including clock-in/out times, hours worked, and status (rest day, holiday, etc.). @author Casa del Software |
| `I_I_Marking` | `I_I_Marking` | Represents database records and implements custom business logic. |
| `I_I_Attendance` | `I_I_Attendance` | Represents database records and implements custom business logic. |
| `I_HR_Attendance` | `I_HR_Attendance` | Represents database records and implements custom business logic. |
| `X_HR_AttendanceDevices` | `X_HR_AttendanceDevices` | Represents database records and implements custom business logic. |
| `MIAttendance` | `IAttendance` | This file is part of iDempiere ERP Open Source * http://www.idempiere.org * * Copyright (C) Contributors * * This program is free software; you can redistribute it and/or * modify it under the terms of the GNU General Public License * as published by the Free Software Foundation; either version 2 * of the License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; if not, write to the Free Software * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, * MA 02110-1301, USA. * * Contributors: * - Casa del Software * / package com.cdsoftware.lirion.attendance.model; import java.sql.ResultSet; import java.sql.Timestamp; import java.util.Properties; /** Model class for Attendance Import (I_Attendance). Used as a staging model for importing attendance records from external sources. @author Casa del Software |
| `X_GH_Shifts_RG` | `X_GH_Shifts_RG` | Represents database records and implements custom business logic. |
| `MMarking` | `Marking` | This file is part of iDempiere ERP Open Source * http://www.idempiere.org * * Copyright (C) Contributors * * This program is free software; you can redistribute it and/or * modify it under the terms of the GNU General Public License * as published by the Free Software Foundation; either version 2 * of the License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; if not, write to the Free Software * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, * MA 02110-1301, USA. * * Contributors: * - Casa del Software * / package com.cdsoftware.lirion.attendance.model; import java.sql.ResultSet; import java.util.List; import java.util.Properties; import org.compiere.model.MRefList; import org.compiere.model.Query; /** Model class for Attendance Markings (I_Marking). Represents raw markings imported from attendance devices. @author Casa del Software |
| `X_GH_Shifts` | `X_GH_Shifts` | Represents database records and implements custom business logic. |
| `MGH_Shifts` | `GH_Shifts` | This file is part of iDempiere ERP Open Source * http://www.idempiere.org * * Copyright (C) Contributors * * This program is free software; you can redistribute it and/or * modify it under the terms of the GNU General Public License * as published by the Free Software Foundation; either version 2 * of the License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; if not, write to the Free Software * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, * MA 02110-1301, USA. * * Contributors: * - Casa del Software * / package com.cdsoftware.lirion.attendance.model; import java.sql.ResultSet; import java.util.Properties; /** Model class for Work Shifts (GH_Shifts). Represents a work shift configuration, including diurnal/nocturnal boundaries and specific work rules. @author Casa del Software |
| `X_GH_ShiftsLine` | `X_GH_ShiftsLine` | Represents database records and implements custom business logic. |
| `MHR_C_BPartnerShifts` | `HR_C_BPartnerShifts` | This file is part of iDempiere ERP Open Source * http://www.idempiere.org * * Copyright (C) Contributors * * This program is free software; you can redistribute it and/or * modify it under the terms of the GNU General Public License * as published by the Free Software Foundation; either version 2 * of the License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; if not, write to the Free Software * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, * MA 02110-1301, USA. * * Contributors: * - Casa del Software * / package com.cdsoftware.lirion.attendance.model; import java.sql.ResultSet; import java.sql.Timestamp; import java.util.Properties; /** Model class for Business Partner Work Shifts (HR_C_BPartnerShifts). Links employees to specific work shifts for a given date range. @author Casa del Software |
| `I_GH_Shifts_RG_Line` | `I_GH_Shifts_RG_Line` | Represents database records and implements custom business logic. |
| `I_GH_Shifts` | `I_GH_Shifts` | Represents database records and implements custom business logic. |
| `MHR_Attendance` | `HR_Attendance` | This file is part of iDempiere ERP Open Source * http://www.idempiere.org * * Copyright (C) Contributors * * This program is free software; you can redistribute it and/or * modify it under the terms of the GNU General Public License * as published by the Free Software Foundation; either version 2 * of the License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; if not, write to the Free Software * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, * MA 02110-1301, USA. * * Contributors: * - Casa del Software * / package com.cdsoftware.lirion.attendance.model; import java.sql.ResultSet; import java.sql.Timestamp; import java.time.LocalDateTime; import java.util.Date; import java.util.List; import java.util.Properties; import org.compiere.model.MRefList; import org.compiere.model.Query; /** Model class for Attendance Headers (HR_Attendance). Manages the top-level attendance records for a period and employee. @author Casa del Software |
| `X_HR_AttendanceLine` | `X_HR_AttendanceLine` | Represents database records and implements custom business logic. |
| `MGH_ShiftsLine` | `GH_ShiftsLine` | This file is part of iDempiere ERP Open Source * http://www.idempiere.org * * Copyright (C) Contributors * * This program is free software; you can redistribute it and/or * modify it under the terms of the GNU General Public License * as published by the Free Software Foundation; either version 2 * of the License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; if not, write to the Free Software * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, * MA 02110-1301, USA. * * Contributors: * - Casa del Software * / package com.cdsoftware.lirion.attendance.model; import java.sql.ResultSet; import java.util.Properties; /** Model class for Work Shift Lines (GH_ShiftsLine). Represents specific daily rules within a work shift, such as start and end times for different days of the week. @author Casa del Software |
| `X_I_Marking` | `X_I_Marking` | Represents database records and implements custom business logic. |
| `I_HR_AttendanceLine` | `I_HR_AttendanceLine` | Represents database records and implements custom business logic. |
| `I_HR_AttendanceDevices` | `I_HR_AttendanceDevices` | Represents database records and implements custom business logic. |
| `I_GH_ShiftsLine` | `I_GH_ShiftsLine` | Represents database records and implements custom business logic. |
| `X_I_Attendance` | `X_I_Attendance` | Represents database records and implements custom business logic. |


### Application Dictionary Metadata (2Pack)

| Package / File Name | Purpose & Dictionary Configurations |
| --- | --- |
| `2Pack_2.0.0.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.2.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.5.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.6.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.7.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.8.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.9.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.1.0.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.1.1.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.1.2.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.1.3.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.1.4_OrdernarMenu.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.1.6_RequestType.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.1.7.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.1.8_UpdateReports.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_3.0.0.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_3.0.1_TablaIAccessControl.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_3.0.2.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_3.0.3.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_3.0.4_UpdateProcessParameter.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_3.0.5.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_3.0.6_Base.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_3.0.7_ProcesoIAttendanceImport.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_3.0.8.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_4.0.0.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_4.0.1_Mantenimiento.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_4.0.2_UpdateProcessADDMissingDate.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `CalloutFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `EventFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `FormFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `ModelFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `ProcessFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `xml-invoice.xml` | Metadata package containing Application Dictionary (AD) configurations. |


## Instructions
1. Deploy the `com.cdsoftware.attendance` OSGi bundle in your iDempiere environment.
2. Restart iDempiere and refresh OSGi bundles to register factories.
3. Configure dictionary and role access rules as needed.
