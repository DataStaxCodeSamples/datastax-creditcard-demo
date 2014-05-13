create keyspace if not exists datastax_transactions_demo WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1' };

use datastax_transactions_demo;

create table  if not exists products(
    productId text,
    capacityleft int,
	orderIds set<text>,
	PRIMARY KEY (productId)
);

create table  if not exists buyers_orders(
	buyerId text,
	orderId text,
	productId text,
	PRIMARY KEY(buyerId, orderId)
);
	
truncate products;
truncate buyers_orders;