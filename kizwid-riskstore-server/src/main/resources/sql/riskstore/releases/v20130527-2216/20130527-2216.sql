create table DEAL (
    deal_id bigint not null,
    name varchar(255),
    creation_time date,
    creation_user bigint,
    primary key (deal_id)
):
create table REGION (
    region_id bigint not null,
    region_name varchar(255),
    primary key (region_id)
);
create table INDEX (
    index_id bigint not null,
    name varchar(255),
    primary key (index_id)
):
create table JOB_TYPE (
    job_type_id bigint not null,
    name varchar(255),
    primary key (job_type_id)
):
create table RESULT(
    result_id bigint not null,
    business_date integer,
    system varchar(255),
    creation_time timestamp,
    value double,
    text_value varchar(255),
    is_bad char(1),
    result_set_id bigint,
    deal_id bigint,
    result_type_id bigint,
    index_0 bigint,
    index_1 bigint,
    index_2 bigint,
    user_id bigint,
    primary key (result_id)
);
create table RESULT_SET (
    result_set_id bigint not null,
    scenario_step integer,
    business_date integer,
    region_id bigint,
    job_type bigint,
    scenario bigint,
    workspace bigint,
    primary key (result_set_id)
):
create table RESULT_TYPES (
    result_type_id bigint not null,
    name varchar(255),
    dimension_count integer,
    description varchar(255),
    value_type varchar(255),
    status varchar(255),
    last_updated date,
    index_key_0 varchar(255),
    index_key_1 varchar(255),
    index_key_2 varchar(255),
    updated_by bigint,
    primary key (result_type_id)
);
create table SCENARIOS (
    scenario_id bigint not null,
    name varchar(255),
    primary key (scenario_id)
):
create table USERS (
    user_id bigint not null,
    name varchar(255),
    description varchar(255),
    primary key (user_id)
):
create table BUSINESS (
    workspace_id bigint not null,
    name varchar(255),
    primary key (workspace_id)
):
alter table DEAL add constraint FK3DE9967F92FA17A foreign key (creation_user) references USERS;
alter table RESULT add constraint FK6C36C7D6221DDD7B foreign key (result_set_id) references RESULT_SET;
alter table RESULT add constraint FK6C36C7D67D3CE65E foreign key (user_id) references USERS;
alter table RESULT add constraint FK6C36C7D637B7A6C2 foreign key (index_0) references INDEX;
alter table RESULT add constraint FK6C36C7D637B7A6C3 foreign key (index_1) references INDEX;
alter table RESULT add constraint FK6C36C7D637B7A6C4 foreign key (index_2) references INDEX;
alter table RESULT add constraint FK6C36C7D67B6FF099 foreign key (result_type_id) references RESULT_TYPES;
alter table RESULT add constraint FK6C36C7D6E1CC16BE foreign key (deal_id) references DEAL;
create sequence DEAL_SEQ;
create sequence REGION_SEQ;
create sequence INDEX_SEQ;
create sequence JOB_TYPE_SEQ;
create sequence RESULT_SEQ;
create sequence RESULT_SET_SEQ;
create sequence RESULT_TYPES_SEQ;
create sequence SCENARIOS_SEQ;
create sequence USERS_SEQ;
create sequence BUSINESS_SEQ;