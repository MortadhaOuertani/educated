-- scolarite: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS scolarite;

CREATE TABLE scolarite.student (
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
    matricule varchar(40) NOT NULL,
    status varchar(40) NOT NULL,
    admission_date date,
    baccalaureate_section varchar(100),
    baccalaureate_average numeric(5,2),
    baccalaureate_year integer,
    previous_institution varchar(255)
);
CREATE INDEX ix_student_establishment ON scolarite.student (establishment_id);
CREATE UNIQUE INDEX uq_student_1 ON scolarite.student (establishment_id, matricule);

CREATE TABLE scolarite.guardian (
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
    profession varchar(255),
    employer varchar(255),
    work_phone varchar(30),
    preferred_language varchar(5),
    sms_opt_in boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_guardian_establishment ON scolarite.guardian (establishment_id);

CREATE TABLE scolarite.kinship (
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
CREATE INDEX ix_kinship_establishment ON scolarite.kinship (establishment_id);
CREATE UNIQUE INDEX uq_kinship_1 ON scolarite.kinship (establishment_id, code);

CREATE TABLE scolarite.student_guardian (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    primary_contact boolean NOT NULL DEFAULT false,
    financial_responsible boolean NOT NULL DEFAULT false,
    lives_with_student boolean NOT NULL DEFAULT false,
    notify_push boolean NOT NULL DEFAULT false,
    notify_sms boolean NOT NULL DEFAULT false,
    student_id uuid NOT NULL REFERENCES scolarite.student(id),
    guardian_id uuid NOT NULL REFERENCES scolarite.guardian(id),
    kinship_id uuid NOT NULL REFERENCES scolarite.kinship(id)
);
CREATE INDEX ix_student_guardian_establishment ON scolarite.student_guardian (establishment_id);
CREATE INDEX ix_student_guardian_student_id ON scolarite.student_guardian (student_id);
CREATE INDEX ix_student_guardian_guardian_id ON scolarite.student_guardian (guardian_id);
CREATE INDEX ix_student_guardian_kinship_id ON scolarite.student_guardian (kinship_id);
CREATE UNIQUE INDEX uq_student_guardian_1 ON scolarite.student_guardian (establishment_id, student_id, guardian_id);

CREATE TABLE scolarite.sibling_relation (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    note varchar(500),
    student_id uuid NOT NULL REFERENCES scolarite.student(id),
    sibling_id uuid NOT NULL REFERENCES scolarite.student(id)
);
CREATE INDEX ix_sibling_relation_establishment ON scolarite.sibling_relation (establishment_id);
CREATE INDEX ix_sibling_relation_student_id ON scolarite.sibling_relation (student_id);
CREATE INDEX ix_sibling_relation_sibling_id ON scolarite.sibling_relation (sibling_id);
CREATE UNIQUE INDEX uq_sibling_relation_1 ON scolarite.sibling_relation (establishment_id, student_id, sibling_id);

CREATE TABLE scolarite.pre_registration_request (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    applicant_first_name varchar(255),
    applicant_last_name varchar(255),
    applicant_first_name_ar varchar(255),
    applicant_last_name_ar varchar(255),
    applicant_gender varchar(255),
    applicant_birth_date date,
    applicant_birth_place varchar(255),
    applicant_nationality varchar(255),
    applicant_national_id_number varchar(255),
    applicant_passport_number varchar(255),
    applicant_email varchar(255),
    applicant_phone varchar(255),
    applicant_mobile varchar(255),
    applicant_address_line1 varchar(255),
    applicant_address_line2 varchar(255),
    applicant_address_city varchar(255),
    applicant_address_postal_code varchar(255),
    applicant_address_governorate varchar(255),
    applicant_address_country varchar(255),
    applicant_photo_file_id uuid,
    program_id uuid NOT NULL,
    study_level_id uuid NOT NULL,
    academic_year_id uuid NOT NULL,
    baccalaureate_section varchar(100),
    baccalaureate_average numeric(5,2),
    status varchar(40) NOT NULL,
    reviewed_by_user_id uuid,
    reviewed_at timestamp(6) with time zone,
    decision_note varchar(1000),
    converted_to_id uuid REFERENCES scolarite.student(id)
);
CREATE INDEX ix_pre_registration_request_establishment ON scolarite.pre_registration_request (establishment_id);
CREATE INDEX ix_pre_registration_request_converted_to_id ON scolarite.pre_registration_request (converted_to_id);

CREATE TABLE scolarite.pre_registration_document (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    label varchar(200) NOT NULL,
    file_id uuid NOT NULL,
    request_id uuid NOT NULL REFERENCES scolarite.pre_registration_request(id)
);
CREATE INDEX ix_pre_registration_document_establishment ON scolarite.pre_registration_document (establishment_id);
CREATE INDEX ix_pre_registration_document_request_id ON scolarite.pre_registration_document (request_id);

CREATE TABLE scolarite.enrollment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    academic_year_id uuid NOT NULL,
    study_level_id uuid NOT NULL,
    enrollment_date date NOT NULL,
    enrollment_type varchar(40) NOT NULL,
    status varchar(40) NOT NULL,
    repeating boolean NOT NULL DEFAULT false,
    registration_form_printed_at timestamp(6) with time zone,
    student_id uuid NOT NULL REFERENCES scolarite.student(id)
);
CREATE INDEX ix_enrollment_establishment ON scolarite.enrollment (establishment_id);
CREATE INDEX ix_enrollment_student_id ON scolarite.enrollment (student_id);

CREATE TABLE scolarite.class_assignment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    school_class_id uuid NOT NULL,
    group_code varchar(20),
    start_date date NOT NULL,
    end_date date,
    pedagogical_status varchar(40) NOT NULL,
    enrollment_id uuid NOT NULL REFERENCES scolarite.enrollment(id)
);
CREATE INDEX ix_class_assignment_establishment ON scolarite.class_assignment (establishment_id);
CREATE INDEX ix_class_assignment_enrollment_id ON scolarite.class_assignment (enrollment_id);

CREATE TABLE scolarite.sanction (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    sanction_type varchar(40) NOT NULL,
    reason varchar(1000) NOT NULL,
    incident_date date NOT NULL,
    start_date date,
    end_date date,
    issued_by_user_id uuid,
    notify_guardians boolean NOT NULL DEFAULT false,
    notified_at timestamp(6) with time zone,
    student_id uuid NOT NULL REFERENCES scolarite.student(id)
);
CREATE INDEX ix_sanction_establishment ON scolarite.sanction (establishment_id);
CREATE INDEX ix_sanction_student_id ON scolarite.sanction (student_id);

CREATE TABLE scolarite.document_type (
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
    language varchar(40) NOT NULL,
    template_file_id uuid,
    active boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_document_type_establishment ON scolarite.document_type (establishment_id);
CREATE UNIQUE INDEX uq_document_type_1 ON scolarite.document_type (establishment_id, code, language);

CREATE TABLE scolarite.document_request (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    reason varchar(1000),
    proof_file_id uuid,
    status varchar(40) NOT NULL,
    processed_by_user_id uuid,
    processed_at timestamp(6) with time zone,
    generated_file_id uuid,
    collected_at timestamp(6) with time zone,
    rejection_reason varchar(1000),
    student_id uuid NOT NULL REFERENCES scolarite.student(id),
    document_type_id uuid NOT NULL REFERENCES scolarite.document_type(id)
);
CREATE INDEX ix_document_request_establishment ON scolarite.document_request (establishment_id);
CREATE INDEX ix_document_request_student_id ON scolarite.document_request (student_id);
CREATE INDEX ix_document_request_document_type_id ON scolarite.document_request (document_type_id);

CREATE TABLE scolarite.student_card (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    academic_year_id uuid NOT NULL,
    card_number varchar(40) NOT NULL,
    qr_token varchar(100) NOT NULL,
    issued_on date NOT NULL,
    valid_until date NOT NULL,
    pdf_file_id uuid,
    student_id uuid NOT NULL REFERENCES scolarite.student(id)
);
CREATE INDEX ix_student_card_establishment ON scolarite.student_card (establishment_id);
CREATE INDEX ix_student_card_student_id ON scolarite.student_card (student_id);
CREATE UNIQUE INDEX uq_student_card_1 ON scolarite.student_card (establishment_id, student_id, academic_year_id);
CREATE UNIQUE INDEX uq_student_card_2 ON scolarite.student_card (establishment_id, card_number);
CREATE UNIQUE INDEX uq_student_card_3 ON scolarite.student_card (establishment_id, qr_token);

CREATE TABLE scolarite.print_log (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    kind varchar(40) NOT NULL,
    registry_number varchar(40) NOT NULL,
    document_name varchar(255) NOT NULL,
    student_id uuid REFERENCES scolarite.student(id)
);
CREATE INDEX ix_print_log_establishment ON scolarite.print_log (establishment_id);
CREATE INDEX ix_print_log_student_id ON scolarite.print_log (student_id);

-- One non-cancelled enrollment per learner and academic year.
CREATE UNIQUE INDEX uq_enrollment_active ON scolarite.enrollment (establishment_id, student_id, academic_year_id)
    WHERE status <> 'CANCELLED';
-- One open class assignment per enrollment.
CREATE UNIQUE INDEX uq_class_assignment_open ON scolarite.class_assignment (enrollment_id) WHERE end_date IS NULL;
-- At most one primary contact per learner.
CREATE UNIQUE INDEX uq_student_guardian_primary ON scolarite.student_guardian (student_id) WHERE primary_contact;
