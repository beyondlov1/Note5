create table if not exists   SYNC_STATE_INFO (
                ID text primary key not null ,
                DOCUMENT_ID text,
                LOCAL text,
                SERVER text,
                STATE integer);