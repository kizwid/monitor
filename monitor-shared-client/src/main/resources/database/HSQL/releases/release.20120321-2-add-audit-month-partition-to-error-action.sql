/*
--partition and default values not supported in HSQLDB
alter table error_action rename to error_action_old;

create table error_action (
    error_action_id number(38,0) primary key
    ,business_date number(38,0) not null
    ,updated_by varchar(10) not null
    ,updated_at timestamp not null
    ,action_comment varchar(255) not null
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


drop index idx_ea_business_date;
create index idx_ea_business_date on error_action(business_date);
grant select, insert, update, delete on error_action to MONITOR_APP_USER;

insert into error_action
(
    error_action_id
    ,business_date
    ,updated_by
    ,updated_at
    ,action_comment
)
select
    error_action_id
    ,business_date
    ,updated_by
    ,updated_at
    ,action_comment
from error_action_old;

drop table error_action_old;
*/
