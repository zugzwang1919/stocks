-- EWG Stock Split
DELETE from stocksplit WHERE afteramount=60 AND beforeamount=1 AND date='2014-05-07';
-- JCI Stock Split
DELETE from stocksplit WHERE afteramount=10000 AND beforeamount=4971 AND date='2012-10-01';