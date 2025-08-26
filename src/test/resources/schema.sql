DROP TABLE IF EXISTS code_analysis;

CREATE TABLE code_analysis(
    id VARCHAR(255) PRIMARY KEY,
    repository_url VARCHAR(500) NOT NULL,
    branch_name VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    error_message VARCHAR(255)
);
