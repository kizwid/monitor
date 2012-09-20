/*
--partition and default values not supported in HSQLDB

alter table error_event rename to error_event_old;

create table error_event (
    error_event_id number(38,0) primary key
    ,created_at timestamp not null
    ,launch_event_id varchar(10) not null
    ,risk_group varchar(50) not null
    ,batch varchar(50) not null
    ,rollup varchar(50) not null
    ,run_id number(38,0) not null
    ,audit_month varchar2(10) default to_char(sysdate, 'MON') not null
) MONITORING
 ENABLE ROW MOVEMENT
 PARTITION BY LIST ( audit_month )
 (
   PARTITION JAN       VALUES ('JAN'),
   PARTITION FEB       VALUES ('FEB'),
   PARTITION MAR       VALUES ('MAR'),
   PARTITION APR       VALUES ('APR'),
   PARTITION MAY       VALUES ('MAY'),
   PARTITION JUN       VALUES ('JUN'),
   PARTITION JUL       VALUES ('JUL'),
   PARTITION AUG       VALUES ('AUG'),
   PARTITION SEP       VALUES ('SEP'),
   PARTITION OCT       VALUES ('OCT'),
   PARTITION NOV       VALUES ('NOV'),
   PARTITION DEC       VALUES (DEFAULT)
 );


drop index idx_ee_launch_event_id;
create unique index idx_ee_launch_event_id on error_event(launch_event_id);
grant select, insert, update, delete on error_event to MONITOR_APP_USER;

insert into error_event
(
   error_event_id
  ,created_at
  ,launch_event_id
  ,risk_group
  ,batch
  ,rollup
  ,run_id
)
select
   error_event_id
  ,created_at
  ,launch_event_id
  ,risk_group
  ,batch
  ,rollup
  ,run_id
from error_event_old;

drop table error_event_old;
*/
