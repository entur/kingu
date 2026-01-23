-- Add missing sequences needed for integration tests

-- Value entity sequence
CREATE SEQUENCE IF NOT EXISTS value_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;