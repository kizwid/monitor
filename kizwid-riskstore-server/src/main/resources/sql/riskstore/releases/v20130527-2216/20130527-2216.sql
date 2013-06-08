create table DEALS (deal_id bigint not null, name varchar(255), creation_time date, creation_user bigint, primary key (deal_id)):
create table FOLDER (folder_id bigint not null, folder_name varchar(255), primary key (folder_id));
create table INDEX_DETAILS (index_id bigint not null, name varchar(255), primary key (index_id)):
create table JOB_TYPES (job_type_id bigint not null, name varchar(255), primary key (job_type_id)):
create table RESULTS     (result_id bigint not null, business_date integer, system varchar(255), creation_time timestamp, value double, text_value varchar(255), is_bad char(1), result_set_id bigint, deal_id bigint, result_type_id bigint, index_0 bigint, index_1 bigint, index_2 bigint, user_id bigint, primary key (result_id));
create table RESULT_SETS (result_set_id bigint not null, scenario_step integer, business_date integer, folder bigint, job_type bigint, scenario bigint, workspace bigint, primary key (result_set_id)):
create table RESULT_TYPES (result_type_id bigint not null, name varchar(255), dimension_count integer, description varchar(255), value_type varchar(255), status varchar(255), last_updated date, index_key_0 varchar(255), index_key_1 varchar(255), index_key_2 varchar(255), updated_by bigint, primary key (result_type_id));
create table SCENARIOS (scenario_id bigint not null, name varchar(255), primary key (scenario_id)):
create table USERS (user_id bigint not null, name varchar(255), description varchar(255), primary key (user_id)):
create table WORKSPACES (workspace_id bigint not null, name varchar(255), primary key (workspace_id)):
alter table DEALS add constraint FK3DE9967F92FA17A foreign key (creation_user) references USERS;
alter table RESULTS add constraint FK6C36C7D6221DDD7B foreign key (result_set_id) references RESULT_SETS;
alter table RESULTS add constraint FK6C36C7D67D3CE65E foreign key (user_id) references USERS;
alter table RESULTS add constraint FK6C36C7D637B7A6C2 foreign key (index_0) references INDEX_DETAILS;
alter table RESULTS add constraint FK6C36C7D637B7A6C3 foreign key (index_1) references INDEX_DETAILS;
alter table RESULTS add constraint FK6C36C7D67B6FF099 foreign key (result_type_id) references RESULT_TYPES;
alter table RESULTS add constraint FK6C36C7D637B7A6C4 foreign key (index_2) references INDEX_DETAILS;
alter table RESULTS add constraint FK6C36C7D6E1CC16BE foreign key (deal_id) references DEALS;
create sequence DEALS_SEQ;
create sequence FOLDER_SEQ;
create sequence INDEX_DETAILS_SEQ;
create sequence JOB_TYPES_SEQ;
create sequence RESULTS_SEQ;
create sequence RESULT_SETS_SEQ;
create sequence RESULT_TYPES_SEQ;
create sequence SCENARIOS_SEQ;
create sequence USERS_SEQ;
create sequence WORKSPACES_SEQ;