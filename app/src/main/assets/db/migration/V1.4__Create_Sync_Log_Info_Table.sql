create table if not exists  SYNC_LOG_INFO (
                ID text primary key not null ,
                DOCUMENT_ID text,
                REF_PATH text,
                REF_SERVER text,
                OPERATION text,
                OPERATION_TIME integer,
                SOURCE text);