create table  SYNC_LOG_INFO (
                ID text primary key not null ,
                REF_PATH text,
                REF_SERVER text,
                OPERATION text,
                OPERATION_TIME integer,
                SOURCE text);