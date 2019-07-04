alter table SYNC_LOG_INFO add column CREATE_TIME integer;

update SYNC_LOG_INFO set CREATE_TIME = OPERATION_TIME;