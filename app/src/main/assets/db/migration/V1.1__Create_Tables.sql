create table NOTE (  
                ID text primary key not null,
                TITLE text,
                CONTENT text,
                TYPE text,
                CREATE_TIME integer,
                LAST_MODIFY_TIME integer,
                VERSION integer,
                READ_FLAG integer,
                PRIORITY integer);
                
create table TODO (  
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

create table DOCUMENT ( 
                ID text primary key not null ,  
                TITLE text,  
                CONTENT text, 
                TYPE text, 
                CREATE_TIME integer,  
                LAST_MODIFY_TIME integer,  
                VERSION integer, 
                READ_FLAG integer,  
                PRIORITY integer);

create table  ATTACHMENT (
                ID text primary key not null ,
                NOTE_ID text,
                NAME text,
                TYPE text,
                PATH text);

create table  REMINDER (
                ID text primary key not null ,
                CALENDAR_ID integer,
                CALENDAR_EVENT_ID integer,
                CALENDAR_REMINDER_ID integer,
                START integer,
                END integer,
                REPEAT_MILLS integer);


