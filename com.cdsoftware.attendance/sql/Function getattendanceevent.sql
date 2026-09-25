-- FUNCTION: adempiere.getattendanceevent(numeric, numeric, character varying)

-- DROP FUNCTION IF EXISTS adempiere.getattendanceevent(numeric, numeric, character varying);

CREATE OR REPLACE FUNCTION adempiere.getattendanceevent(
	p_c_bpartner_id numeric,
	p_attendanceline_id numeric,
	p_returnvalue character varying)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    STABLE PARALLEL UNSAFE
AS $BODY$
DECLARE
v_event varchar = '';
v_starttime timestamp;
v_endtime timestamp;
v_startturn timestamp;
v_endturn timestamp;
v_tolerancestart int;
v_toleranceend int;
v_hasrequest boolean;
v_lateminutes int;
v_r_subtypeID int;
BEGIN
v_hasrequest=false;
v_tolerancestart = 0;
v_toleranceend = 0;
v_r_subtypeID = 0;

	--GET Turn of employee
	select distinct ON (al.attendancedate) 
	sl.Time1,sl.time4,Tolerance into v_startturn,v_endturn,v_tolerancestart	 
	from HR_AttendanceLine al
	join HR_C_BPartnerShifts bps on bps.c_bpartner_id = al.c_bpartner_id
	join GH_ShiftsLine sl on sl.GH_Shifts_ID=bps.GH_Shifts_ID and al.WeekDay=sl.WeekDay
	where (al.AttendanceDate >=(bps.DateFrom) OR bps.DateFrom IS NULL)
	AND (al.AttendanceDate <=(bps.DateTo) OR bps.DateTo IS NULL)
	AND al.HR_AttendanceLine_ID = p_attendanceline_id
	order  by al.attendancedate asc,bps.DateFrom,bps.DateTo;

	--IF v_startturn null get default turn
	if(v_startturn IS null) THEN 
	select distinct ON (al.attendancedate) 
	sl.Time1,sl.time4,Tolerance into v_startturn,v_endturn,v_tolerancestart
	from HR_AttendanceLine al
	join GH_ShiftsLine sl on al.WeekDay=sl.WeekDay
	JOIN GH_Shifts s on s.GH_Shifts_ID = sl.GH_Shifts_ID AND s.IsDefault='Y'
	where al.HR_AttendanceLine_ID = p_attendanceline_id
	order  by al.attendancedate asc;
	END IF;

--Query para analizar casos de problemas en marcación de entrada (Time1)    
select Time1,Time4,case when r_requesttype_id > 0 then true else false END,EXTRACT(EPOCH FROM al.time1-v_startturn)/60, r.cds_r_requesttypedetails_ID
into v_starttime,v_endtime,v_hasrequest,v_lateminutes,v_r_subtypeID from HR_AttendanceLine al
left JOIN 
(SELECT CDS_StartDate,CDS_EndDate,r.R_RequestType_ID,r.documentno,r.c_bpartner_id,cds_r_requesttypedetails_ID 
           FROM r_requesttype rt
             JOIN r_request r ON rt.r_requesttype_id = r.r_requesttype_id AND rt.isforattendance = 'Y'::bpchar AND r.R_status_ID IN (SELECT Value::numeric FROM AD_SysConfig sy where sy.Name IN ('CDS_SA_RStatusApprovedByRRHHID','CDS_SA_RStatusApprovedBySup','CDS_SA_RStatusReceivedID')))
as r on date_trunc('day'::text, al.attendancedate) >= date_trunc('day'::text, r.CDS_StartDate) and
date_trunc('day'::text, al.attendancedate) <= date_trunc('day'::text, r.CDS_EndDate) and
(CASE WHEN al.time1 is not null THEN to_char(al.time1, 'HH24:MI') between to_char(CDS_StartDate, 'HH24:MI') AND to_char(CDS_EndDate, 'HH24:MI') ELSE 1=1 END) AND
al.c_bpartner_id = r.c_bpartner_id
WHERE HR_AttendanceLine_id = p_attendanceline_id;

IF(p_returnValue='minutes' AND v_hasrequest is false) THEN
	v_event = (v_lateminutes)::varchar;
	if(v_lateminutes<0 OR v_event::numeric<0 OR (v_lateminutes-1)<v_tolerancestart) THEN
	v_event=null; END IF;
END IF;
IF(p_returnValue='rank')  THEN
	IF(v_lateminutes>v_tolerancestart AND v_lateminutes < 31 AND v_hasrequest is false) THEN
		v_event = 'Menor';
	ELSE IF(v_lateminutes>=31 AND v_lateminutes < 61 AND v_hasrequest is false) THEN
		v_event = 'Mayor';
	ELSE IF(v_lateminutes>=61 AND v_hasrequest is false) THEN
		v_event = 'Superior';        
	END IF;		
    END IF;	
	END IF;
END IF;
IF(p_returnValue='type') THEN
--Ausencia injustificada = No tiene marcación en el dia ni tampoco una solicitud aprobada
	IF(v_starttime is null AND v_endtime is null AND v_hasrequest is false)
		THEN v_event = ' AI';
	ELSE
	--Ausencia justificada = No tiene marcación en el dia y tiene una solicitud aprobada
		IF(v_starttime is null AND v_endtime is null AND v_hasrequest is true)
			THEN v_event = ' AJ';
		ELSE		
		--Falta marcación de entrada = No tiene marcación inicial (No es posible en el sistema)
			IF(v_starttime is null)
				THEN v_event = ' ME';
			END IF;
		--Falta marcación de Salida = No tiene marcación Final ni esta justificada por una solicitud
			IF(v_endtime is null AND (v_hasrequest is false and v_r_subtypeID = 1000000))
				THEN v_event = ' MS';
			END IF;
		END IF;	
		--Tardanza injustificada = tiene marcación posterior a la hora de ingreso segun horario y no esta justificada por una solicitud
		IF(v_starttime is not null AND (v_lateminutes-1)>=v_tolerancestart and v_starttime>v_startturn AND v_hasrequest is false) THEN
			v_event = v_event||' TI';
		--Tardanza justificada = tiene marcación posterior a la hora de ingreso segun horario y no justificada por una solicitud		
		ELSE 
			IF(v_starttime is not null AND (v_lateminutes-1)>=v_tolerancestart and v_starttime>v_startturn AND v_hasrequest is true) THEN
				v_event = v_event||' TJ';
			END IF;
		END IF;
	END IF;
END IF;

--Query para analizar casos de problemas en marcación de salida (Time2,Time3,Time4)    
select Time1,Time4,case when r_requesttype_id > 0 then true else false END,EXTRACT(EPOCH FROM al.time1-v_startturn)/60, r.cds_r_requesttypedetails_ID
into v_starttime,v_endtime,v_hasrequest,v_lateminutes,v_r_subtypeID from HR_AttendanceLine al
left JOIN 
(SELECT CDS_StartDate,CDS_EndDate,r.R_RequestType_ID,r.documentno,r.c_bpartner_id,cds_r_requesttypedetails_ID 
           FROM r_requesttype rt
             JOIN r_request r ON rt.r_requesttype_id = r.r_requesttype_id AND rt.isforattendance = 'Y'::bpchar AND r.R_status_ID IN (SELECT Value::numeric FROM AD_SysConfig sy where sy.Name IN ('CDS_SA_RStatusApprovedByRRHHID','CDS_SA_RStatusApprovedBySup','CDS_SA_RStatusReceivedID')))
as r on al.c_bpartner_id = r.c_bpartner_id
AND date_trunc('day'::text, al.attendancedate)      
	BETWEEN date_trunc('day', r.CDS_StartDate) 
	AND date_trunc('day', r.CDS_EndDate) 
AND (
	CASE WHEN COALESCE(al.time4,al.time3,al.time2) is not null 
		THEN 
		to_char(COALESCE(al.time4,al.time3,al.time2), 'HH24:MI') 
			between to_char(CDS_StartDate, 'HH24:MI') 
			AND to_char(CDS_EndDate, 'HH24:MI') ELSE 1=1 END
	)
WHERE HR_AttendanceLine_id = p_attendanceline_id;

IF(p_returnValue='type') THEN
	--Salida temprana injustificada = tiene marcación de salida previa a la hora de salida segun horario y no justificada por una solicitud			
	IF(v_endtime is not null and v_endtime<v_endturn AND v_hasrequest is false) THEN
		v_event = v_event||' SI';
	--Salida temprana justificada = tiene marcación de salida previa a la hora de salida segun horario y justificada por una solicitud	
	ELSE IF(v_endtime is not null and v_endtime<v_endturn AND v_hasrequest is true) THEN
		v_event = v_event||' SJ';
		END IF;
	END IF;
END IF;

	if(v_event='') THEN
		v_event=null;
		END IF;
RETURN v_event;
END;

$BODY$;

ALTER FUNCTION adempiere.getattendanceevent(numeric, numeric, character varying)
    OWNER TO adempiere;

