alter table SYNC_INFO add column LAST_SYNC_TIME_START integer;

update SYNC_INFO set LAST_SYNC_TIME_START = LAST_SYNC_TIME;