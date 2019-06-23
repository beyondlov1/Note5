create table if not exists  NOTE (
                ID text primary key not null,
                TITLE text,
                CONTENT text,
                TYPE text,
                CREATE_TIME integer,
                LAST_MODIFY_TIME integer,
                VERSION integer,
                READ_FLAG integer,
                PRIORITY integer);
                
create table if not exists  TODO (
                ID text primary key not null ,
                REMINDER_ID text,   
                TITLE text,  
                CONTENT text,  
                CONTENT_WITHOUT_TIME text, 
                TYPE text,
                CREATE_TIME integer,  
                LAST_MODIFY_TIME integer,  
                VERSION integer, 
                READ_FLAG integer,  
                PRIORITY integer);

create table if not exists  DOCUMENT (
                ID text primary key not null ,  
                TITLE text,  
                CONTENT text, 
                TYPE text, 
                CREATE_TIME integer,  
                LAST_MODIFY_TIME integer,  
                VERSION integer, 
                READ_FLAG integer,  
                PRIORITY integer);

create table if not exists  ATTACHMENT (
                ID text primary key not null ,
                NOTE_ID text,
                NAME text,
                TYPE text,
                PATH text);

create table if not exists  REMINDER (
                ID text primary key not null ,
                CALENDAR_ID integer,
                CALENDAR_EVENT_ID integer,
                CALENDAR_REMINDER_ID integer,
                START integer,
                END integer,
                REPEAT_MILLS integer);


