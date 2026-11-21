-- pedagogie: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS pedagogie;

CREATE TABLE pedagogie.teacher_grade (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    label_fr varchar(255),
    label_en varchar(255),
    label_ar varchar(255)
);
CREATE INDEX ix_teacher_grade_establishment ON pedagogie.teacher_grade (establishment_id);
CREATE UNIQUE INDEX uq_teacher_grade_1 ON pedagogie.teacher_grade (establishment_id, code);

CREATE TABLE pedagogie.teacher (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    person_first_name varchar(255),
    person_last_name varchar(255),
    person_first_name_ar varchar(255),
    person_last_name_ar varchar(255),
    person_gender varchar(255),
    person_birth_date date,
    person_birth_place varchar(255),
    person_nationality varchar(255),
    person_national_id_number varchar(255),
    person_passport_number varchar(255),
    person_email varchar(255),
    person_phone varchar(255),
    person_mobile varchar(255),
    person_address_line1 varchar(255),
    person_address_line2 varchar(255),
    person_address_city varchar(255),
    person_address_postal_code varchar(255),
    person_address_governorate varchar(255),
    person_address_country varchar(255),
    person_photo_file_id uuid,
    employee_number varchar(40),
    department_id uuid,
    employment_type varchar(40) NOT NULL,
    hire_date date,
    hourly_rate_amount numeric(14,3),
    hourly_rate_currency varchar(3),
    statutory_hours integer,
    max_weekly_hours integer,
    bank_account_iban varchar(40),
    department_head boolean NOT NULL DEFAULT false,
    active boolean NOT NULL DEFAULT false,
    grade_id uuid NOT NULL REFERENCES pedagogie.teacher_grade(id)
);
CREATE INDEX ix_teacher_establishment ON pedagogie.teacher (establishment_id);
CREATE INDEX ix_teacher_grade_id ON pedagogie.teacher (grade_id);
CREATE UNIQUE INDEX uq_teacher_1 ON pedagogie.teacher (establishment_id, employee_number);

CREATE TABLE pedagogie.teacher_domain (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    teaching_domain_id uuid NOT NULL,
    primary_domain boolean NOT NULL DEFAULT false,
    teacher_id uuid NOT NULL REFERENCES pedagogie.teacher(id)
);
CREATE INDEX ix_teacher_domain_establishment ON pedagogie.teacher_domain (establishment_id);
CREATE INDEX ix_teacher_domain_teacher_id ON pedagogie.teacher_domain (teacher_id);
CREATE UNIQUE INDEX uq_teacher_domain_1 ON pedagogie.teacher_domain (establishment_id, teacher_id, teaching_domain_id);

CREATE TABLE pedagogie.subject (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(40) NOT NULL,
    name_fr varchar(255),
    name_en varchar(255),
    name_ar varchar(255),
    description varchar(1000),
    teaching_domain_id uuid,
    default_coefficient numeric(6,2),
    default_credits integer,
    active boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_subject_establishment ON pedagogie.subject (establishment_id);
CREATE UNIQUE INDEX uq_subject_1 ON pedagogie.subject (establishment_id, code);

CREATE TABLE pedagogie.study_plan (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(80) NOT NULL,
    description varchar(255),
    study_level_id uuid NOT NULL,
    semester_number integer NOT NULL,
    status varchar(40) NOT NULL
);
CREATE INDEX ix_study_plan_establishment ON pedagogie.study_plan (establishment_id);
CREATE UNIQUE INDEX uq_study_plan_1 ON pedagogie.study_plan (establishment_id, code);

CREATE TABLE pedagogie.teaching_unit (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(40) NOT NULL,
    name_fr varchar(255),
    name_en varchar(255),
    name_ar varchar(255),
    coefficient numeric(6,2),
    credits integer,
    order_index integer NOT NULL,
    study_plan_id uuid NOT NULL REFERENCES pedagogie.study_plan(id)
);
CREATE INDEX ix_teaching_unit_establishment ON pedagogie.teaching_unit (establishment_id);
CREATE INDEX ix_teaching_unit_study_plan_id ON pedagogie.teaching_unit (study_plan_id);

CREATE TABLE pedagogie.study_plan_subject (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    coefficient numeric(6,2),
    credits integer,
    lecture_hours integer,
    tutorial_hours integer,
    lab_hours integer,
    project_hours integer,
    teaching_unit_id uuid NOT NULL REFERENCES pedagogie.teaching_unit(id),
    subject_id uuid NOT NULL REFERENCES pedagogie.subject(id)
);
CREATE INDEX ix_study_plan_subject_establishment ON pedagogie.study_plan_subject (establishment_id);
CREATE INDEX ix_study_plan_subject_teaching_unit_id ON pedagogie.study_plan_subject (teaching_unit_id);
CREATE INDEX ix_study_plan_subject_subject_id ON pedagogie.study_plan_subject (subject_id);
CREATE UNIQUE INDEX uq_study_plan_subject_1 ON pedagogie.study_plan_subject (establishment_id, teaching_unit_id, subject_id);

CREATE TABLE pedagogie.subject_assessment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    assessment_type varchar(40) NOT NULL,
    weight_percent numeric(5,2) NOT NULL,
    max_score numeric(5,2) NOT NULL,
    study_plan_subject_id uuid NOT NULL REFERENCES pedagogie.study_plan_subject(id)
);
CREATE INDEX ix_subject_assessment_establishment ON pedagogie.subject_assessment (establishment_id);
CREATE INDEX ix_subject_assessment_study_plan_subject_id ON pedagogie.subject_assessment (study_plan_subject_id);

CREATE TABLE pedagogie.teacher_workload (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    academic_year_id uuid NOT NULL,
    statutory_hours integer,
    assigned_hours integer,
    status varchar(40) NOT NULL,
    teacher_id uuid NOT NULL REFERENCES pedagogie.teacher(id)
);
CREATE INDEX ix_teacher_workload_establishment ON pedagogie.teacher_workload (establishment_id);
CREATE INDEX ix_teacher_workload_teacher_id ON pedagogie.teacher_workload (teacher_id);
CREATE UNIQUE INDEX uq_teacher_workload_1 ON pedagogie.teacher_workload (establishment_id, teacher_id, academic_year_id);

CREATE TABLE pedagogie.teaching_assignment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    school_class_id uuid NOT NULL,
    academic_year_id uuid NOT NULL,
    session_type varchar(40) NOT NULL,
    planned_hours integer,
    study_plan_subject_id uuid NOT NULL REFERENCES pedagogie.study_plan_subject(id),
    teacher_id uuid REFERENCES pedagogie.teacher(id),
    workload_id uuid REFERENCES pedagogie.teacher_workload(id)
);
CREATE INDEX ix_teaching_assignment_establishment ON pedagogie.teaching_assignment (establishment_id);
CREATE INDEX ix_teaching_assignment_study_plan_subject_id ON pedagogie.teaching_assignment (study_plan_subject_id);
CREATE INDEX ix_teaching_assignment_teacher_id ON pedagogie.teaching_assignment (teacher_id);
CREATE INDEX ix_teaching_assignment_workload_id ON pedagogie.teaching_assignment (workload_id);

CREATE TABLE pedagogie.teacher_subject_preference (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    academic_year_id uuid NOT NULL,
    study_level_id uuid,
    priority integer,
    comment varchar(1000),
    teacher_id uuid NOT NULL REFERENCES pedagogie.teacher(id),
    subject_id uuid NOT NULL REFERENCES pedagogie.subject(id)
);
CREATE INDEX ix_teacher_subject_preference_establishment ON pedagogie.teacher_subject_preference (establishment_id);
CREATE INDEX ix_teacher_subject_preference_teacher_id ON pedagogie.teacher_subject_preference (teacher_id);
CREATE INDEX ix_teacher_subject_preference_subject_id ON pedagogie.teacher_subject_preference (subject_id);

CREATE TABLE pedagogie.teacher_availability_preference (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    academic_year_id uuid NOT NULL,
    day_of_week varchar(40) NOT NULL,
    start_time time(6) NOT NULL,
    end_time time(6) NOT NULL,
    preference_level varchar(40) NOT NULL,
    teacher_id uuid NOT NULL REFERENCES pedagogie.teacher(id)
);
CREATE INDEX ix_teacher_availability_preference_establishment ON pedagogie.teacher_availability_preference (establishment_id);
CREATE INDEX ix_teacher_availability_preference_teacher_id ON pedagogie.teacher_availability_preference (teacher_id);

CREATE TABLE pedagogie.teaching_program (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    title varchar(255) NOT NULL,
    objectives text,
    prerequisites text,
    bibliography text,
    version_number integer,
    study_plan_subject_id uuid NOT NULL REFERENCES pedagogie.study_plan_subject(id)
);
CREATE INDEX ix_teaching_program_establishment ON pedagogie.teaching_program (establishment_id);
CREATE INDEX ix_teaching_program_study_plan_subject_id ON pedagogie.teaching_program (study_plan_subject_id);

CREATE TABLE pedagogie.program_chapter (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    order_index integer NOT NULL,
    title varchar(255) NOT NULL,
    content text,
    planned_hours integer,
    program_id uuid NOT NULL REFERENCES pedagogie.teaching_program(id)
);
CREATE INDEX ix_program_chapter_establishment ON pedagogie.program_chapter (establishment_id);
CREATE INDEX ix_program_chapter_program_id ON pedagogie.program_chapter (program_id);

CREATE TABLE pedagogie.progress_entry (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    planned_date date,
    actual_date date,
    status varchar(40) NOT NULL,
    completion_percent integer,
    comment varchar(1000),
    teaching_assignment_id uuid NOT NULL REFERENCES pedagogie.teaching_assignment(id),
    chapter_id uuid NOT NULL REFERENCES pedagogie.program_chapter(id)
);
CREATE INDEX ix_progress_entry_establishment ON pedagogie.progress_entry (establishment_id);
CREATE INDEX ix_progress_entry_teaching_assignment_id ON pedagogie.progress_entry (teaching_assignment_id);
CREATE INDEX ix_progress_entry_chapter_id ON pedagogie.progress_entry (chapter_id);

CREATE TABLE pedagogie.course_journal_entry (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    session_occurrence_id uuid NOT NULL,
    content text NOT NULL,
    confirmed boolean NOT NULL DEFAULT false,
    confirmed_by_user_id uuid,
    confirmed_at timestamp(6) with time zone,
    teaching_assignment_id uuid NOT NULL REFERENCES pedagogie.teaching_assignment(id),
    chapter_id uuid REFERENCES pedagogie.program_chapter(id)
);
CREATE INDEX ix_course_journal_entry_establishment ON pedagogie.course_journal_entry (establishment_id);
CREATE INDEX ix_course_journal_entry_teaching_assignment_id ON pedagogie.course_journal_entry (teaching_assignment_id);
CREATE INDEX ix_course_journal_entry_chapter_id ON pedagogie.course_journal_entry (chapter_id);
CREATE UNIQUE INDEX uq_course_journal_entry_1 ON pedagogie.course_journal_entry (establishment_id, session_occurrence_id);

CREATE TABLE pedagogie.homework (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    session_occurrence_id uuid,
    title varchar(255) NOT NULL,
    description text,
    assigned_date date NOT NULL,
    due_date date,
    attachment_file_id uuid,
    confirmed boolean NOT NULL DEFAULT false,
    teaching_assignment_id uuid NOT NULL REFERENCES pedagogie.teaching_assignment(id)
);
CREATE INDEX ix_homework_establishment ON pedagogie.homework (establishment_id);
CREATE INDEX ix_homework_teaching_assignment_id ON pedagogie.homework (teaching_assignment_id);

CREATE TABLE pedagogie.course_material (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    title varchar(255) NOT NULL,
    material_type varchar(40) NOT NULL,
    description varchar(1000),
    file_id uuid NOT NULL,
    published_at timestamp(6) with time zone,
    visible_to_students boolean NOT NULL DEFAULT false,
    teaching_assignment_id uuid NOT NULL REFERENCES pedagogie.teaching_assignment(id)
);
CREATE INDEX ix_course_material_establishment ON pedagogie.course_material (establishment_id);
CREATE INDEX ix_course_material_teaching_assignment_id ON pedagogie.course_material (teaching_assignment_id);

CREATE TABLE pedagogie.make_up_request (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    original_occurrence_id uuid NOT NULL,
    make_up_occurrence_id uuid,
    requested_date date NOT NULL,
    start_time time(6) NOT NULL,
    end_time time(6) NOT NULL,
    room_id uuid,
    reason varchar(1000),
    status varchar(40) NOT NULL,
    decided_by_user_id uuid,
    decided_at timestamp(6) with time zone,
    teaching_assignment_id uuid NOT NULL REFERENCES pedagogie.teaching_assignment(id),
    teacher_id uuid NOT NULL REFERENCES pedagogie.teacher(id)
);
CREATE INDEX ix_make_up_request_establishment ON pedagogie.make_up_request (establishment_id);
CREATE INDEX ix_make_up_request_teaching_assignment_id ON pedagogie.make_up_request (teaching_assignment_id);
CREATE INDEX ix_make_up_request_teacher_id ON pedagogie.make_up_request (teacher_id);

CREATE TABLE pedagogie.teacher_meeting_slot (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    day_of_week varchar(40),
    specific_date date,
    start_time time(6) NOT NULL,
    end_time time(6) NOT NULL,
    location varchar(255),
    capacity integer,
    active boolean NOT NULL DEFAULT false,
    teacher_id uuid NOT NULL REFERENCES pedagogie.teacher(id)
);
CREATE INDEX ix_teacher_meeting_slot_establishment ON pedagogie.teacher_meeting_slot (establishment_id);
CREATE INDEX ix_teacher_meeting_slot_teacher_id ON pedagogie.teacher_meeting_slot (teacher_id);

CREATE TABLE pedagogie.meeting_request (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    requester_type varchar(40) NOT NULL,
    requester_person_id uuid NOT NULL,
    student_id uuid,
    meeting_date date NOT NULL,
    topic varchar(500),
    status varchar(40) NOT NULL,
    note varchar(1000),
    slot_id uuid NOT NULL REFERENCES pedagogie.teacher_meeting_slot(id)
);
CREATE INDEX ix_meeting_request_establishment ON pedagogie.meeting_request (establishment_id);
CREATE INDEX ix_meeting_request_slot_id ON pedagogie.meeting_request (slot_id);

CREATE TABLE pedagogie.observation (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid,
    school_class_id uuid,
    category varchar(40) NOT NULL,
    content text NOT NULL,
    observed_on date NOT NULL,
    visibility varchar(40) NOT NULL,
    teacher_id uuid REFERENCES pedagogie.teacher(id)
);
CREATE INDEX ix_observation_establishment ON pedagogie.observation (establishment_id);
CREATE INDEX ix_observation_teacher_id ON pedagogie.observation (teacher_id);

CREATE TABLE pedagogie.continuous_training (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    title varchar(255) NOT NULL,
    provider varchar(255),
    description text,
    start_date date,
    end_date date,
    hours integer,
    status varchar(40) NOT NULL
);
CREATE INDEX ix_continuous_training_establishment ON pedagogie.continuous_training (establishment_id);

CREATE TABLE pedagogie.training_participation (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    attended boolean NOT NULL DEFAULT false,
    certificate_file_id uuid,
    training_id uuid NOT NULL REFERENCES pedagogie.continuous_training(id),
    teacher_id uuid NOT NULL REFERENCES pedagogie.teacher(id)
);
CREATE INDEX ix_training_participation_establishment ON pedagogie.training_participation (establishment_id);
CREATE INDEX ix_training_participation_training_id ON pedagogie.training_participation (training_id);
CREATE INDEX ix_training_participation_teacher_id ON pedagogie.training_participation (teacher_id);
CREATE UNIQUE INDEX uq_training_participation_1 ON pedagogie.training_participation (establishment_id, training_id, teacher_id);
