--begin execute immediate 'drop table pricing_run'; exception when others then null; end;
--begin execute immediate 'drop sequence pricing_run_seq'; exception when others then null; end;
create table pricing_run (
    pricing_run_id number(38,0) primary key
    ,created_at timestamp not null
    ,config_id varchar(250) not null
    ,run_label varchar(50) not null
    ,business_date integer not null
);
grant select, insert, update, delete on pricing_run to MONITOR_APP_USER;

CREATE SEQUENCE pricing_run_seq;
--grant select on pricing_run_seq to MONITOR_APP_USER;

/*
--rollback
begin execute immediate 'drop table pricing_run'; exception when others then null; end;
delete from database_release where script = 'release.20120211-1-create-pricing-run-table.sql'
*/
