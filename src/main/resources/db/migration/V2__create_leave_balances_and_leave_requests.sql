CREATE TABLE leave_balances (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    employee_id VARCHAR(255) NOT NULL,
    leave_type_id UUID NOT NULL,
    year INTEGER NOT NULL,
    allocated_days INTEGER NOT NULL,
    used_days INTEGER NOT NULL DEFAULT 0,
    carried_forward INTEGER,
    carry_expiry_date DATE,

    CONSTRAINT fk_leave_balance_leave_type
        FOREIGN KEY (leave_type_id)
        REFERENCES leave_types(id),

    CONSTRAINT uk_leave_balance_employee_type_year
        UNIQUE (employee_id, leave_type_id, year)
);

CREATE INDEX idx_leave_balance_employee ON leave_balances(employee_id);
CREATE INDEX idx_leave_balance_leave_type ON leave_balances(leave_type_id);

CREATE TABLE leave_requests (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    employee_id VARCHAR(255) NOT NULL,
    leave_type_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_requested INTEGER NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL,
    decision_by VARCHAR(255),
    decision_at TIMESTAMP,
    decision_comment TEXT,

    CONSTRAINT fk_leave_request_leave_type
        FOREIGN KEY (leave_type_id)
        REFERENCES leave_types(id),

    CONSTRAINT chk_leave_request_dates
        CHECK (end_date >= start_date),

    CONSTRAINT chk_leave_request_days
        CHECK (days_requested > 0)
);

CREATE INDEX idx_leave_request_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_request_status ON leave_requests(status);
CREATE INDEX idx_leave_request_leave_type ON leave_requests(leave_type_id);
CREATE INDEX idx_leave_request_start_date ON leave_requests(start_date);
