# com.cdsoftware.attendance

- Copyright: 2026 https://www.casadelsoftware.com
- Repository: https://bitbucket.org/cdsoftware/com.cdsoftware.attendance
- License: GPL 2

## Description

iDempiere attendance management extension for employee shifts, biometric markings, daily attendance, missing dates, permissions, and payroll extra-hour calculation. It imports data from attachments, directories, interface tables, Bioadmin, ITAS, ZKTeco, and REST attendance devices.

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
- `joda-time` 2.10.8
- `json` 20190722.0.0
- `com.cdsoftware.payroll` 12.0.0
- `com.cdsoftware.pluginconfig` 12.x

## Features/Documentation

### Source Structure

```text
com.cdsoftware.attendance/src
└── com/cdsoftware/lirion/attendance
    ├── base
    ├── callout
    │   ├── CheckBpPermission.java
    │   └── SetQtyOfHours.java
    ├── component
    ├── model
    │   ├── MGH_Shifts.java
    │   ├── MGH_ShiftsLine.java
    │   ├── MHR_Attendance.java
    │   ├── MHR_AttendanceLine.java
    │   ├── MHR_C_BPartnerShifts.java
    │   ├── MIAttendance.java
    │   ├── MMarking.java
    │   └── ...
    ├── process
    │   ├── AddMissingDates.java
    │   ├── CalculateExtraHour.java
    │   ├── CompleteAttendance.java
    │   ├── Create_C_BPartner_Shift.java
    │   ├── ImportAttendance*.java
    │   └── ProcessAttendance*.java
    └── util
```

### Processes

| Process | Class | Installed by 2Pack | Annotation | Purpose |
| --- | --- | --- | --- | --- |
| Add Missing Dates | `AddMissingDates` | Yes | Yes | Creates or deletes attendance rows for dates without markings. |
| Calculate Extra Hour | `CalculateExtraHour` | Yes | Yes | Calculates payroll movements for worked intervals outside the shift. |
| Complete Attendance | `CompleteAttendance` | No evidence in included 2Pack | Yes | Completes imported markings using shift and lost-time information. |
| Assign Employee Shifts | `Create_C_BPartner_Shift` | Yes | Yes | Assigns a shift or rotating shift group to selected employees. |
| Import Attendance | `ImportAttendance` | Yes | Yes | Creates a manual marking for an employee and date. |
| Import Bioadmin Directory | `ImportAttendanceBioadmin` | No evidence in current 2Pack | Yes | Imports Bioadmin files from a configured directory. |
| Import Bioadmin by Clock Code | `ImportAttendanceBioadminClkCode` | No evidence in current 2Pack | Yes | Imports Bioadmin data using device clock codes. |
| Import Attached Attendance | `ImportAttendanceFromAttachment` | Yes | Yes | Imports a supported attendance attachment from the current record. |
| Import Configurable Bioadmin Attachment | `ImportAttendanceFromAttachmentBioadmin` | Yes | Yes | Imports delimited Bioadmin files with configurable columns and formats. |
| Import ITAS Attachment | `ImportAttendanceFromAttachmentITAS` | No evidence in current 2Pack | Yes | Imports ITAS attendance attachments. |
| Import ZKTeco Attachment | `ImportAttendanceFromAttachmentZKTeco` | No evidence in current 2Pack | Yes | Imports ZKTeco attendance attachments with configurable date formats. |
| Import Legacy ZK Attachment | `ImportAttendanceFromAttachmentzk` | No evidence in current 2Pack | Yes | Imports legacy ZK attachment formats. |
| Import Attendance Directory | `ImportAttendanceFromFile` | Legacy class name in 2Pack | Yes | Imports CSV files from a server directory into the attendance interface. |
| Import Attendance Interface | `ImportAttendanceFromI_Attendance` | Yes | Yes | Converts `I_Attendance` staging rows into attendance data. |
| Import Attendance REST | `ImportAttendanceFromServerREST` | Yes | Yes | Authenticates with a configured REST device server and imports markings. |
| Process Attendance | `ProcessAttendance` | Yes | Yes | Consolidates raw markings into attendance lines and extra-hour calculations. |
| Process Bioadmin Attendance | `ProcessAttendanceBioadmin` | No evidence in current 2Pack | Yes | Processes Bioadmin markings into attendance lines. |

#### `AddMissingDates`

- **Type:** Server process on an attendance header.
- **Parameters:** `HR_Attendance_ID`, `DateFrom`, date-to value, and delete mode.
- **Validations:** Requires a valid attendance date range.
- **Main logic:** Creates missing employee/date attendance rows or removes generated attendance data.
- **Result:** Reports created or deleted attendance records.

#### `CalculateExtraHour`

- **Type:** Payroll integration server process.
- **Parameters:** `AttendanceDate`, `AttendanceDate2`, `C_BPartner_ID`, `Time1`, `Time2`, `HR_Process_ID`, and `HR_Attendance_ID`.
- **Validations:** Requires shift configuration for the indicated day.
- **Main logic:** Classifies worked time outside the assigned shift and creates the applicable extra-hour payroll values.
- **Result:** Payroll extra-hour movements are calculated for the interval.

#### `CompleteAttendance`

- **Type:** Attendance completion process.
- **Parameters:** Uses the current record and `Description` context.
- **Validations:** Resolves the assigned shift before calculating lost time.
- **Main logic:** Completes marking information and derives missing or lost work time.
- **Result:** The attendance record is completed with shift-based values.

#### `Create_C_BPartner_Shift`

- **Type:** Employee shift assignment process.
- **Parameters:** `DateFrom`, `DateTo`, employee selection, `GH_Shifts_ID`, `HR_Department_ID`, and `GH_Shifts_RG_ID`.
- **Validations:** Requires at least one valid worker and a usable fixed or rotating shift selection.
- **Main logic:** Selects employees directly or by department and creates validity-dated shift assignments.
- **Result:** Employees receive fixed or rotating work schedules.

#### `ImportAttendance`

- **Type:** Manual marking process.
- **Parameters:** `MarkingDate` and `C_BPartner_ID`.
- **Validations:** Requires an employee and marking timestamp.
- **Main logic:** Creates a raw attendance marking for the selected employee.
- **Result:** One manual marking is available for attendance processing.

#### `ImportAttendanceBioadmin`

- **Type:** Directory import process.
- **Parameters:** Uses the current attendance/device configuration record.
- **Validations:** Requires valid Bioadmin files in the configured directory.
- **Main logic:** Scans and parses Bioadmin files and creates raw markings.
- **Result:** Valid files are imported and the process reports completion or an empty directory.

#### `ImportAttendanceBioadminClkCode`

- **Type:** Device-code directory import process.
- **Parameters:** Uses the current attendance/device configuration record.
- **Validations:** Validates source and destination directories and supported date values.
- **Main logic:** Maps clock codes to employees, imports Bioadmin rows, and moves processed files when configured.
- **Result:** Device-code markings are imported and archived.

#### `ImportAttendanceFromAttachment`

- **Type:** Attachment import process.
- **Parameters:** Uses the current record attachment.
- **Validations:** Requires an attachment and rejects rows whose employee cannot be resolved.
- **Main logic:** Parses the supported delimited format and creates raw markings.
- **Result:** Attached attendance rows become marking records.

#### `ImportAttendanceFromAttachmentBioadmin`

- **Type:** Configurable attachment import process.
- **Parameters:** Date-time format, delimiter type, header flag, employee/date indexes, hour-column flag, and up to four hour indexes.
- **Validations:** Requires an attachment and a supported, consistent separator.
- **Main logic:** Parses configurable Bioadmin layouts and creates one or more markings per row.
- **Result:** Flexible Bioadmin attachments are imported and optionally archived.

#### `ImportAttendanceFromAttachmentITAS`

- **Type:** ITAS attachment import process.
- **Parameters:** Uses the current record attachment.
- **Validations:** Requires an attached ITAS file and ignores unsupported rows.
- **Main logic:** Parses employee and timestamp values from the ITAS layout.
- **Result:** Returns total, created, and ignored line counts.

#### `ImportAttendanceFromAttachmentZKTeco`

- **Type:** ZKTeco attachment import process.
- **Parameters:** `DateTimeFormat`, `DateFormat`, and `HasHeader`.
- **Validations:** Requires an attachment and parseable employee/date values.
- **Main logic:** Reads ZKTeco rows using the configured formats and creates raw markings.
- **Result:** ZKTeco attachment data is imported.

#### `ImportAttendanceFromAttachmentzk`

- **Type:** Legacy ZK attachment import process.
- **Parameters:** Uses the current record attachment and configured layout.
- **Validations:** Requires an attachment and a resolvable employee for each accepted row.
- **Main logic:** Parses legacy ZK formats and writes marking records.
- **Result:** Legacy device exports are converted to attendance markings.

#### `ImportAttendanceFromFile`

- **Type:** Server-directory CSV import process.
- **Parameters:** `File_Directory`.
- **Validations:** Requires readable CSV files and reports rows that cannot be persisted.
- **Main logic:** Reads directory files and writes attendance interface records.
- **Result:** File data is staged for subsequent attendance import.

#### `ImportAttendanceFromI_Attendance`

- **Type:** Interface import process.
- **Parameters:** Optional `Device_Name`.
- **Validations:** Requires eligible records in `I_Attendance`.
- **Main logic:** Filters and validates staging rows and converts them to attendance data.
- **Result:** Interface records are imported and marked accordingly.

#### `ImportAttendanceFromServerREST`

- **Type:** REST integration process.
- **Parameters:** `HR_AttendanceDevices_ID`.
- **Validations:** Requires a configured device endpoint and successful authentication.
- **Main logic:** Authenticates with the remote service, downloads markings, and stores local records.
- **Result:** Remote device attendance is synchronized.

#### `ProcessAttendance`

- **Type:** Attendance consolidation process.
- **Parameters:** Optional `C_BPartner_ID`; the current attendance header supplies the date range.
- **Validations:** Requires header dates and raw markings in the selected period.
- **Main logic:** Orders employee markings, resolves shifts, creates attendance lines, and invokes extra-hour calculation for eligible intervals.
- **Result:** Raw markings become payroll-ready daily attendance lines.

#### `ProcessAttendanceBioadmin`

- **Type:** Bioadmin attendance consolidation process.
- **Parameters:** Optional `C_BPartner_ID`; uses the current attendance header.
- **Validations:** Requires Bioadmin markings for the selected date range.
- **Main logic:** Groups imported markings by employee and date and creates attendance line intervals.
- **Result:** Bioadmin markings become consolidated attendance records.

### Callouts

| Callout | Trigger | Window/table impact | User-visible result |
| --- | --- | --- | --- |
| `CheckBpPermission` | Request type, subtype, employee, or start date | Staff action request | Prevents more than three configured permission or tardiness requests for the employee in the same month. |
| `SetQtyOfHours` | `Time1` through `Time4` | `I_Marking`, `GH_ShiftsLine`, `HR_AttendanceLine` | Calculates both work intervals and total hours, including intervals crossing midnight. |

### Models

- `GH_Shifts` and `GH_ShiftsLine` define work schedules, daily intervals, tolerances, and breaks.
- `GH_Shifts_RG` and its lines define rotating shift groups.
- `HR_C_BPartnerShifts` assigns fixed or rotating shifts to employees for a validity period.
- `HR_Attendance` and `HR_AttendanceLine` store consolidated attendance headers and employee-day details.
- `I_Attendance` and `I_Marking` provide staging and raw-marking storage for imports.
- `HR_AttendanceDevices` stores biometric or REST device connection metadata.

### 2Pack Content

- `2Pack_3.0.6_Base.zip` installs the main attendance dictionary: shifts, rotating groups, employee assignments, markings, attendance headers and lines, devices, windows, tabs, menus, references, and core processes.
- `2Pack_3.0.7_ProcesoIAttendanceImport.zip` installs the interface attendance import process.
- `2Pack_3.0.8.zip` installs attendance directory import metadata; its recorded class uses a legacy package name.
- `2Pack_4.0.0.zip` updates employee shift assignment.
- `2Pack_4.0.1_Mantenimiento.zip` contains maintenance updates to columns and menus.
- `2Pack_4.0.2_UpdateProcessADDMissingDate.zip` updates Add Missing Dates parameters and references.

## Instructions

1. Install `com.cdsoftware.payroll` and `com.cdsoftware.pluginconfig` for iDempiere 12.
2. Deploy `com.cdsoftware.attendance` and refresh or restart the OSGi runtime.
3. Verify the incremental 2Pack packages under `META-INF` were imported in version order.
4. Configure shifts, rotating shift groups, employee assignments, request limits, attendance devices, directories, and import formats.
5. Import or synchronize raw markings, run attendance processing, review generated lines, and then calculate or transfer extra hours to payroll.
6. Grant roles access to the attendance windows, menus, reports, and processes.

## Extra Links

- [iDempiere](https://www.idempiere.org/)
- [Casa del Software](https://www.casadelsoftware.com/)
