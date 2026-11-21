-- vieetablissement: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS vieetablissement;

CREATE TABLE vieetablissement.survey (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    title varchar(255) NOT NULL,
    description text,
    purpose varchar(40) NOT NULL,
    audience varchar(40) NOT NULL,
    teaching_assignment_id uuid,
    opens_at timestamp(6) with time zone,
    closes_at timestamp(6) with time zone,
    anonymous boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_survey_establishment ON vieetablissement.survey (establishment_id);

CREATE TABLE vieetablissement.survey_question (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    order_index integer NOT NULL,
    text varchar(1000) NOT NULL,
    question_type varchar(40) NOT NULL,
    options_json text,
    required boolean NOT NULL DEFAULT false,
    survey_id uuid NOT NULL REFERENCES vieetablissement.survey(id)
);
CREATE INDEX ix_survey_question_establishment ON vieetablissement.survey_question (establishment_id);
CREATE INDEX ix_survey_question_survey_id ON vieetablissement.survey_question (survey_id);

CREATE TABLE vieetablissement.survey_response (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    respondent_user_id uuid,
    answers_json text NOT NULL,
    survey_id uuid NOT NULL REFERENCES vieetablissement.survey(id)
);
CREATE INDEX ix_survey_response_establishment ON vieetablissement.survey_response (establishment_id);
CREATE INDEX ix_survey_response_survey_id ON vieetablissement.survey_response (survey_id);

CREATE TABLE vieetablissement.canteen_menu (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    menu_date date NOT NULL,
    meal_type varchar(40) NOT NULL,
    items text,
    price_amount numeric(14,3),
    price_currency varchar(3)
);
CREATE INDEX ix_canteen_menu_establishment ON vieetablissement.canteen_menu (establishment_id);
CREATE UNIQUE INDEX uq_canteen_menu_1 ON vieetablissement.canteen_menu (establishment_id, menu_date, meal_type);

CREATE TABLE vieetablissement.meal_reservation (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    person_type varchar(40) NOT NULL,
    person_id uuid NOT NULL,
    status varchar(40) NOT NULL,
    menu_id uuid NOT NULL REFERENCES vieetablissement.canteen_menu(id)
);
CREATE INDEX ix_meal_reservation_establishment ON vieetablissement.meal_reservation (establishment_id);
CREATE INDEX ix_meal_reservation_menu_id ON vieetablissement.meal_reservation (menu_id);
CREATE UNIQUE INDEX uq_meal_reservation_1 ON vieetablissement.meal_reservation (establishment_id, menu_id, person_id);

CREATE TABLE vieetablissement.infirmary_visit (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    visited_at timestamp(6) with time zone NOT NULL,
    reason varchar(1000) NOT NULL,
    care_given text,
    guardians_notified boolean NOT NULL DEFAULT false,
    follow_up varchar(1000)
);
CREATE INDEX ix_infirmary_visit_establishment ON vieetablissement.infirmary_visit (establishment_id);

CREATE TABLE vieetablissement.access_point (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    site_id uuid,
    code varchar(40) NOT NULL,
    name varchar(255) NOT NULL
);
CREATE INDEX ix_access_point_establishment ON vieetablissement.access_point (establishment_id);
CREATE UNIQUE INDEX uq_access_point_1 ON vieetablissement.access_point (establishment_id, code);

CREATE TABLE vieetablissement.access_event (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    person_type varchar(40) NOT NULL,
    person_id uuid NOT NULL,
    occurred_at timestamp(6) with time zone NOT NULL,
    direction varchar(40) NOT NULL,
    method varchar(40) NOT NULL,
    granted boolean NOT NULL DEFAULT false,
    access_point_id uuid NOT NULL REFERENCES vieetablissement.access_point(id)
);
CREATE INDEX ix_access_event_establishment ON vieetablissement.access_event (establishment_id);
CREATE INDEX ix_access_event_access_point_id ON vieetablissement.access_event (access_point_id);

CREATE TABLE vieetablissement.entry_ticket (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    attendance_record_id uuid,
    reason varchar(40) NOT NULL,
    valid_for_date date NOT NULL,
    note varchar(500)
);
CREATE INDEX ix_entry_ticket_establishment ON vieetablissement.entry_ticket (establishment_id);
