--apply to bring hsqldb inline with oracle syntax
set database sql syntax ORA true;

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

CREATE SEQUENCE error_event_seq;

create table pricing_error (
    pricing_error_id number(38,0) primary key
    ,error_event_id number(38,0) not null
    ,dictionary varchar(50) not null
    ,market_data varchar(50) not null
    ,split varchar(100) not null
    ,error_message varchar(500) not null
);
create index idx_pe on pricing_error(error_event_id, pricing_error_id);

CREATE SEQUENCE pricing_error_seq;

create table error_action (
    error_action_id number(38,0) primary key
    ,business_date number(38,0) not null
    ,updated_by varchar(10) not null
    ,updated_at timestamp not null
    ,action_comment varchar(255) not null
);
create index idx_ea_business_date on error_action(business_date);

CREATE SEQUENCE error_action_seq;

create table error_action_pricing_error (
    error_action_id number(38,0) not null
    ,pricing_error_id number(38,0) not null
);

create table pricing_run (
    pricing_run_id number(38,0) primary key
    ,created_at timestamp not null
    ,config_id varchar(250) not null
    ,run_label varchar(50) not null
    ,business_date integer not null
);

CREATE SEQUENCE pricing_run_seq;

create view v_error_details as
select
   nvl(ea.error_action_id,-1) error_action_id
  ,nvl(ea.business_date, to_number(to_char(sysdate,'YYYYMMDD'))) business_date
  ,nvl(ea.updated_by, '-') updated_by
  ,ea.updated_at
  ,nvl(ea.action_comment, 'New Errors') action_comment
  ,pr.pricing_run_id run_id
  ,pr.created_at run_created_at
  ,pr.config_id
  ,pr.run_label
  ,pr.business_date run_business_date
  ,ee.error_event_id
  ,ee.created_at error_created_at
  ,ee.launch_event_id
  ,ee.rollup
  ,ee.risk_group
  ,ee.batch
  ,pe.pricing_error_id
  ,pe.dictionary
  ,pe.market_data
  ,pe.split
  ,pe.error_message
from
   pricing_run pr
   inner join error_event ee on(pr.pricing_run_id = ee.run_id)
   inner join pricing_error pe on(pe.error_event_id = ee.error_event_id)
   left outer join (
    select iea.*,ieape.pricing_error_id
    from error_action iea, error_action_pricing_error ieape
    where iea.error_action_id = ieape.error_action_id
   ) ea on ( pe.pricing_error_id = ea.pricing_error_id)
;