CREATE DATABASE orangehrm_test_db;
USE orangehrm_test_db;

-- Table 1: test_suites
CREATE TABLE test_suites (
    suite_id INT PRIMARY KEY AUTO_INCREMENT,
    suite_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table 2: test_cases
CREATE TABLE test_cases (
    test_case_id INT PRIMARY KEY AUTO_INCREMENT,
    suite_id INT NOT NULL,
    test_case_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (suite_id) REFERENCES test_suites(suite_id) ON DELETE CASCADE,
    UNIQUE KEY unique_test_case (suite_id, test_case_name)
);

-- Table 3: test_configurations
CREATE TABLE test_configurations (
    config_id INT PRIMARY KEY AUTO_INCREMENT,
    test_case_id INT NOT NULL,
    base_url VARCHAR(500),
    config_data JSON,
    is_active BOOLEAN DEFAULT TRUE,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (test_case_id) REFERENCES test_cases(test_case_id) ON DELETE CASCADE
);

-- Table 4: baselines
CREATE TABLE baselines (
    baseline_id INT PRIMARY KEY AUTO_INCREMENT,
    test_case_id INT NOT NULL,
    baseline_type VARCHAR(50) DEFAULT 'url',
    expected_value TEXT NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    version INT DEFAULT 1,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (test_case_id) REFERENCES test_cases(test_case_id) ON DELETE CASCADE
);

-- Table 5: test_executions
CREATE TABLE test_executions (
    execution_id INT PRIMARY KEY AUTO_INCREMENT,
    execution_name VARCHAR(200),
    browser VARCHAR(50),
    base_url VARCHAR(500),
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    total_tests INT DEFAULT 0,
    passed_tests INT DEFAULT 0,
    failed_tests INT DEFAULT 0,
    skipped_tests INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'RUNNING',
    execution_metadata JSON
);

-- Table 6: test_results
CREATE TABLE test_results (
    result_id INT PRIMARY KEY AUTO_INCREMENT,
    execution_id INT NOT NULL,
    test_case_id INT NOT NULL,
    baseline_id INT,
    actual_value TEXT,
    expected_value TEXT,
    comparison_result VARCHAR(20),
    error_message TEXT,
    stack_trace TEXT,
    duration_ms BIGINT,
    screenshot_path VARCHAR(500),
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (execution_id) REFERENCES test_executions(execution_id) ON DELETE CASCADE,
    FOREIGN KEY (test_case_id) REFERENCES test_cases(test_case_id) ON DELETE CASCADE,
    FOREIGN KEY (baseline_id) REFERENCES baselines(baseline_id) ON DELETE SET NULL
);

-- Table 7: test_steps
CREATE TABLE test_steps (
    step_id INT PRIMARY KEY AUTO_INCREMENT,
    result_id INT NOT NULL,
    step_number INT NOT NULL,
    step_description TEXT,
    status VARCHAR(20),
    step_data TEXT,
    screenshot_path VARCHAR(500),
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (result_id) REFERENCES test_results(result_id) ON DELETE CASCADE
);

-- Table 8: baseline_comparisons
CREATE TABLE baseline_comparisons (
    comparison_id INT PRIMARY KEY AUTO_INCREMENT,
    result_id INT NOT NULL,
    baseline_id INT NOT NULL,
    expected_value TEXT,
    actual_value TEXT,
    diff_details TEXT,
    match_status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (result_id) REFERENCES test_results(result_id) ON DELETE CASCADE,
    FOREIGN KEY (baseline_id) REFERENCES baselines(baseline_id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_test_results_execution ON test_results(execution_id);
CREATE INDEX idx_test_results_testcase ON test_results(test_case_id);
CREATE INDEX idx_test_results_status ON test_results(comparison_result);
CREATE INDEX idx_test_executions_status ON test_executions(status);
CREATE INDEX idx_test_executions_started ON test_executions(started_at);
CREATE INDEX idx_baselines_testcase ON baselines(test_case_id, is_active);

-- Verify tables created
SHOW TABLES;
-------------------------------------------------------------------------------------------------------------------------
-- =====================================================
-- USEFUL QUERIES FOR REPORTING
-- =====================================================

-- Latest execution summary
SELECT 
    execution_id,
    execution_name,
    browser,
    started_at,
    completed_at,
    TIMESTAMPDIFF(SECOND, started_at, completed_at) as duration_seconds,
    total_tests,
    passed_tests,
    failed_tests,
    status
FROM test_executions 
ORDER BY execution_id DESC 
LIMIT 1;

-- All test results from the latest execution
SELECT 
    tc.test_case_name,
    tr.comparison_result,
    tr.actual_value,
    tr.expected_value,
    tr.duration_ms,
    tr.started_at
FROM test_results tr
JOIN test_cases tc ON tr.test_case_id = tc.test_case_id
WHERE tr.execution_id = (SELECT MAX(execution_id) FROM test_executions)
ORDER BY tr.result_id;

-- Calculate pass rate for each test suite
SELECT 
    ts.suite_name,
    COUNT(tr.result_id) as total_runs,
    SUM(CASE WHEN tr.comparison_result = 'PASS' THEN 1 ELSE 0 END) as passes,
    SUM(CASE WHEN tr.comparison_result = 'FAIL' THEN 1 ELSE 0 END) as fails,
    ROUND(SUM(CASE WHEN tr.comparison_result = 'PASS' THEN 1 ELSE 0 END) * 100.0 / COUNT(tr.result_id), 2) as pass_rate
FROM test_suites ts
JOIN test_cases tc ON ts.suite_id = tc.suite_id
JOIN test_results tr ON tc.test_case_id = tr.test_case_id
GROUP BY ts.suite_name;

-- Get all failed test results
SELECT 
    tc.test_case_name,
    tr.actual_value,
    tr.expected_value,
    tr.error_message,
    tr.started_at
FROM test_results tr
JOIN test_cases tc ON tr.test_case_id = tc.test_case_id
WHERE tr.comparison_result = 'FAIL'
ORDER BY tr.started_at DESC;

-- Find the slowest running tests
SELECT 
    tc.test_case_name,
    AVG(tr.duration_ms) as avg_duration_ms,
    MAX(tr.duration_ms) as max_duration_ms,
    MIN(tr.duration_ms) as min_duration_ms,
    COUNT(*) as runs
FROM test_results tr
JOIN test_cases tc ON tr.test_case_id = tc.test_case_id
GROUP BY tc.test_case_name
ORDER BY avg_duration_ms DESC
LIMIT 10;

-- Identify unstable/flaky tests
SELECT 
    tc.test_case_name,
    SUM(CASE WHEN tr.comparison_result = 'PASS' THEN 1 ELSE 0 END) as passes,
    SUM(CASE WHEN tr.comparison_result = 'FAIL' THEN 1 ELSE 0 END) as fails,
    COUNT(*) as total_runs,
    ROUND(SUM(CASE WHEN tr.comparison_result = 'PASS' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as stability_rate
FROM test_results tr
JOIN test_cases tc ON tr.test_case_id = tc.test_case_id
GROUP BY tc.test_case_name
HAVING passes > 0 AND fails > 0
ORDER BY stability_rate ASC;

-- Get execution history for a specific test
SELECT 
    te.execution_id,
    te.started_at,
    tr.comparison_result,
    tr.duration_ms,
    tr.actual_value,
    tr.expected_value
FROM test_results tr
JOIN test_executions te ON tr.execution_id = te.execution_id
JOIN test_cases tc ON tr.test_case_id = tc.test_case_id
WHERE tc.test_case_name = 'TC_LOG_001_validLogin'
ORDER BY te.started_at DESC
LIMIT 10;

-- Compare latest execution results with previous execution
SELECT 
    tc.test_case_name,
    MAX(CASE WHEN te.execution_id = (SELECT MAX(execution_id) FROM test_executions) 
        THEN tr.comparison_result END) as latest_result,
    MAX(CASE WHEN te.execution_id = (SELECT MAX(execution_id) - 1 FROM test_executions) 
        THEN tr.comparison_result END) as previous_result
FROM test_results tr
JOIN test_cases tc ON tr.test_case_id = tc.test_case_id
JOIN test_executions te ON tr.execution_id = te.execution_id
WHERE te.execution_id IN (
    (SELECT MAX(execution_id) FROM test_executions),
    (SELECT MAX(execution_id) - 1 FROM test_executions)
)
GROUP BY tc.test_case_name;

-- Comprehensive summary of the latest execution
SELECT 
    te.execution_id,
    te.execution_name,
    te.browser,
    te.started_at,
    te.completed_at,
    TIMESTAMPDIFF(SECOND, te.started_at, te.completed_at) as duration_seconds,
    te.total_tests,
    te.passed_tests,
    te.failed_tests,
    te.skipped_tests,
    ROUND(te.passed_tests * 100.0 / NULLIF(te.total_tests, 0), 2) as pass_rate,
    te.status
FROM test_executions te
WHERE te.execution_id = (SELECT MAX(execution_id) FROM test_executions);

-- Daily test trend (last 30 days)
SELECT 
    DATE(started_at) as date,
    COUNT(*) as total_tests,
    SUM(CASE WHEN comparison_result = 'PASS' THEN 1 ELSE 0 END) as passed,
    ROUND(AVG(CASE WHEN comparison_result = 'PASS' THEN 100 ELSE 0 END), 2) as pass_rate
FROM test_results tr
JOIN test_executions te ON tr.execution_id = te.execution_id
WHERE te.started_at > DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(started_at)
ORDER BY date DESC;
---------------------------------------------------------------------------------------------------
