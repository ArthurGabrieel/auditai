create type audit_status as enum ('PENDING', 'PROCESSING', 'COMPLETED', 'ERROR');

create table audit (
  id uuid primary key,
  time_log_content text not null,
  status audit_status not null,
  ai_opinion text null,
  error_reason text null,
  processing_attempts integer not null default 0,
  created_at timestamp not null,
  completed_at timestamp null
);

create index idx_audit_status on audit (status);
create index idx_audit_created_at on audit (created_at desc);
