/*
--partition and default values not supported in HSQLDB
alter table pricing_run rename to pricing_run_old;

create table pricing_run (
    pricing_run_id number(38,0) primary key
    ,created_at timestamp not null
    ,config_id varchar(250) not null
    ,run_label varchar(50) not null
    ,business_date integer not null
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

grant select, insert, update, delete on pricing_run to MONITOR_APP_USER;

insert into pricing_run
(
    pricing_run_id
    ,created_at
    ,config_id
    ,run_label
    ,business_date
)
select
    pricing_run_id
    ,created_at
    ,config_id
    ,run_label
    ,business_date
from pricing_run_old;

drop table pricing_run_old;
*/
