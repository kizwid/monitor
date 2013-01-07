--begin execute immediate 'drop table database_release'; exception when others then null; end;
create table database_release(
    script varchar(100) primary key
    ,deployed_at timestamp not null
);

/*
--rollback
begin execute immediate 'drop table database_release'; exception when others then null; end;

drop table database_release
drop table error_event
drop sequence error_event_seq
drop table pricing_error
drop sequence pricing_error_seq
drop table error_action
drop sequence error_action_seq
drop table error_action_pricing_error
drop table pricing_run
drop sequence pricing_run_seq
*/
