-- parametrage: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS parametrage;

CREATE TABLE parametrage.establishment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    code varchar(20) NOT NULL UNIQUE,
    name_fr varchar(255),
    name_en varchar(255),
    name_ar varchar(255),
    legal_name varchar(255),
    tax_number varchar(40),
    address_line1 varchar(255),
    address_line2 varchar(255),
    address_city varchar(255),
    address_postal_code varchar(255),
    address_governorate varchar(255),
    address_country varchar(255),
    email varchar(255),
    phone varchar(30),
    website varchar(255),
    logo_file_id uuid,
    timezone varchar(40) NOT NULL,
    default_locale varchar(5) NOT NULL,
    active boolean NOT NULL DEFAULT false
);

CREATE TABLE parametrage.site (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    name varchar(255) NOT NULL,
    address_line1 varchar(255),
    address_line2 varchar(255),
    address_city varchar(255),
    address_postal_code varchar(255),
    address_governorate varchar(255),
    address_country varchar(255)
);
CREATE INDEX ix_site_establishment ON parametrage.site (establishment_id);
CREATE UNIQUE INDEX uq_site_1 ON parametrage.site (establishment_id, code);

CREATE TABLE parametrage.building (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    name varchar(255) NOT NULL,
    site_id uuid NOT NULL REFERENCES parametrage.site(id)
);
CREATE INDEX ix_building_establishment ON parametrage.building (establishment_id);
CREATE INDEX ix_building_site_id ON parametrage.building (site_id);
CREATE UNIQUE INDEX uq_building_1 ON parametrage.building (establishment_id, code);

CREATE TABLE parametrage.room (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    name varchar(255),
    capacity integer,
    room_type varchar(40) NOT NULL,
    floor integer,
    equipment varchar(500),
    active boolean NOT NULL DEFAULT false,
    building_id uuid REFERENCES parametrage.building(id)
);
CREATE INDEX ix_room_establishment ON parametrage.room (establishment_id);
CREATE INDEX ix_room_building_id ON parametrage.room (building_id);
CREATE UNIQUE INDEX uq_room_1 ON parametrage.room (establishment_id, code);

CREATE TABLE parametrage.academic_year (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status varchar(40) NOT NULL,
    current boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_academic_year_establishment ON parametrage.academic_year (establishment_id);
CREATE UNIQUE INDEX uq_academic_year_1 ON parametrage.academic_year (establishment_id, code);

CREATE TABLE parametrage.academic_period (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    name_fr varchar(255),
    name_en varchar(255),
    name_ar varchar(255),
    period_type varchar(40) NOT NULL,
    order_index integer NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    current boolean NOT NULL DEFAULT false,
    academic_year_id uuid NOT NULL REFERENCES parametrage.academic_year(id)
);
CREATE INDEX ix_academic_period_establishment ON parametrage.academic_period (establishment_id);
CREATE INDEX ix_academic_period_academic_year_id ON parametrage.academic_period (academic_year_id);
CREATE UNIQUE INDEX uq_academic_period_1 ON parametrage.academic_period (establishment_id, academic_year_id, code);

CREATE TABLE parametrage.department (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    name_fr varchar(255),
    name_en varchar(255),
    name_ar varchar(255)
);
CREATE INDEX ix_department_establishment ON parametrage.department (establishment_id);
CREATE UNIQUE INDEX uq_department_1 ON parametrage.department (establishment_id, code);

CREATE TABLE parametrage.cycle (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    name_fr varchar(255),
    name_en varchar(255),
    name_ar varchar(255),
    duration_years integer
);
CREATE INDEX ix_cycle_establishment ON parametrage.cycle (establishment_id);
CREATE UNIQUE INDEX uq_cycle_1 ON parametrage.cycle (establishment_id, code);

CREATE TABLE parametrage.program (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    name_fr varchar(255),
    name_en varchar(255),
    name_ar varchar(255),
    diploma_title varchar(255),
    active boolean NOT NULL DEFAULT false,
    department_id uuid REFERENCES parametrage.department(id)
);
CREATE INDEX ix_program_establishment ON parametrage.program (establishment_id);
CREATE INDEX ix_program_department_id ON parametrage.program (department_id);
CREATE UNIQUE INDEX uq_program_1 ON parametrage.program (establishment_id, code);

CREATE TABLE parametrage.specialization (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    name_fr varchar(255),
    name_en varchar(255),
    name_ar varchar(255),
    program_id uuid NOT NULL REFERENCES parametrage.program(id)
);
CREATE INDEX ix_specialization_establishment ON parametrage.specialization (establishment_id);
CREATE INDEX ix_specialization_program_id ON parametrage.specialization (program_id);
CREATE UNIQUE INDEX uq_specialization_1 ON parametrage.specialization (establishment_id, code);

CREATE TABLE parametrage.study_level (
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
    year_of_study integer NOT NULL,
    rhythm varchar(40) NOT NULL,
    active boolean NOT NULL DEFAULT false,
    program_id uuid NOT NULL REFERENCES parametrage.program(id),
    cycle_id uuid NOT NULL REFERENCES parametrage.cycle(id),
    specialization_id uuid REFERENCES parametrage.specialization(id)
);
CREATE INDEX ix_study_level_establishment ON parametrage.study_level (establishment_id);
CREATE INDEX ix_study_level_program_id ON parametrage.study_level (program_id);
CREATE INDEX ix_study_level_cycle_id ON parametrage.study_level (cycle_id);
CREATE INDEX ix_study_level_specialization_id ON parametrage.study_level (specialization_id);
CREATE UNIQUE INDEX uq_study_level_1 ON parametrage.study_level (establishment_id, code);

CREATE TABLE parametrage.school_class (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(40) NOT NULL,
    name varchar(255),
    capacity integer,
    active boolean NOT NULL DEFAULT false,
    study_level_id uuid NOT NULL REFERENCES parametrage.study_level(id),
    academic_year_id uuid NOT NULL REFERENCES parametrage.academic_year(id),
    site_id uuid REFERENCES parametrage.site(id)
);
CREATE INDEX ix_school_class_establishment ON parametrage.school_class (establishment_id);
CREATE INDEX ix_school_class_study_level_id ON parametrage.school_class (study_level_id);
CREATE INDEX ix_school_class_academic_year_id ON parametrage.school_class (academic_year_id);
CREATE INDEX ix_school_class_site_id ON parametrage.school_class (site_id);
CREATE UNIQUE INDEX uq_school_class_1 ON parametrage.school_class (establishment_id, academic_year_id, code);

CREATE TABLE parametrage.calendar_event (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    title_fr varchar(255),
    title_en varchar(255),
    title_ar varchar(255),
    event_type varchar(40) NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    blocks_classes boolean NOT NULL DEFAULT false,
    academic_year_id uuid NOT NULL REFERENCES parametrage.academic_year(id)
);
CREATE INDEX ix_calendar_event_establishment ON parametrage.calendar_event (establishment_id);
CREATE INDEX ix_calendar_event_academic_year_id ON parametrage.calendar_event (academic_year_id);

CREATE TABLE parametrage.teaching_domain (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    code varchar(20) NOT NULL,
    name_fr varchar(255),
    name_en varchar(255),
    name_ar varchar(255)
);
CREATE INDEX ix_teaching_domain_establishment ON parametrage.teaching_domain (establishment_id);
CREATE UNIQUE INDEX uq_teaching_domain_1 ON parametrage.teaching_domain (establishment_id, code);

CREATE TABLE parametrage.feature_toggle (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    feature_key varchar(80) NOT NULL,
    portal varchar(40) NOT NULL,
    enabled boolean NOT NULL DEFAULT false,
    disabled_message varchar(500)
);
CREATE INDEX ix_feature_toggle_establishment ON parametrage.feature_toggle (establishment_id);
CREATE UNIQUE INDEX uq_feature_toggle_1 ON parametrage.feature_toggle (establishment_id, feature_key, portal);

CREATE TABLE parametrage.calendar_event_level (
    calendar_event_id uuid NOT NULL REFERENCES parametrage.calendar_event(id) ON DELETE CASCADE,
    study_level_id uuid NOT NULL REFERENCES parametrage.study_level(id),
    PRIMARY KEY (calendar_event_id, study_level_id)
);
