/*
--partition and default values not supported in HSQLDB

alter table pricing_error rename to pricing_error_old;

create table pricing_error (
    pricing_error_id number(38,0) primary key
    ,error_event_id number(38,0) not null
    ,dictionary varchar(50) not null
    ,market_data varchar(50) not null
    ,split varchar(100) not null
    ,error_message varchar(500) not null
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


drop index idx_pe;
create index idx_pe on pricing_error(error_event_id, pricing_error_id);
grant select, insert, update, delete on pricing_error to MONITOR_APP_USER;

insert into pricing_error
(
   pricing_error_id
  ,error_event_id
  ,dictionary
  ,market_data
  ,split
  ,error_message
)
select
   pricing_error_id
  ,error_event_id
  ,dictionary
  ,market_data
  ,split
  ,error_message
from pricing_error_old;

drop table pricing_error_old;
*/
