-- stages: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS stages;

CREATE TABLE stages.internship_nature (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(40) NOT NULL,
    label_fr varchar(255),
    label_en varchar(255),
    label_ar varchar(255),
    study_level_id uuid,
    requires_defense boolean NOT NULL DEFAULT false,
    min_weeks integer
);
CREATE INDEX ix_internship_nature_establishment ON stages.internship_nature (establishment_id);
CREATE UNIQUE INDEX uq_internship_nature_1 ON stages.internship_nature (establishment_id, code);

CREATE TABLE stages.company (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    name varchar(255) NOT NULL,
    service varchar(255),
    tax_number varchar(40),
    sector varchar(255),
    address_line1 varchar(255),
    address_line2 varchar(255),
    address_city varchar(255),
    address_postal_code varchar(255),
    address_governorate varchar(255),
    address_country varchar(255),
    phone varchar(30),
    email varchar(255),
    website varchar(255),
    partner boolean NOT NULL DEFAULT false,
    active boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_company_establishment ON stages.company (establishment_id);

CREATE TABLE stages.company_supervisor (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    position varchar(255),
    email varchar(255),
    phone varchar(30),
    company_id uuid NOT NULL REFERENCES stages.company(id)
);
CREATE INDEX ix_company_supervisor_establishment ON stages.company_supervisor (establishment_id);
CREATE INDEX ix_company_supervisor_company_id ON stages.company_supervisor (company_id);

CREATE TABLE stages.defense_session (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    name varchar(255) NOT NULL,
    academic_year_id uuid NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    requests_open_from date,
    requests_open_to date
);
CREATE INDEX ix_defense_session_establishment ON stages.defense_session (establishment_id);

CREATE TABLE stages.internship (
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
    academic_supervisor_id uuid,
    reference_number varchar(40) NOT NULL,
    title varchar(500) NOT NULL,
    objectives text,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status varchar(40) NOT NULL,
    approved_by_user_id uuid,
    approved_at timestamp(6) with time zone,
    refusal_reason varchar(1000),
    defense_requested_at timestamp(6) with time zone,
    nature_id uuid NOT NULL REFERENCES stages.internship_nature(id),
    company_id uuid NOT NULL REFERENCES stages.company(id),
    company_supervisor_id uuid REFERENCES stages.company_supervisor(id),
    requested_session_id uuid REFERENCES stages.defense_session(id)
);
CREATE INDEX ix_internship_establishment ON stages.internship (establishment_id);
CREATE INDEX ix_internship_nature_id ON stages.internship (nature_id);
CREATE INDEX ix_internship_company_id ON stages.internship (company_id);
CREATE INDEX ix_internship_company_supervisor_id ON stages.internship (company_supervisor_id);
CREATE INDEX ix_internship_requested_session_id ON stages.internship (requested_session_id);
CREATE UNIQUE INDEX uq_internship_1 ON stages.internship (establishment_id, reference_number);

CREATE TABLE stages.internship_status_change (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    from_status varchar(40),
    to_status varchar(40) NOT NULL,
    comment varchar(1000),
    internship_id uuid NOT NULL REFERENCES stages.internship(id)
);
CREATE INDEX ix_internship_status_change_establishment ON stages.internship_status_change (establishment_id);
CREATE INDEX ix_internship_status_change_internship_id ON stages.internship_status_change (internship_id);

CREATE TABLE stages.internship_document (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    document_type varchar(40) NOT NULL,
    file_id uuid NOT NULL,
    locked boolean NOT NULL DEFAULT false,
    internship_id uuid NOT NULL REFERENCES stages.internship(id)
);
CREATE INDEX ix_internship_document_establishment ON stages.internship_document (establishment_id);
CREATE INDEX ix_internship_document_internship_id ON stages.internship_document (internship_id);

CREATE TABLE stages.jury (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(80) NOT NULL,
    jury_date date NOT NULL,
    room_id uuid,
    defense_session_id uuid NOT NULL REFERENCES stages.defense_session(id)
);
CREATE INDEX ix_jury_establishment ON stages.jury (establishment_id);
CREATE INDEX ix_jury_defense_session_id ON stages.jury (defense_session_id);
CREATE UNIQUE INDEX uq_jury_1 ON stages.jury (establishment_id, code);

CREATE TABLE stages.jury_member (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    teacher_id uuid,
    external_name varchar(255),
    role varchar(40) NOT NULL,
    jury_id uuid NOT NULL REFERENCES stages.jury(id)
);
CREATE INDEX ix_jury_member_establishment ON stages.jury_member (establishment_id);
CREATE INDEX ix_jury_member_jury_id ON stages.jury_member (jury_id);

CREATE TABLE stages.defense (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    defense_date date NOT NULL,
    start_time time(6) NOT NULL,
    end_time time(6) NOT NULL,
    status varchar(40) NOT NULL,
    grade numeric(5,2),
    mention varchar(40),
    grade_validated boolean NOT NULL DEFAULT false,
    published boolean NOT NULL DEFAULT false,
    published_at timestamp(6) with time zone,
    jury_id uuid NOT NULL REFERENCES stages.jury(id),
    internship_id uuid NOT NULL REFERENCES stages.internship(id)
);
CREATE INDEX ix_defense_establishment ON stages.defense (establishment_id);
CREATE INDEX ix_defense_jury_id ON stages.defense (jury_id);
CREATE INDEX ix_defense_internship_id ON stages.defense (internship_id);
CREATE UNIQUE INDEX uq_defense_1 ON stages.defense (establishment_id, internship_id);

CREATE TABLE stages.defense_evaluation (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    score numeric(5,2),
    comment varchar(1000),
    defense_id uuid NOT NULL REFERENCES stages.defense(id),
    jury_member_id uuid NOT NULL REFERENCES stages.jury_member(id)
);
CREATE INDEX ix_defense_evaluation_establishment ON stages.defense_evaluation (establishment_id);
CREATE INDEX ix_defense_evaluation_defense_id ON stages.defense_evaluation (defense_id);
CREATE INDEX ix_defense_evaluation_jury_member_id ON stages.defense_evaluation (jury_member_id);

-- Exactly one president per jury.
CREATE UNIQUE INDEX uq_jury_member_president ON stages.jury_member (jury_id) WHERE role = 'PRESIDENT';
