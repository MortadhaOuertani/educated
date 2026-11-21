-- assiduite: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS assiduite;

CREATE TABLE assiduite.attendance_policy (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    study_level_id uuid,
    at_risk_percent numeric(5,2) NOT NULL,
    critical_percent numeric(5,2) NOT NULL,
    call_deadline_minutes integer NOT NULL,
    edit_window_hours integer NOT NULL,
    count_only_confirmed_calls boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_attendance_policy_establishment ON assiduite.attendance_policy (establishment_id);

CREATE TABLE assiduite.student_absence_request (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    from_date_time timestamp(6) NOT NULL,
    to_date_time timestamp(6) NOT NULL,
    reason varchar(1000) NOT NULL,
    proof_file_id uuid,
    status varchar(40) NOT NULL,
    reviewed_by_user_id uuid,
    reviewed_at timestamp(6) with time zone
);
CREATE INDEX ix_student_absence_request_establishment ON assiduite.student_absence_request (establishment_id);

CREATE TABLE assiduite.attendance_sheet (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    session_occurrence_id uuid NOT NULL,
    school_class_id uuid NOT NULL,
    teacher_id uuid,
    status varchar(40) NOT NULL,
    confirmed_at timestamp(6) with time zone,
    editable_until timestamp(6) with time zone
);
CREATE INDEX ix_attendance_sheet_establishment ON assiduite.attendance_sheet (establishment_id);
CREATE UNIQUE INDEX uq_attendance_sheet_1 ON assiduite.attendance_sheet (establishment_id, session_occurrence_id);

CREATE TABLE assiduite.attendance_record (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    status varchar(40) NOT NULL,
    late_minutes integer,
    justified boolean NOT NULL DEFAULT false,
    comment varchar(500),
    guardians_notified_at timestamp(6) with time zone,
    sheet_id uuid NOT NULL REFERENCES assiduite.attendance_sheet(id),
    justified_by_id uuid REFERENCES assiduite.student_absence_request(id)
);
CREATE INDEX ix_attendance_record_establishment ON assiduite.attendance_record (establishment_id);
CREATE INDEX ix_attendance_record_sheet_id ON assiduite.attendance_record (sheet_id);
CREATE INDEX ix_attendance_record_justified_by_id ON assiduite.attendance_record (justified_by_id);
CREATE UNIQUE INDEX uq_attendance_record_1 ON assiduite.attendance_record (establishment_id, sheet_id, student_id);

CREATE TABLE assiduite.teacher_attendance (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    session_occurrence_id uuid NOT NULL,
    teacher_id uuid NOT NULL,
    session_date date NOT NULL,
    week_number integer,
    status varchar(40) NOT NULL,
    hours numeric(5,2) NOT NULL,
    validated boolean NOT NULL DEFAULT false,
    validated_by_user_id uuid,
    validated_at timestamp(6) with time zone
);
CREATE INDEX ix_teacher_attendance_establishment ON assiduite.teacher_attendance (establishment_id);
CREATE UNIQUE INDEX uq_teacher_attendance_1 ON assiduite.teacher_attendance (establishment_id, session_occurrence_id);

CREATE INDEX ix_attendance_record_student ON assiduite.attendance_record (student_id);
