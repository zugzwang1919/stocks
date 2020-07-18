ALTER TABLE authority CHANGE authority role char(64);

ALTER TABLE security DROP FOREIGN KEY FK_ROLLUP_TO_SECURITY;
ALTER TABLE security DROP COLUMN securityType;
ALTER TABLE security DROP COLUMN securityGeography;
ALTER TABLE security DROP COLUMN rollupSecurity_id;

ALTER TABLE user DROP COLUMN enabled;

RENAME TABLE security TO stock;
ALTER TABLE transaction CHANGE security_id  stock_id int(11);

RENAME TABLE transaction to stocktransaction;

RENAME TABLE securityprice to stockprice;
ALTER TABLE stockprice CHANGE security_id stock_id int(11);