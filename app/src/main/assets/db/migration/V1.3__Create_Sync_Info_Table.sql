create table if not exists  SYNC_INFO (
                ID text primary key not null ,
                LOCAL_KEY text,
                REMOTE_KEY text,
                LAST_SYNC_TIME integer);