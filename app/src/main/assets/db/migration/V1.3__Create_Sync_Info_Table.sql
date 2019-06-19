create table SYNC_INFO (
                ID text primary key not null ,
                NODE text,
                PATH text,
                LAST_SYNC_TIME integer);