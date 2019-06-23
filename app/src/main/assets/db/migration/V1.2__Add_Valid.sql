alter table NOTE add column VALID varchar;
alter table TODO add column VALID varchar ;
alter table DOCUMENT add column VALID varchar ;

update NOTE set VALID = '1';
update TODO set VALID = '1' ;
update DOCUMENT set VALID = '1' ;