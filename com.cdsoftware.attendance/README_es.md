# Gestión de Asistencia y Turnos (Attendance)

- Copyright: 2026 cdsoftware
- Repositorio: git@bitbucket.org:cdsoftware/com.cdsoftware.attendance.git
- Licencia: GPL 2

## Descripción

Este plugin ofrece un sistema completo para la gestión de asistencia, turnos y cálculo de horas extras de los empleados en iDempiere. Incluye integraciones con diversos dispositivos biométricos y servidores REST, además de una lógica avanzada para el procesamiento de marcas y asignación de turnos.

## Colaboradores

- info@casadelsoftware.com

## Componentes

- iDempiere Plugin [com.cdsoftware.attendance](com.cdsoftware.attendance)
- iDempiere Unit Test Fragment [com.cdsoftware.attendance.test](com.cdsoftware.attendance.test)

## Diccionario y Modelos de Datos

### Ventanas y Pestañas
- **Gestión de Turnos**: Configuración de horarios de entrada/salida por día de la semana.
- **Marcaciones**: Registro de marcas crudas provenientes de relojes biométricos.
- **Asistencia**: Resumen procesado de la jornada laboral de cada empleado.
- **Dispositivos de Asistencia**: Configuración de terminales biométricas.

### Modelos (Tablas)
- `GH_Shifts`: Cabecera de turnos.
- `GH_ShiftsLine`: Detalle de horarios, tolerancias y descansos por día.
- `HR_Attendance`: Registro consolidado de asistencia diaria.
- `HR_AttendanceLine`: Detalle de entrada/salida y horas calculadas.
- `I_Marking`: Tabla de interfaz para la importación de marcas.

## Prerrequisitos

- Java 11, comandos `java` and `javac`.
- iDempiere 10
- Configurar la variable de entorno `IDEMPIERE_REPOSITORY`
- Dependencias: `joda-time`, `json`.

## Características/Documentación

### Paquetes
- `com.cdsoftware.lirion.attendance.process`: Procesadores de marcas, calculadoras de horas extras e importadores.
- `com.cdsoftware.lirion.attendance.model`: Lógica de persistencia para turnos y asistencias.
- `com.cdsoftware.lirion.attendance.callout`: Validaciones de permisos y cálculo de horas en tiempo real.
- `com.cdsoftware.lirion.attendance.util`: Generadores de plantillas y utilidades SQL.

### Procesos

| Proceso | Parámetros | Descripción |
|---------|------------|-------------|
| `ProcessAttendance` | N/A | Procesa las marcas crudas y las convierte en registros de asistencia basados en el turno del empleado. |
| `CalculateExtraHour` | `AttendanceDate`, `C_BPartner_ID`, etc. | Calcula horas extras diurnas, nocturnas, de domingo y días de descanso. |
| `ImportAttendanceBioadmin` | Archivo adjunto | Importa marcas desde archivos generados por el software Bioadmin. |
| `ImportAttendanceFromServerREST` | N/A | Sincroniza marcas desde un servidor REST centralizado. |
| `AddMissingDates` | Rango de fechas | Genera registros vacíos para días sin marcas para facilitar el control administrativo. |

### Validaciones y Lógica
- **Cálculo Automático**: El sistema diferencia automáticamente entre horas laborales normales y diferentes tipos de extras según la legislación configurada en los conceptos de nómina.
- **Tolerancias**: Soporta configuración de minutos de gracia para entradas y salidas tardías.

## Instrucciones

- Instale el plugin y configure los dispositivos en la ventana "Dispositivos de Asistencia".
- Defina los turnos de trabajo y asígnelos a los empleados en la pestaña de Recursos Humanos.
- Ejecute periódicamente el proceso de "Procesar Asistencia" para consolidar la información para la nómina.

## Enlaces Extra

- [Sitio Web de CDS](https://casadelsoftware.com)

## Comandos

Compilar plugin y ejecutar pruebas:

```bash
./build
```

Usar el parámetro `debug` para el modo de depuración, ejemplo:

```bash
./build debug
```

Para usar `.\build.bat` en Windows.
