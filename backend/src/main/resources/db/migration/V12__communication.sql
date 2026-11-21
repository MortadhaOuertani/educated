-- communication: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS communication;

CREATE TABLE communication.message (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    subject varchar(255) NOT NULL,
    body text,
    sms_text varchar(160),
    audience varchar(40) NOT NULL,
    category varchar(40) NOT NULL,
    send_push boolean NOT NULL DEFAULT false,
    send_sms boolean NOT NULL DEFAULT false,
    send_email boolean NOT NULL DEFAULT false,
    also_notify_guardians boolean NOT NULL DEFAULT false,
    sent_at timestamp(6) with time zone,
    reply_to_id uuid REFERENCES communication.message(id)
);
CREATE INDEX ix_message_establishment ON communication.message (establishment_id);
CREATE INDEX ix_message_reply_to_id ON communication.message (reply_to_id);

CREATE TABLE communication.message_attachment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    file_id uuid NOT NULL,
    message_id uuid NOT NULL REFERENCES communication.message(id)
);
CREATE INDEX ix_message_attachment_establishment ON communication.message_attachment (establishment_id);
CREATE INDEX ix_message_attachment_message_id ON communication.message_attachment (message_id);

CREATE TABLE communication.message_audience_filter (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    all_recipients boolean NOT NULL DEFAULT false,
    study_level_id uuid,
    school_class_id uuid,
    message_id uuid NOT NULL REFERENCES communication.message(id)
);
CREATE INDEX ix_message_audience_filter_establishment ON communication.message_audience_filter (establishment_id);
CREATE INDEX ix_message_audience_filter_message_id ON communication.message_audience_filter (message_id);

CREATE TABLE communication.message_recipient (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    recipient_user_id uuid NOT NULL,
    delivered_at timestamp(6) with time zone,
    read_at timestamp(6) with time zone,
    sms_status varchar(40),
    archived boolean NOT NULL DEFAULT false,
    message_id uuid NOT NULL REFERENCES communication.message(id)
);
CREATE INDEX ix_message_recipient_establishment ON communication.message_recipient (establishment_id);
CREATE INDEX ix_message_recipient_message_id ON communication.message_recipient (message_id);
CREATE UNIQUE INDEX uq_message_recipient_1 ON communication.message_recipient (establishment_id, message_id, recipient_user_id);

CREATE TABLE communication.conversation (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    conversation_type varchar(40) NOT NULL,
    scope varchar(40) NOT NULL,
    title varchar(255),
    last_message_at timestamp(6) with time zone
);
CREATE INDEX ix_conversation_establishment ON communication.conversation (establishment_id);

CREATE TABLE communication.conversation_participant (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    user_id uuid NOT NULL,
    last_read_at timestamp(6) with time zone,
    muted boolean NOT NULL DEFAULT false,
    conversation_id uuid NOT NULL REFERENCES communication.conversation(id)
);
CREATE INDEX ix_conversation_participant_establishment ON communication.conversation_participant (establishment_id);
CREATE INDEX ix_conversation_participant_conversation_id ON communication.conversation_participant (conversation_id);
CREATE UNIQUE INDEX uq_conversation_participant_1 ON communication.conversation_participant (establishment_id, conversation_id, user_id);

CREATE TABLE communication.chat_message (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    content text NOT NULL,
    attachment_file_id uuid,
    conversation_id uuid NOT NULL REFERENCES communication.conversation(id)
);
CREATE INDEX ix_chat_message_establishment ON communication.chat_message (establishment_id);
CREATE INDEX ix_chat_message_conversation_id ON communication.chat_message (conversation_id);

CREATE TABLE communication.news (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    title varchar(255) NOT NULL,
    body text,
    cover_image_file_id uuid,
    audience varchar(40) NOT NULL,
    status varchar(40) NOT NULL,
    published_by_user_id uuid,
    published_at timestamp(6) with time zone,
    notified_at timestamp(6) with time zone
);
CREATE INDEX ix_news_establishment ON communication.news (establishment_id);

CREATE TABLE communication.news_attachment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    file_id uuid NOT NULL,
    news_id uuid NOT NULL REFERENCES communication.news(id)
);
CREATE INDEX ix_news_attachment_establishment ON communication.news_attachment (establishment_id);
CREATE INDEX ix_news_attachment_news_id ON communication.news_attachment (news_id);

CREATE TABLE communication.notification (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    recipient_user_id uuid NOT NULL,
    notification_type varchar(40) NOT NULL,
    title varchar(255) NOT NULL,
    body varchar(1000),
    target_type varchar(80),
    target_id uuid,
    read_at timestamp(6) with time zone,
    push_sent_at timestamp(6) with time zone,
    sms_sent_at timestamp(6) with time zone
);
CREATE INDEX ix_notification_establishment ON communication.notification (establishment_id);

CREATE TABLE communication.notification_preference (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    user_id uuid NOT NULL,
    notification_type varchar(40) NOT NULL,
    in_app boolean NOT NULL DEFAULT false,
    push boolean NOT NULL DEFAULT false,
    email boolean NOT NULL DEFAULT false,
    sms boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_notification_preference_establishment ON communication.notification_preference (establishment_id);
CREATE UNIQUE INDEX uq_notification_preference_1 ON communication.notification_preference (establishment_id, user_id, notification_type);

CREATE TABLE communication.do_not_disturb_setting (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    user_id uuid NOT NULL,
    mode varchar(40) NOT NULL,
    from_time time(6),
    to_time time(6)
);
CREATE INDEX ix_do_not_disturb_setting_establishment ON communication.do_not_disturb_setting (establishment_id);
CREATE UNIQUE INDEX uq_do_not_disturb_setting_1 ON communication.do_not_disturb_setting (establishment_id, user_id);

CREATE INDEX ix_notification_recipient ON communication.notification (recipient_user_id, created_at);
