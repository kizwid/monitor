--begin execute immediate 'drop table database_release'; exception when others then null; end;
create table database_release(
    script varchar(100) primary key
    ,deployed_at timestamp not null
)
