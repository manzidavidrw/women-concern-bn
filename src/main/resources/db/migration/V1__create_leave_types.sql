CREATE TABLE leave_types (
                             id UUID PRIMARY KEY,

                             name VARCHAR(100) NOT NULL,

                             description TEXT,

                             max_days_per_year INTEGER,

                             requires_attachment BOOLEAN NOT NULL DEFAULT FALSE,

                             is_paid BOOLEAN NOT NULL DEFAULT TRUE,

                             created_at TIMESTAMP NOT NULL,

                             updated_at TIMESTAMP NOT NULL
);