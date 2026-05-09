-- View: adempiere.hr_attendanceline_mt_v

-- DROP MATERIALIZED VIEW IF EXISTS adempiere.hr_attendanceline_mt_v;

CREATE MATERIALIZED VIEW IF NOT EXISTS adempiere.hr_attendanceline_mt_v
TABLESPACE pg_default
AS
 SELECT al.hr_attendanceline_id,
    al.ad_client_id,
    al.ad_org_id,
    al.c_bpartner_id,
    al.created,
    al.createdby,
    al.hr_attendanceline_uu,
    al.isactive,
    al.qtyofhours1,
    al.time1,
    al.time2,
    al.updated,
    al.updatedby,
    al.weekday,
    al.time3,
    al.time4,
    al.qtyofhours2,
    al.hr_attendance_id,
    al.totalqtyofhours,
    al.attendancedate,
    al.authorizationtype,
    al.qtyminutesdifference1,
    al.qtyminutesdifference2,
    a.value,
    a.name,
    a.datefrom,
    a.dateto,
    bp.taxid,
    bp.name AS bp_name,
    dep.name AS dep_name,
    job.name AS job_name,
    ( SELECT max(r_requesttype.name::text) AS max
           FROM r_requesttype
             JOIN r_request ON r_requesttype.r_requesttype_id = r_request.r_requesttype_id AND r_requesttype.isforattendance = 'Y'::bpchar AND (r_request.r_status_id IN ( SELECT sy.value::numeric AS value
                   FROM ad_sysconfig sy
                  WHERE sy.name::text = ANY (ARRAY['CDS_SA_RStatusApprovedByRRHHID'::character varying::text, 'CDS_SA_RStatusApprovedBySup'::character varying::text, 'CDS_SA_RStatusReceivedID'::character varying::text])))
          WHERE date_trunc('day'::text, al.attendancedate) >= date_trunc('day'::text, r_request.cds_startdate) AND date_trunc('day'::text, al.attendancedate) <= date_trunc('day'::text, r_request.cds_enddate) AND r_request.c_bpartner_id = al.c_bpartner_id
          GROUP BY (date_trunc('day'::text, al.attendancedate)), r_request.c_bpartner_id) AS requesttype,
    u.ad_user_id,
    weekday_trl.name AS weekday_trl,
    getattendanceevent(bp.c_bpartner_id, al.hr_attendanceline_id, 'minutes'::character varying) AS hr_delayminutes,
    getattendanceevent(bp.c_bpartner_id, al.hr_attendanceline_id, 'rank'::character varying) AS hr_delayrank,
    getattendanceevent(bp.c_bpartner_id, al.hr_attendanceline_id, 'type'::character varying) AS hr_delaytype,
    dep.hr_department_id,
    bp.value AS bpvalue,
        CASE
            WHEN shline.weekday IS NULL OR shline.restday = 'Y'::bpchar THEN 'Y'::text
            ELSE 'N'::text
        END AS isrestday,
        CASE
            WHEN nbd.date1 IS NOT NULL THEN 'Y'::text
            ELSE 'N'::text
        END AS isnonbusinessday
   FROM hr_attendanceline al
     LEFT JOIN hr_attendance a ON a.hr_attendance_id = al.hr_attendance_id
     JOIN c_bpartner bp ON al.c_bpartner_id = bp.c_bpartner_id
     JOIN ( SELECT DISTINCT ON (u_1.c_bpartner_id) u_1.ad_user_id,
            u_1.c_bpartner_id
           FROM ad_user u_1
          WHERE u_1.isinpayroll = 'Y'::bpchar
          ORDER BY u_1.c_bpartner_id, u_1.updated DESC NULLS LAST, u_1.ad_user_id DESC) u ON u.c_bpartner_id = bp.c_bpartner_id
     LEFT JOIN LATERAL ( SELECT emp_1.code,
            emp_1.name,
            emp_1.c_activity_id,
            emp_1.c_bpartner_id,
            emp_1.created,
            emp_1.createdby,
            emp_1.enddate,
            emp_1.hr_department_id,
            emp_1.hr_employee_id,
            emp_1.hr_job_id,
            emp_1.hr_payroll_id,
            emp_1.isactive,
            emp_1.name2,
            emp_1.nationalcode,
            emp_1.sscode,
            emp_1.startdate,
            emp_1.updated,
            emp_1.ad_client_id,
            emp_1.updatedby,
            emp_1.ad_org_id,
            emp_1.imageurl,
            emp_1.hr_employee_uu,
            emp_1.hr_region,
            emp_1.hr_exclude,
            emp_1.hr_contracttype_id,
            emp_1.hr_endreason,
            emp_1.gh_structure_id
           FROM hr_employee emp_1
          WHERE emp_1.c_bpartner_id = bp.c_bpartner_id AND COALESCE(emp_1.startdate, '1900-01-01 00:00:00'::timestamp without time zone) <= al.attendancedate AND COALESCE(emp_1.enddate, '2999-12-31 00:00:00'::timestamp without time zone) >= al.attendancedate
          ORDER BY emp_1.startdate DESC NULLS LAST, emp_1.hr_employee_id DESC
         LIMIT 1) emp ON true
     LEFT JOIN hr_department dep ON dep.hr_department_id = emp.hr_department_id AND dep.isactive = 'Y'::bpchar
     LEFT JOIN hr_job job ON job.hr_job_id = emp.hr_job_id AND job.isactive = 'Y'::bpchar
     LEFT JOIN ad_ref_list ON ad_ref_list.value::bpchar = al.weekday AND ad_ref_list.ad_reference_id = 167::numeric
     LEFT JOIN ad_ref_list_trl weekday_trl ON weekday_trl.ad_ref_list_id = ad_ref_list.ad_ref_list_id AND weekday_trl.ad_language::text = 'es_PA'::text
     LEFT JOIN hr_c_bpartnershifts bpshift ON bpshift.c_bpartner_id = bp.c_bpartner_id AND al.attendancedate >= COALESCE(bpshift.datefrom, '2000-01-01 00:00:00'::timestamp without time zone) AND al.attendancedate <= COALESCE(bpshift.dateto, '2300-01-01 00:00:00'::timestamp without time zone) AND bpshift.isactive = 'Y'::bpchar
     LEFT JOIN gh_shifts gh ON gh.gh_shifts_id = bpshift.gh_shifts_id AND gh.isactive = 'Y'::bpchar
     LEFT JOIN gh_shiftsline shline ON shline.gh_shifts_id = gh.gh_shifts_id AND shline.isactive = 'Y'::bpchar AND shline.weekday = al.weekday
     LEFT JOIN c_nonbusinessday nbd ON nbd.date1 = al.attendancedate AND nbd.isactive = 'Y'::bpchar
  WHERE
        CASE
            WHEN al.totalqtyofhours = 0::numeric THEN 1 =
            CASE
                WHEN shline.weekday IS NULL OR shline.restday = 'Y'::bpchar THEN 0
                ELSE 1
            END
            ELSE 1 = 1
        END AND
        CASE
            WHEN al.totalqtyofhours = 0::numeric THEN 1 =
            CASE
                WHEN nbd.date1 IS NOT NULL THEN 0
                ELSE 1
            END
            ELSE 1 = 1
        END
WITH DATA;

ALTER TABLE IF EXISTS adempiere.hr_attendanceline_mt_v
    OWNER TO adempiere;


CREATE INDEX idx_hr_attendanceline_mt_bp
    ON adempiere.hr_attendanceline_mt_v USING btree
    (c_bpartner_id)
    TABLESPACE pg_default;

CREATE INDEX idx_hr_attendanceline_mt_date
    ON adempiere.hr_attendanceline_mt_v USING btree
    (attendancedate)
    TABLESPACE pg_default;

CREATE INDEX idx_hr_attendanceline_mt_org
    ON adempiere.hr_attendanceline_mt_v USING btree
    (ad_org_id)
    TABLESPACE pg_default;

CREATE INDEX idx_hr_attendanceline_mt_report_order
    ON adempiere.hr_attendanceline_mt_v USING btree
    (hr_department_id, c_bpartner_id, attendancedate)
    TABLESPACE pg_default;