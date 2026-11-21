-- examens: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS examens;

CREATE TABLE examens.appreciation_scale (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    label_fr varchar(255),
    label_en varchar(255),
    label_ar varchar(255),
    min_average numeric(5,2) NOT NULL,
    max_average numeric(5,2) NOT NULL
);
CREATE INDEX ix_appreciation_scale_establishment ON examens.appreciation_scale (establishment_id);

CREATE TABLE examens.exam_session (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    name varchar(255) NOT NULL,
    session_type varchar(40) NOT NULL,
    academic_period_id uuid NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    published boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_exam_session_establishment ON examens.exam_session (establishment_id);

CREATE TABLE examens.exam (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    study_plan_subject_id uuid NOT NULL,
    assessment_type varchar(40) NOT NULL,
    exam_date date NOT NULL,
    start_time time(6) NOT NULL,
    end_time time(6) NOT NULL,
    duration_minutes integer,
    published boolean NOT NULL DEFAULT false,
    rooms_published boolean NOT NULL DEFAULT false,
    exam_session_id uuid NOT NULL REFERENCES examens.exam_session(id)
);
CREATE INDEX ix_exam_establishment ON examens.exam (establishment_id);
CREATE INDEX ix_exam_exam_session_id ON examens.exam (exam_session_id);

CREATE TABLE examens.exam_class (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    school_class_id uuid NOT NULL,
    exam_id uuid NOT NULL REFERENCES examens.exam(id)
);
CREATE INDEX ix_exam_class_establishment ON examens.exam_class (establishment_id);
CREATE INDEX ix_exam_class_exam_id ON examens.exam_class (exam_id);
CREATE UNIQUE INDEX uq_exam_class_1 ON examens.exam_class (establishment_id, exam_id, school_class_id);

CREATE TABLE examens.exam_room_allocation (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    room_id uuid NOT NULL,
    school_class_id uuid,
    seat_count integer,
    seating_range varchar(100),
    exam_id uuid NOT NULL REFERENCES examens.exam(id)
);
CREATE INDEX ix_exam_room_allocation_establishment ON examens.exam_room_allocation (establishment_id);
CREATE INDEX ix_exam_room_allocation_exam_id ON examens.exam_room_allocation (exam_id);

CREATE TABLE examens.invigilation (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    teacher_id uuid NOT NULL,
    room_id uuid,
    role varchar(40) NOT NULL,
    published boolean NOT NULL DEFAULT false,
    exam_id uuid NOT NULL REFERENCES examens.exam(id)
);
CREATE INDEX ix_invigilation_establishment ON examens.invigilation (establishment_id);
CREATE INDEX ix_invigilation_exam_id ON examens.invigilation (exam_id);

CREATE TABLE examens.grade_grid (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    study_plan_subject_id uuid NOT NULL,
    school_class_id uuid NOT NULL,
    academic_period_id uuid NOT NULL,
    teacher_id uuid,
    assessment_type varchar(40) NOT NULL,
    max_score numeric(5,2) NOT NULL,
    status varchar(40) NOT NULL,
    confirmed_at timestamp(6) with time zone,
    published_at timestamp(6) with time zone,
    exam_id uuid REFERENCES examens.exam(id)
);
CREATE INDEX ix_grade_grid_establishment ON examens.grade_grid (establishment_id);
CREATE INDEX ix_grade_grid_exam_id ON examens.grade_grid (exam_id);
CREATE UNIQUE INDEX uq_grade_grid_1 ON examens.grade_grid (establishment_id, study_plan_subject_id, school_class_id, academic_period_id, assessment_type);

CREATE TABLE examens.grade_entry (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    score numeric(5,2),
    reason varchar(40),
    comment varchar(500),
    grid_id uuid NOT NULL REFERENCES examens.grade_grid(id)
);
CREATE INDEX ix_grade_entry_establishment ON examens.grade_entry (establishment_id);
CREATE INDEX ix_grade_entry_grid_id ON examens.grade_entry (grid_id);
CREATE UNIQUE INDEX uq_grade_entry_1 ON examens.grade_entry (establishment_id, grid_id, student_id);

CREATE TABLE examens.report_card (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    academic_period_id uuid NOT NULL,
    average numeric(5,2),
    rank_in_class integer,
    credits_earned integer,
    appreciation_text varchar(255),
    pdf_file_id uuid,
    generated_at timestamp(6) with time zone,
    published boolean NOT NULL DEFAULT false,
    appreciation_scale_id uuid REFERENCES examens.appreciation_scale(id)
);
CREATE INDEX ix_report_card_establishment ON examens.report_card (establishment_id);
CREATE INDEX ix_report_card_appreciation_scale_id ON examens.report_card (appreciation_scale_id);
CREATE UNIQUE INDEX uq_report_card_1 ON examens.report_card (establishment_id, student_id, academic_period_id);

CREATE TABLE examens.subject_result (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    study_plan_subject_id uuid NOT NULL,
    academic_period_id uuid NOT NULL,
    average numeric(5,2),
    credits_earned integer,
    validated boolean NOT NULL DEFAULT false,
    result_session varchar(40) NOT NULL,
    report_card_id uuid REFERENCES examens.report_card(id)
);
CREATE INDEX ix_subject_result_establishment ON examens.subject_result (establishment_id);
CREATE INDEX ix_subject_result_report_card_id ON examens.subject_result (report_card_id);

CREATE TABLE examens.class_decision (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    academic_year_id uuid NOT NULL,
    decision varchar(40) NOT NULL,
    jury_date date,
    comment varchar(1000)
);
CREATE INDEX ix_class_decision_establishment ON examens.class_decision (establishment_id);
CREATE UNIQUE INDEX uq_class_decision_1 ON examens.class_decision (establishment_id, student_id, academic_year_id);

-- A grade has either a score or a reason, never both and never neither.
ALTER TABLE examens.grade_entry ADD CONSTRAINT ck_grade_entry_score_or_reason
    CHECK ((score IS NULL) <> (reason IS NULL));
