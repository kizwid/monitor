/*
--partition and default values not supported in HSQLDB
alter table error_action_pricing_error rename to error_action_pricing_error_old;

create table error_action_pricing_error (
    error_action_id number(38,0) not null
    ,pricing_error_id number(38,0) not null
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

grant select, insert, update, delete on error_action_pricing_error to MONITOR_APP_USER;

insert into error_action_pricing_error
(
    error_action_id
    ,pricing_error_id
)
select
    error_action_id
    ,pricing_error_id
from error_action_pricing_error_old;

drop table error_action_pricing_error_old;
*/
