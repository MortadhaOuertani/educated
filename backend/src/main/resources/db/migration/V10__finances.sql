-- finances: generated from the class diagram (see docs/class-diagram.md).
CREATE SCHEMA IF NOT EXISTS finances;

CREATE TABLE finances.fee_schedule (
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
    fee_type varchar(40) NOT NULL,
    amount_amount numeric(14,3),
    amount_currency varchar(3),
    installments_count integer NOT NULL,
    active boolean NOT NULL DEFAULT false
);
CREATE INDEX ix_fee_schedule_establishment ON finances.fee_schedule (establishment_id);

CREATE TABLE finances.student_fee_plan (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    enrollment_id uuid NOT NULL,
    total_amount_amount numeric(14,3),
    total_amount_currency varchar(3),
    discount_amount numeric(14,3),
    discount_currency varchar(3),
    scholarship_amount numeric(14,3),
    scholarship_currency varchar(3),
    status varchar(40) NOT NULL
);
CREATE INDEX ix_student_fee_plan_establishment ON finances.student_fee_plan (establishment_id);
CREATE UNIQUE INDEX uq_student_fee_plan_1 ON finances.student_fee_plan (establishment_id, enrollment_id);

CREATE TABLE finances.installment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    installment_number integer NOT NULL,
    due_date date NOT NULL,
    amount_amount numeric(14,3),
    amount_currency varchar(3),
    paid_amount_amount numeric(14,3),
    paid_amount_currency varchar(3),
    status varchar(40) NOT NULL,
    fee_plan_id uuid NOT NULL REFERENCES finances.student_fee_plan(id)
);
CREATE INDEX ix_installment_establishment ON finances.installment (establishment_id);
CREATE INDEX ix_installment_fee_plan_id ON finances.installment (fee_plan_id);

CREATE TABLE finances.payment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    student_id uuid NOT NULL,
    amount_amount numeric(14,3),
    amount_currency varchar(3),
    paid_at timestamp(6) with time zone NOT NULL,
    method varchar(40) NOT NULL,
    reference varchar(100),
    receipt_number varchar(40) NOT NULL
);
CREATE INDEX ix_payment_establishment ON finances.payment (establishment_id);
CREATE UNIQUE INDEX uq_payment_1 ON finances.payment (establishment_id, receipt_number);

CREATE TABLE finances.payment_allocation (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    amount_amount numeric(14,3),
    amount_currency varchar(3),
    payment_id uuid NOT NULL REFERENCES finances.payment(id),
    installment_id uuid NOT NULL REFERENCES finances.installment(id)
);
CREATE INDEX ix_payment_allocation_establishment ON finances.payment_allocation (establishment_id);
CREATE INDEX ix_payment_allocation_payment_id ON finances.payment_allocation (payment_id);
CREATE INDEX ix_payment_allocation_installment_id ON finances.payment_allocation (installment_id);

CREATE TABLE finances.payment_reminder (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    reminder_level integer NOT NULL,
    channel varchar(40) NOT NULL,
    message varchar(1000),
    sent_at timestamp(6) with time zone,
    installment_id uuid NOT NULL REFERENCES finances.installment(id)
);
CREATE INDEX ix_payment_reminder_establishment ON finances.payment_reminder (establishment_id);
CREATE INDEX ix_payment_reminder_installment_id ON finances.payment_reminder (installment_id);

CREATE TABLE finances.teacher_payment (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    teacher_id uuid NOT NULL,
    period_from date NOT NULL,
    period_to date NOT NULL,
    total_hours numeric(7,2),
    hourly_rate_amount numeric(14,3),
    hourly_rate_currency varchar(3),
    total_amount_amount numeric(14,3),
    total_amount_currency varchar(3),
    status varchar(40) NOT NULL,
    paid_by_user_id uuid,
    paid_at timestamp(6) with time zone,
    cancelled_at timestamp(6) with time zone
);
CREATE INDEX ix_teacher_payment_establishment ON finances.teacher_payment (establishment_id);

CREATE TABLE finances.teacher_payment_line (
    id uuid PRIMARY KEY,
    created_at timestamp(6) with time zone NOT NULL,
    created_by_user_id uuid,
    updated_at timestamp(6) with time zone,
    updated_by_user_id uuid,
    version bigint,
    establishment_id uuid NOT NULL,
    archived_at timestamp(6) with time zone,
    teacher_attendance_id uuid NOT NULL,
    session_date date NOT NULL,
    hours numeric(5,2) NOT NULL,
    amount_amount numeric(14,3),
    amount_currency varchar(3),
    payment_id uuid NOT NULL REFERENCES finances.teacher_payment(id)
);
CREATE INDEX ix_teacher_payment_line_establishment ON finances.teacher_payment_line (establishment_id);
CREATE INDEX ix_teacher_payment_line_payment_id ON finances.teacher_payment_line (payment_id);

CREATE TABLE finances.student_fee_plan_schedule (
    student_fee_plan_id uuid NOT NULL REFERENCES finances.student_fee_plan(id) ON DELETE CASCADE,
    fee_schedule_id uuid NOT NULL REFERENCES finances.fee_schedule(id),
    PRIMARY KEY (student_fee_plan_id, fee_schedule_id)
);
