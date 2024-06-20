INSERT INTO customer_info (user_id,first_name,email,phone)
VALUES
('1234','joko','cp@example.com','62084532435644')
;

INSERT INTO item (item_id,price,quantity,item_name)
VALUES
('1234',10000,2,'mie bakso'),
('5432',5000,4,'mie goreng')
;

INSERT INTO transact (order_id,transact_id,gross_amount,currency,transact_on,transact_method,transact_status,customer_id,message)
VALUES
('order-1','12345',20000,'IDR','2021-12-01 14:30:15','alfamart','pending','1234','hai'),
('order-2','54321',20000,'IDR','2021-12-05 14:30:15','alfamart','pending','1234','hello')
;

INSERT INTO transact_with_item (order_id,item_id)
VALUES
('order-1',(SELECT id FROM item WHERE item_id = '1234')),
('order-2',(SELECT id FROM item WHERE item_id = '5432'))
;