-- emploidutemps: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS emploidutemps;

CREATE TABLE emploidutemps.time_slot_template (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    start_time time(6) NOT NULL,
    end_time time(6) NOT NULL,
    order_index integer NOT NULL
);
CREATE INDEX ix_time_slot_template_establishment ON emploidutemps.time_slot_template (establishment_id);
CREATE UNIQUE INDEX uq_time_slot_template_1 ON emploidutemps.time_slot_template (establishment_id, code);

CREATE TABLE emploidutemps.timetable (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    school_class_id uuid NOT NULL,
    academic_period_id uuid NOT NULL,
    name varchar(255),
    valid_from date,
    valid_to date,
    status varchar(40) NOT NULL,
    generation_mode varchar(40) NOT NULL,
    generated_at timestamp(6) with time zone,
    version_number integer
);
CREATE INDEX ix_timetable_establishment ON emploidutemps.timetable (establishment_id);

CREATE TABLE emploidutemps.timetable_slot (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    teaching_assignment_id uuid NOT NULL,
    teacher_id uuid,
    subject_id uuid,
    room_id uuid,
    day_of_week varchar(40) NOT NULL,
    start_time time(6) NOT NULL,
    end_time time(6) NOT NULL,
    week_pattern varchar(40) NOT NULL,
    out_of_control boolean NOT NULL DEFAULT false,
    timetable_id uuid NOT NULL REFERENCES emploidutemps.timetable(id),
    time_slot_template_id uuid REFERENCES emploidutemps.time_slot_template(id)
);
CREATE INDEX ix_timetable_slot_establishment ON emploidutemps.timetable_slot (establishment_id);
CREATE INDEX ix_timetable_slot_timetable_id ON emploidutemps.timetable_slot (timetable_id);
CREATE INDEX ix_timetable_slot_time_slot_template_id ON emploidutemps.timetable_slot (time_slot_template_id);

CREATE TABLE emploidutemps.session_occurrence (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    teaching_assignment_id uuid NOT NULL,
    teacher_id uuid,
    subject_id uuid,
    school_class_id uuid NOT NULL,
    room_id uuid,
    session_date date NOT NULL,
    start_time time(6) NOT NULL,
    end_time time(6) NOT NULL,
    session_type varchar(40) NOT NULL,
    status varchar(40) NOT NULL,
    make_up boolean NOT NULL DEFAULT false,
    make_up_request_id uuid,
    cancellation_reason varchar(500),
    slot_id uuid REFERENCES emploidutemps.timetable_slot(id),
    make_up_of_id uuid REFERENCES emploidutemps.session_occurrence(id)
);
CREATE INDEX ix_session_occurrence_establishment ON emploidutemps.session_occurrence (establishment_id);
CREATE INDEX ix_session_occurrence_slot_id ON emploidutemps.session_occurrence (slot_id);
CREATE INDEX ix_session_occurrence_make_up_of_id ON emploidutemps.session_occurrence (make_up_of_id);

CREATE TABLE emploidutemps.scheduling_rule (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    name varchar(255) NOT NULL,
    rule_type varchar(40) NOT NULL,
    parameters_json text,
    weight integer,
    hard_constraint boolean NOT NULL DEFAULT false,
    study_level_id uuid,
    school_class_id uuid,
    teacher_id uuid,
    active boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_scheduling_rule_establishment ON emploidutemps.scheduling_rule (establishment_id);

CREATE INDEX ix_session_occurrence_class_date ON emploidutemps.session_occurrence (school_class_id, session_date);
CREATE INDEX ix_session_occurrence_teacher_date ON emploidutemps.session_occurrence (teacher_id, session_date);
