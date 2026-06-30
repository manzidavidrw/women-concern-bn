CREATE TABLE leave_attachments (
                                   id UUID PRIMARY KEY,
                                   leave_request_id UUID NOT NULL,
                                   file_url VARCHAR(1000) NOT NULL,
                                   public_id VARCHAR(255) NOT NULL,
                                   original_file_name VARCHAR(255),
                                   content_type VARCHAR(100),
                                   file_size BIGINT,

                                   created_at TIMESTAMP,
                                   updated_at TIMESTAMP,

                                   CONSTRAINT fk_leave_attachment_request
                                       FOREIGN KEY (leave_request_id)
                                           REFERENCES leave_requests(id)
                                           ON DELETE CASCADE
);