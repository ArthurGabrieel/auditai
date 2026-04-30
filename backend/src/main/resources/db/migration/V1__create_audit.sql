create table audit (
  id uuid primary key,
  time_log_content text not null,
  status varchar(20) not null,
  ai_opinion text null,
  error_reason text null,
  processing_attempts integer not null default 0,
  created_at timestamp not null,
  completed_at timestamp null,
  constraint ck_audit_status check (status in ('PENDING', 'PROCESSING', 'COMPLETED', 'ERROR'))
);

create index idx_audit_status on audit (status);
create index idx_audit_created_at on audit (created_at desc);
