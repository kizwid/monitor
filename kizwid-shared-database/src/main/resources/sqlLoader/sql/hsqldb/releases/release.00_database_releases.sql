--apply to bring hsqldb inline with oracle syntax
set database sql syntax ORA true;

create table database_release(
    script varchar(100) primary key
    ,deployed_at timestamp not null
);