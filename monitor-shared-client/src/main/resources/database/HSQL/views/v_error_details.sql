create view v_error_details as
select
   nvl(ea.error_action_id,-1) error_action_id
  ,nvl(ea.business_date, to_number(to_char(sysdate,'YYYYMMDD'))) business_date
  ,nvl(ea.updated_by, '-') updated_by
  ,ea.updated_at
  ,nvl(ea.action_comment, 'New Errors') action_comment
  ,pr.pricing_run_id run_id
  ,pr.created_at run_created_at
  ,pr.config_id
  ,pr.run_label
  ,pr.business_date run_business_date
  ,ee.error_event_id
  ,ee.created_at error_created_at
  ,ee.launch_event_id
  ,ee.rollup
  ,ee.risk_group
  ,ee.batch
  ,pe.pricing_error_id
  ,pe.dictionary
  ,pe.market_data
  ,pe.split
  ,pe.error_message
from
   pricing_run pr
   inner join error_event ee on(pr.pricing_run_id = ee.run_id)
   inner join pricing_error pe on(pe.error_event_id = ee.error_event_id)
   left outer join (
    select iea.*,ieape.pricing_error_id
    from error_action iea, error_action_pricing_error ieape
    where iea.error_action_id = ieape.error_action_id
   ) ea on ( pe.pricing_error_id = ea.pricing_error_id)
;
grant select on v_error_details to MONITOR_APP_USER;
