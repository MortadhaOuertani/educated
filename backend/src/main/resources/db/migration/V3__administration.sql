-- administration: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS administration;

CREATE TABLE administration.user_account (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    keycloak_id uuid UNIQUE,
    username varchar(100) NOT NULL UNIQUE,
    email varchar(255),
    phone varchar(30),
    person_type varchar(40),
    person_id uuid,
    active boolean NOT NULL DEFAULT false,
    force_password_change boolean NOT NULL DEFAULT false,
    two_factor_required boolean NOT NULL DEFAULT false,
    last_login_at timestamp(6) with time zone,
    preferred_locale varchar(5),
    theme varchar(40) NOT NULL,
    display_density varchar(40) NOT NULL
);

CREATE TABLE administration.role (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    name varchar(100) NOT NULL,
    description varchar(500),
    administrative boolean NOT NULL DEFAULT false,
    can_edit_model boolean NOT NULL DEFAULT false,
    permission_policy varchar(40) NOT NULL,
    preset varchar(40) NOT NULL,
    system_role boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_role_establishment ON administration.role (establishment_id);
CREATE UNIQUE INDEX uq_role_1 ON administration.role (establishment_id, name);

CREATE TABLE administration.user_role (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    valid_from date,
    valid_to date,
    user_account_id uuid NOT NULL REFERENCES administration.user_account(id),
    role_id uuid NOT NULL REFERENCES administration.role(id)
);
CREATE INDEX ix_user_role_establishment ON administration.user_role (establishment_id);
CREATE INDEX ix_user_role_user_account_id ON administration.user_role (user_account_id);
CREATE INDEX ix_user_role_role_id ON administration.user_role (role_id);
CREATE UNIQUE INDEX uq_user_role_1 ON administration.user_role (establishment_id, user_account_id, role_id);

CREATE TABLE administration.role_module_access (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    module varchar(40) NOT NULL,
    visible boolean NOT NULL DEFAULT false,
    role_id uuid NOT NULL REFERENCES administration.role(id)
);
CREATE INDEX ix_role_module_access_establishment ON administration.role_module_access (establishment_id);
CREATE INDEX ix_role_module_access_role_id ON administration.role_module_access (role_id);
CREATE UNIQUE INDEX uq_role_module_access_1 ON administration.role_module_access (establishment_id, role_id, module);

CREATE TABLE administration.type_permission (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    target_type varchar(100) NOT NULL,
    can_read varchar(40) NOT NULL,
    can_write varchar(40) NOT NULL,
    can_create varchar(40) NOT NULL,
    can_delete varchar(40) NOT NULL,
    can_navigate varchar(40) NOT NULL,
    role_id uuid NOT NULL REFERENCES administration.role(id)
);
CREATE INDEX ix_type_permission_establishment ON administration.type_permission (establishment_id);
CREATE INDEX ix_type_permission_role_id ON administration.type_permission (role_id);
CREATE UNIQUE INDEX uq_type_permission_1 ON administration.type_permission (establishment_id, role_id, target_type);

CREATE TABLE administration.object_permission (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    criteria text NOT NULL,
    can_read varchar(40) NOT NULL,
    can_write varchar(40) NOT NULL,
    can_delete varchar(40) NOT NULL,
    can_navigate varchar(40) NOT NULL,
    type_permission_id uuid NOT NULL REFERENCES administration.type_permission(id)
);
CREATE INDEX ix_object_permission_establishment ON administration.object_permission (establishment_id);
CREATE INDEX ix_object_permission_type_permission_id ON administration.object_permission (type_permission_id);

CREATE TABLE administration.member_permission (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    members text NOT NULL,
    criteria text,
    can_read varchar(40) NOT NULL,
    can_write varchar(40) NOT NULL,
    type_permission_id uuid NOT NULL REFERENCES administration.type_permission(id)
);
CREATE INDEX ix_member_permission_establishment ON administration.member_permission (establishment_id);
CREATE INDEX ix_member_permission_type_permission_id ON administration.member_permission (type_permission_id);

CREATE TABLE administration.navigation_permission (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    item_path varchar(200) NOT NULL,
    can_navigate varchar(40) NOT NULL,
    role_id uuid NOT NULL REFERENCES administration.role(id)
);
CREATE INDEX ix_navigation_permission_establishment ON administration.navigation_permission (establishment_id);
CREATE INDEX ix_navigation_permission_role_id ON administration.navigation_permission (role_id);
CREATE UNIQUE INDEX uq_navigation_permission_1 ON administration.navigation_permission (establishment_id, role_id, item_path);

CREATE TABLE administration.denied_action (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    action_key varchar(100) NOT NULL,
    category varchar(100),
    role_id uuid NOT NULL REFERENCES administration.role(id)
);
CREATE INDEX ix_denied_action_establishment ON administration.denied_action (establishment_id);
CREATE INDEX ix_denied_action_role_id ON administration.denied_action (role_id);
CREATE UNIQUE INDEX uq_denied_action_1 ON administration.denied_action (establishment_id, role_id, action_key);

CREATE TABLE administration.user_favorite (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    navigation_item varchar(200) NOT NULL,
    order_index integer NOT NULL,
    user_account_id uuid NOT NULL REFERENCES administration.user_account(id)
);
CREATE INDEX ix_user_favorite_establishment ON administration.user_favorite (establishment_id);
CREATE INDEX ix_user_favorite_user_account_id ON administration.user_favorite (user_account_id);

CREATE TABLE administration.role_change_request (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    change_type varchar(40) NOT NULL,
    payload_json text NOT NULL,
    status varchar(40) NOT NULL,
    decided_by_user_id uuid,
    decided_at timestamp(6) with time zone,
    role_id uuid NOT NULL REFERENCES administration.role(id)
);
CREATE INDEX ix_role_change_request_establishment ON administration.role_change_request (establishment_id);
CREATE INDEX ix_role_change_request_role_id ON administration.role_change_request (role_id);

CREATE TABLE administration.staff_member (
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
    job_title varchar(255),
    service varchar(255),
    active boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_staff_member_establishment ON administration.staff_member (establishment_id);
CREATE UNIQUE INDEX uq_staff_member_1 ON administration.staff_member (establishment_id, employee_number);
