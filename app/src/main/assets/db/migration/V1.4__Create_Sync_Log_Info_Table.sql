create table if not exists  SYNC_LOG_INFO (
                ID text primary key not null ,
                DOCUMENT_ID text,
                REF_PATH text,
                REF_SERVER text,
                OPERATION text,
                OPERATION_TIME integer,
                SOURCE text,
                TYPE text);
insert into SYNC_LOG_INFO select ID,ID,null,null,'add',LAST_MODIFY_TIME,null,'note' from NOTE;
insert into SYNC_LOG_INFO select ID,ID,null,null,'add',LAST_MODIFY_TIME,null,'todo' from TODO;