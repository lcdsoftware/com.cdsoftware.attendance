# com.cdsoftware.attendance

- Copyright: 2026 https://www.casadelsoftware.com
- Repository: https://github.com/lcdsoftware/com.cdsoftware.attendance
- License: GPL 2

## Description

iDempiere attendance management extension for employee shifts, biometric markings, daily attendance, missing dates, permissions, and payroll extra-hour calculation. It imports data from attachments, directories, interface tables, Bioadmin, ITAS, ZKTeco, and a REST service. Importers populate attendance lines or staging records; payroll processes calculate absence and extra-hour attributes from existing attendance data.

## Contributors

- 2024–2026 Carlo Gonzalez <carlogonzalez@casadelsoftware.com>.
- 2024 Eduardo Gil <egil@ghintech.com>.
- 2024 Josian Ascanio <josianascanio@casadelsoftware.com>.
- 2024–2026 Ángel Lara <angel@casadelsoftware.com>.

## Components

- iDempiere Plugin [com.cdsoftware.attendance](com.cdsoftware.attendance)
- iDempiere Unit Test Fragment [com.cdsoftware.attendance.test](com.cdsoftware.attendance.test)

## Prerequisites

- Java 17, commands `java` and `javac`.
- iDempiere 12
- `joda-time` 2.10.8
- `json` 20190722.0.0
- `com.cdsoftware.payroll` 12.0.0
- `com.cdsoftware.pluginconfig` 12.x

## Features/Documentation

### Source Structure

```text
com.cdsoftware.attendance/src
└── com
    └── cdsoftware
        └── lirion
            └── attendance
                ├── base
                │   ├── BundleInfo.java
                │   ├── CustomCallout.java
                │   ├── CustomEvent.java
                │   ├── CustomForm.java
                │   └── CustomProcess.java
                ├── callout
                │   ├── CheckBpPermission.java
                │   └── SetQtyOfHours.java
                ├── component
                │   ├── CalloutFactory.java
                │   ├── EventFactory.java
                │   ├── FormFactory.java
                │   ├── ModelFactory.java
                │   └── ProcessFactory.java
                ├── model
                │   ├── I_GH_Shifts.java
                │   ├── I_GH_ShiftsLine.java
                │   ├── I_GH_Shifts_RG.java
                │   ├── I_GH_Shifts_RG_Line.java
                │   ├── I_HR_Attendance.java
                │   ├── I_HR_AttendanceDevices.java
                │   ├── I_HR_AttendanceLine.java
                │   ├── I_HR_C_BPartnerShifts.java
                │   ├── I_I_Attendance.java
                │   ├── I_I_Marking.java
                │   ├── MGH_Shifts.java
                │   ├── MGH_ShiftsLine.java
                │   ├── MHR_Attendance.java
                │   ├── MHR_AttendanceLine.java
                │   ├── MHR_C_BPartnerShifts.java
                │   ├── MIAttendance.java
                │   ├── MMarking.java
                │   ├── X_GH_Shifts.java
                │   ├── X_GH_ShiftsLine.java
                │   ├── X_GH_Shifts_RG.java
                │   ├── X_GH_Shifts_RG_Line.java
                │   ├── X_HR_Attendance.java
                │   ├── X_HR_AttendanceDevices.java
                │   ├── X_HR_AttendanceLine.java
                │   ├── X_HR_C_BPartnerShifts.java
                │   ├── X_I_Attendance.java
                │   └── X_I_Marking.java
                ├── process
                │   ├── AddMissingDates.java
                │   ├── CalculateExtraHour.java
                │   ├── CompleteAttendance.java
                │   ├── Create_C_BPartner_Shift.java
                │   ├── ImportAttendance.java
                │   ├── ImportAttendanceBioadmin.java
                │   ├── ImportAttendanceBioadminClkCode.java
                │   ├── ImportAttendanceFromAttachment.java
                │   ├── ImportAttendanceFromAttachmentBioadmin.java
                │   ├── ImportAttendanceFromAttachmentITAS.java
                │   ├── ImportAttendanceFromAttachmentZKTeco.java
                │   ├── ImportAttendanceFromAttachmentzk.java
                │   ├── ImportAttendanceFromFile.java
                │   ├── ImportAttendanceFromI_Attendance.java
                │   ├── ImportAttendanceFromServerREST.java
                │   ├── ProcessAttendance.java
                │   └── ProcessAttendanceBioadmin.java
                └── util
                    ├── FileTemplateBuilder.java
                    ├── KeyValueLogger.java
                    ├── SqlBuilder.java
                    └── TimestampUtil.java
com.cdsoftware.attendance.test/src
└── com
    └── cdsoftware
        └── lirion
            └── attendance
                ├── test
                │   ├── assertion
                │   │   └── Annotations.java
                │   └── util
                │       ├── RandomTestUtil.java
                │       └── ReflectionTestUtil.java
                └── util
                    ├── FileTemplateBuilderTest.java
                    ├── KeyValueLoggerTest.java
                    ├── SqlBuilderTest.java
                    └── TimestampUtilTest.java
```

### Processes

Parameters below describe what the Java implementation reads and needs. Actual mandatory flags and defaults must also be checked in the installed Application Dictionary; annotations alone do not install a process or its parameters.

| Class | Purpose | Main parameters and requirements | Key logic and results |
| --- | --- | --- | --- |
| `AddMissingDates` | Generate missing attendance dates or delete empty data. | Generation: `HR_Attendance_ID` or both ends of the `DateFrom` range. Optional `delete` (default false); deletion uses the explicit date range. | Creates missing employee/day lines using employment and shift assignments. Delete mode removes zero-hour lines in the range and all line-less headers for the client, not just the selected header. |
| `CalculateExtraHour` | Calculate payroll extra hours. | `AttendanceDate`, `C_BPartner_ID`, `Time1`, `Time2` and payroll/attendance context must be usable; also reads `AttendanceDate2`, `HR_Process_ID`, `HR_Attendance_ID`. | Classifies daytime, nighttime, rest-day and Sunday intervals and saves `HR_Attribute` amounts using predefined payroll concept search keys. Requires the corresponding shift and payroll setup. |
| `CompleteAttendance` | Incomplete legacy clock-out estimation. | `Description` is interpreted as a numeric shift ID. | Queries `I_Marking` rows with null `Time2` and compares shift times. The implementation does not assign or save an estimated clock-out; it must not be treated as an operational completion process. |
| `Create_C_BPartner_Shift` | Assign a shift to employees. | Usable `DateFrom`, `DateTo`, `GH_Shifts_ID`, and employee selection through `C_BPartner_ID` or `GH_Shifts_RG_ID`. `HR_Department_ID` is read but unused. | A supplied group replaces the single-employee selection. Creates dated `HR_C_BPartnerShifts` records and skips assignments detected by its existing-range check. |
| `ImportAttendance` | Calculate lateness from legacy markings. | `MarkingDate` range is needed. `C_BPartner_ID` is read but not applied as a query filter. | Reads existing `I_Marking` rows, sums shift-hour differences by employee value, marks rows imported and creates absence-hour `HR_Attribute` records. It does not create a manual marking; payroll process and organization IDs are hardcoded. |
| `ImportAttendanceBioadmin` | Import Bioadmin directory files. | No named parameters; requires files under `ATTENDANCE_FILE_LOCATION`. | Parses supported layouts, resolves employees and writes attendance headers/lines. Uses format-specific file handling and renames processed files. |
| `ImportAttendanceBioadminClkCode` | Import Bioadmin using clock codes. | No named parameters; requires `ATTENDANCE_FILE_LOCATION` and employee clock-code mappings. | Resolves device clock codes to employees, creates attendance headers/lines and renames processed files. |
| `ImportAttendanceFromAttachment` | Import the supported delimited attendance layout. | No named parameters; attachment required on the current record. | Parses employee/date/time columns and creates attendance headers/lines. The column layout is implemented in the importer. |
| `ImportAttendanceFromAttachmentBioadmin` | Import configurable Bioadmin attachments. | Attachment required. Layout options: `DateTimeFormat`, `FormatType`, `HasHeader`, `bpIndex`, `dateIndex`, `HasHoursColumns`, `Hour1Index`, `Hour2Index`, `Hour3Index`, `Hour4Index`; use values matching the input file. | Supports timestamp rows and separate hour columns, groups employee/date entries, applies its configured time-block filtering and writes attendance lines. Includes file archival handling. |
| `ImportAttendanceFromAttachmentITAS` | Import ITAS attachments. | No named parameters; supported ITAS attachment required. | Parses employee/date/time fields into attendance lines and reports created and ignored row counts. |
| `ImportAttendanceFromAttachmentZKTeco` | Import ZKTeco attachments. | Attachment and valid `DateTimeFormat` / `DateFormat` required; optional `HasHeader` defaults to false. | Groups parsed employee/date entries into attendance lines with up to four time values. |
| `ImportAttendanceFromAttachmentzk` | Import legacy ZK attachments. | No named parameters; supported attachment required. | Parses the legacy layout and populates attendance data using its interface and attendance models. |
| `ImportAttendanceFromFile` | Stage server-directory CSV data. | `File_Directory` must identify a readable directory. | Reads CSV files and saves `I_Attendance` records for subsequent interface import. |
| `ImportAttendanceFromI_Attendance` | Convert interface rows into attendance. | Optional `Device_Name` filter; eligible `I_Attendance` records required. | Creates attendance headers/lines, updates interface records and can create missing employee or device records as implemented. |
| `ImportAttendanceFromServerREST` | Stage attendance downloaded through REST. | Optional `HR_AttendanceDevices_ID` selects the device serial; URL comes from `CDS_AT_BASE_URL` or a fallback. | Authenticates and saves remote rows into `I_Attendance`. Authentication and context values are hardcoded in this version; device metadata alone does not configure the connection. |
| `ProcessAttendance` | Calculate absence and extra-hour payroll attributes. | Current attendance header with `DateFrom` and `DateTo`; optional `C_BPartner_ID`. | Deletes existing `HR_Attribute` records linked to that attendance/client (and employee when selected), then evaluates existing attendance lines against shifts, holidays and rest days. Writes absence attributes and invokes extra-hour calculation. |
| `ProcessAttendanceBioadmin` | Evaluate time differences in Bioadmin attendance lines. | Current attendance header with a date range; optional `C_BPartner_ID`. | Evaluates existing lines against shifts and updates time differences. Payroll absence writes, prior-attribute deletion and the extra-hour invocation are commented out in the execution path; this variant does not generate those payroll results. |

### Events

`EventFactory` scans `com.cdsoftware.lirion.attendance.event`, but this source tree contains no concrete event handlers. Its OSGi descriptor registers the component; it is not a 2Pack.

### Callouts

| Class | Target fields | Business rule and UI impact |
| --- | --- | --- |
| `CheckBpPermission` | Annotation targets `M_Request`: `CDS_R_RequestTypeDetails_ID`, `R_RequestType_ID`, `C_BPartner_ID`, `CDS_StartDate`. Queries `R_Request`. | Returns `SubtypeOver3PerMonth` when the matching permission or tardiness count reaches three. Uses hardcoded type/subtype UUIDs and compares month numbers without a year filter. Verify the annotation target against the actual request table before relying on activation. |
| `SetQtyOfHours` | `I_Marking`, `GH_ShiftsLine`, `HR_AttendanceLine`: `Time1` through `Time4`. | Calculates `QtyOfHours1`, `QtyOfHours2` and, when available, `TotalQtyOfHours`. Adds 24 hours for a negative interval to handle midnight crossing. |

### Models

- `GH_Shifts` and `GH_ShiftsLine` define work schedules, daily intervals, tolerances, and breaks.
- `GH_Shifts_RG` and its lines define rotating shift groups.
- `HR_C_BPartnerShifts` assigns fixed or rotating shifts to employees for a validity period.
- `HR_Attendance` and `HR_AttendanceLine` store consolidated attendance headers and employee-day details.
- `I_Attendance` and `I_Marking` provide staging and raw-marking storage for imports.
- `HR_AttendanceDevices` stores biometric or REST device connection metadata.

### Application Dictionary Metadata (2Pack)

These are the six packages currently included under the plugin's `META-INF` directory. Their historical package versions differ from the bundle version.

| Package | Purpose and dictionary content |
| --- | --- |
| `2Pack_3.0.6_Base.zip` | Main attendance dictionary: shifts, groups, assignments, markings, attendance, devices, windows, menus and configuration. Includes `AddMissingDates`, `CalculateExtraHour`, `Create_C_BPartner_Shift`, `ImportAttendance`, `ImportAttendanceFromAttachment`, `ImportAttendanceFromAttachmentBioadmin`, `ImportAttendanceFromServerREST` and `ProcessAttendance`. |
| `2Pack_3.0.7_ProcesoIAttendanceImport.zip` | Menu/process metadata for `ImportAttendanceFromI_Attendance`. |
| `2Pack_3.0.8.zip` | Directory-import menu/process metadata. Its class is `com.cdsoftware.lirion.attendancemanager.process.ImportAttendanceFromFile`; the current Java package is `com.cdsoftware.lirion.attendance.process`. Check and correct the installed process class before use. |
| `2Pack_4.0.0.zip` | Updates `Create_C_BPartner_Shift` process metadata. |
| `2Pack_4.0.1_Mantenimiento.zip` | Maintenance SQL, menu changes and package-export metadata. |
| `2Pack_4.0.2_UpdateProcessADDMissingDate.zip` | Updates `AddMissingDates` process parameters and references. |

The remaining annotated process classes have no matching class entry in these bundled `PackOut.xml` files. Check existing dictionary registration or configure it explicitly before exposing them to users. `OSGI-INF/*.xml` files register components, and the test fragment's `xml-invoice.xml` is a test resource; neither belongs in the 2Pack inventory.

## Instructions

1. Install the iDempiere 12 dependencies listed above and deploy the `com.cdsoftware.attendance` bundle using Java 17.
2. Refresh or restart the OSGi runtime. The bundle uses `Incremental2PackActivator`; verify the import results for the packaged dictionary updates and resolve any import errors before use.
3. Verify process class names, parameter definitions and role access in the installed dictionary, including the legacy class name documented above.
4. Configure employees, shifts, dated assignments, calendars and the payroll concepts used by the selected process. Several legacy paths contain fixed record IDs; check their applicability to the target client.
5. Select the importer matching the source format. Directory and REST imports can populate `I_Attendance`; run `ImportAttendanceFromI_Attendance` to convert staging records. Attachment and Bioadmin importers can create attendance lines directly.
6. Review attendance lines, generate missing dates when needed, and run the appropriate payroll calculation process. Review the resulting payroll attributes before continuing payroll processing. `CompleteAttendance` is unfinished and is not part of this operational sequence.
7. For REST import, review the implementation's authentication/context configuration before enabling it; `CDS_AT_BASE_URL` and device selection configure only part of the connection.

## Extra Links

- [iDempiere](https://www.idempiere.org/)
- [Casa del Software](https://www.casadelsoftware.com/)
