create table pricing_run (
    pricing_run_id number(38,0) primary key
    ,created_at timestamp not null
    ,config_id varchar(250) not null
    ,run_label varchar(50) not null
    ,business_date integer not null
);

CREATE SEQUENCE pricing_run_seq;