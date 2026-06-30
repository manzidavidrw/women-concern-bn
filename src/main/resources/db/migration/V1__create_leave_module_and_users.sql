-- =========================
-- USERS TABLE
-- =========================
CREATE TABLE users (
                       id UUID PRIMARY KEY,

                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,

                       first_name VARCHAR(255) NOT NULL,
                       last_name VARCHAR(255),

                       phone_number VARCHAR(50),

                       role VARCHAR(50) NOT NULL,

                       job_title VARCHAR(255),
                       address TEXT,

                       national_id VARCHAR(100),
                       emergency_contact VARCHAR(100),

                       date_of_birth DATE,
                       gender VARCHAR(50) NOT NULL,

                       certificates TEXT,
                       joined_at DATE,

                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       must_change_password BOOLEAN NOT NULL DEFAULT FALSE,

                       created_at TIMESTAMP,
                       updated_at TIMESTAMP
);

-- =========================
-- LEAVE TYPES
-- =========================
CREATE TABLE leave_types (
                             id UUID PRIMARY KEY,

                             name VARCHAR(255) NOT NULL,
                             description TEXT,

                             max_days_per_year INTEGER,
                             requires_attachment BOOLEAN DEFAULT FALSE,
                             is_paid BOOLEAN DEFAULT TRUE,

                             eligibility VARCHAR(50) NOT NULL DEFAULT 'ALL',

                             allow_carry_forward BOOLEAN DEFAULT FALSE,
                             max_carry_forward_days INTEGER,

                             created_at TIMESTAMP,
                             updated_at TIMESTAMP
);

-- =========================
-- LEAVE BALANCES
-- =========================
CREATE TABLE leave_balances (
                                id UUID PRIMARY KEY,

                                employee_id UUID NOT NULL,
                                leave_type_id UUID NOT NULL,

                                year INTEGER NOT NULL,
                                allocated_days INTEGER NOT NULL,
                                used_days INTEGER NOT NULL DEFAULT 0,
                                carried_forward INTEGER NOT NULL DEFAULT 0,
                                carry_expiry_date DATE,

                                created_at TIMESTAMP,
                                updated_at TIMESTAMP,

                                CONSTRAINT fk_leave_balance_employee
                                    FOREIGN KEY (employee_id) REFERENCES users(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_leave_balance_type
                                    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT uq_leave_balance UNIQUE (employee_id, leave_type_id, year)
);

-- =========================
-- LEAVE REQUESTS
-- =========================
CREATE TABLE leave_requests (
                                id UUID PRIMARY KEY,

                                employee_id UUID NOT NULL,
                                leave_type_id UUID NOT NULL,

                                start_date DATE NOT NULL,
                                end_date DATE NOT NULL,
                                days_requested INTEGER NOT NULL,

                                reason TEXT,
                                status VARCHAR(50) NOT NULL,

                                decision_by UUID,
                                decision_at TIMESTAMP,
                                decision_comment TEXT,

                                created_at TIMESTAMP,
                                updated_at TIMESTAMP,

                                CONSTRAINT fk_leave_request_employee
                                    FOREIGN KEY (employee_id) REFERENCES users(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_leave_request_type
                                    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_leave_request_decision_by
                                    FOREIGN KEY (decision_by) REFERENCES users(id)
                                        ON DELETE SET NULL
);

-- =========================
-- INDEXES
-- =========================
CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);

CREATE INDEX idx_leave_balances_employee ON leave_balances(employee_id);
CREATE INDEX idx_leave_balances_year ON leave_balances(year);