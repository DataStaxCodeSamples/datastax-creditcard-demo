create keyspace if not exists datastax_creditcard_demo WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1' };

use datastax_creditcard_demo;

CREATE TABLE if not exists credit_card_transactions_balance (
 credit_card_no text,
 balance_at timestamp static,
 balance double static,
 transaction_time timestamp,
 transaction_id text,
 location text,
 items map<text, double>,
 issuer text,
 amount float,
 PRIMARY KEY (credit_card_no, transaction_time)
) WITH CLUSTERING ORDER BY (transaction_time DESC);

CREATE TABLE if not exists credit_card_transactions_by_issuer_date (
 issuer text,
 date text, //format yyyymmdd
 transaction_id text,
 credit_card_no text,
 items map<text, double>,
 transaction_time timestamp,
 location text,
 amount float,
 PRIMARY KEY ((issuer, date), transaction_id)
) WITH CLUSTERING ORDER BY (transaction_id DESC);
	
	
create table if not exists transaction_date_minute_counter (
	date text,
	minute int,
	total_for_minute counter,
	PRIMARY KEY  (date, minute)	
);
	
truncate credit_card_transactions_balance;
truncate credit_card_transactions_by_issuer_date;
truncate transaction_date_minute_counter;