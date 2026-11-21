CREATE SCHEMA IF NOT EXISTS shared;

CREATE TABLE shared.stored_file (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    original_name varchar(255) NOT NULL,
    content_type varchar(150) NOT NULL,
    size_bytes bigint NOT NULL,
    storage_key varchar(255) NOT NULL UNIQUE,
    checksum_sha256 varchar(64) NOT NULL,
    scan_status varchar(20) NOT NULL
);
CREATE INDEX ix_stored_file_establishment ON shared.stored_file (establishment_id);

CREATE TABLE shared.audit_log (
    id uuid PRIMARY KEY,
    establishment_id uuid,
    module_name varchar(40) NOT NULL,
    action varchar(80) NOT NULL,
    entity_type varchar(80) NOT NULL,
    entity_id uuid,
    changes_json text,
    actor_user_id uuid,
    ip_address varchar(64),
    user_agent varchar(512),
    occurred_at timestamp(6) with time zone NOT NULL
);
CREATE INDEX ix_audit_log_entity ON shared.audit_log (entity_type, entity_id);
CREATE INDEX ix_audit_log_establishment ON shared.audit_log (establishment_id, occurred_at);
