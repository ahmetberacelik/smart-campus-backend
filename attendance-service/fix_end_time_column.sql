-- ============================================
-- Fix attendance_sessions table: Change end_time and start_time to TIME type
-- ============================================
-- This script fixes the data type mismatch error:
-- "Data truncation: Incorrect datetime value: '18:30:00' for column 'end_time'"

-- Check current column types
-- DESCRIBE attendance_sessions;

-- If columns are DATETIME/TIMESTAMP, change them to TIME
ALTER TABLE attendance_sessions 
MODIFY COLUMN start_time TIME NOT NULL,
MODIFY COLUMN end_time TIME NULL;

-- Verify the change
-- DESCRIBE attendance_sessions;

