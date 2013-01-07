--begin execute immediate 'drop table error_event'; exception when others then null; end;
create table error_event (
    error_event_id integer primary key
    ,created_at timestamp not null
    ,launch_event_id varchar(10) not null
    ,risk_group varchar(50) not null
    ,batch varchar(50) not null
    ,rollup varchar(50) not null
    ,run_id integer not null
);
--begin execute immediate 'drop sequence error_event_seq'; exception when others then null; end;
CREATE SEQUENCE error_event_seq;

--CREATE OR REPLACE TRIGGER error_event_trg 
--BEFORE INSERT ON error_event 
--FOR EACH ROW
--WHEN (new.error_event_id IS NULL)
--BEGIN
--  SELECT error_event_seq.NEXTVAL
--  INTO   :new.error_event_id
--  FROM   dual;
--END;

--begin execute immediate 'drop table pricing_error'; exception when others then null; end;
create table pricing_error (
    pricing_error_id integer primary key
    ,error_event_id integer not null
    ,dictionary varchar(50) not null
    ,market_data varchar(50) not null
    ,split varchar(100) not null
    ,error_message varchar(500) not null
);
--begin execute immediate 'drop index idx_pricing_error'; exception when others then null; end;
create index idx_pricing_error on pricing_error(error_event_id, pricing_error_id);

--begin execute immediate 'drop sequence pricing_error_seq'; exception when others then null; end;
CREATE SEQUENCE pricing_error_seq;


--begin execute immediate 'drop table error_action'; exception when others then null; end;
create table error_action (
    error_action_id integer primary key
    ,business_date integer not null
    ,updated_by varchar(10) not null
    ,updated_at timestamp not null
    ,action_comment varchar(255) not null
);

--begin execute immediate 'drop sequence error_action_seq'; exception when others then null; end;
CREATE SEQUENCE error_action_seq;


--begin execute immediate 'drop table error_action_pricing_error'; exception when others then null; end;
create table error_action_pricing_error (
    error_action_id integer not null
    ,pricing_error_id integer not null
);


/*
begin execute immediate 'drop table pricing_error'; exception when others then null; end;
begin execute immediate 'drop table error_event'; exception when others then null; end;
begin execute immediate 'drop table error_action_pricing_error'; exception when others then null; end;
begin execute immediate 'drop table error_action'; exception when others then null; end;

begin execute immediate 'drop sequence error_event_seq'; exception when others then null; end;
begin execute immediate 'drop sequence pricing_error_seq'; exception when others then null; end;
begin execute immediate 'drop sequence error_action_seq'; exception when others then null; end;

begin execute immediate 'drop index idx_pricing_error'; exception when others then null; end;



select * from pricing_error


*/
