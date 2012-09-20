--begin execute immediate 'drop table error_event'; exception when others then null; end;
--begin execute immediate 'drop sequence error_event_seq'; exception when others then null; end;
--begin execute immediate 'drop table pricing_error'; exception when others then null; end;
--begin execute immediate 'drop sequence pricing_error_seq'; exception when others then null; end;
--begin execute immediate 'drop table error_action'; exception when others then null; end;
--begin execute immediate 'drop sequence error_action_seq'; exception when others then null; end;
--begin execute immediate 'drop table error_action_pricing_error'; exception when others then null; end;

create table error_event (
    error_event_id number(38,0) primary key
    ,created_at timestamp not null
    ,launch_event_id varchar(10) not null
    ,risk_group varchar(50) not null
    ,batch varchar(50) not null
    ,rollup varchar(50) not null
    ,run_id number(38,0) not null
);
create unique index idx_ee_launch_event_id on error_event(launch_event_id);
grant select, insert, update, delete on error_event to MONITOR_APP_USER;

CREATE SEQUENCE error_event_seq;
--grant select on error_event_seq to MONITOR_APP_USER;

create table pricing_error (
    pricing_error_id number(38,0) primary key
    ,error_event_id number(38,0) not null
    ,dictionary varchar(50) not null
    ,market_data varchar(50) not null
    ,split varchar(100) not null
    ,error_message varchar(500) not null
);
create index idx_pe on pricing_error(error_event_id, pricing_error_id);
grant select, insert, update, delete on pricing_error to MONITOR_APP_USER;

CREATE SEQUENCE pricing_error_seq;
--grant select on pricing_error_seq to MONITOR_APP_USER;

create table error_action (
    error_action_id number(38,0) primary key
    ,business_date number(38,0) not null
    ,updated_by varchar(10) not null
    ,updated_at timestamp not null
    ,action_comment varchar(255) not null
);
create index idx_ea_business_date on error_action(business_date);
grant select, insert, update, delete on error_action to MONITOR_APP_USER;

CREATE SEQUENCE error_action_seq;
--grant select on error_action_seq to MONITOR_APP_USER;

create table error_action_pricing_error (
    error_action_id number(38,0) not null
    ,pricing_error_id number(38,0) not null
);
grant select, insert, update, delete on error_action_pricing_error to MONITOR_APP_USER;


/*
--rollback
begin execute immediate 'drop table error_event'; exception when others then null; end;
begin execute immediate 'drop sequence error_event_seq'; exception when others then null; end;
begin execute immediate 'drop table pricing_error'; exception when others then null; end;
begin execute immediate 'drop sequence pricing_error_seq'; exception when others then null; end;
begin execute immediate 'drop table error_action'; exception when others then null; end;
begin execute immediate 'drop sequence error_action_seq'; exception when others then null; end;
begin execute immediate 'drop table error_action_pricing_error'; exception when others then null; end;
delete from database_release where script = 'release.20120201-1-create-error-monitor-tables.sql'
*/
