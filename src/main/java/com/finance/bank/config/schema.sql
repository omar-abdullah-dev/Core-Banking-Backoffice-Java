CREATE TYPE emplyee_role_enum AS ENUM ('CS','TELLER','MANAGER');
CREATE TYPE account_type_enum AS ENUM ('SAVINGS','CURRENT');
CREATE TYPE transaction_type_enum AS ENUM ('DEPOSIT','WITHDRAWAL','TRANSFER');

CREATE TABLE employees (
	system_id VARCHAR(50) PRIMARY KEY,
	username VARCHAR (100) UNIQUE NOT NULL ,
	national_id VARCHAR(14) UNIQUE NOT NULL,
	password VARCHAR(255) NOT NULL,
	role emplyee_role_enum NOT NULL ,
	email VARCHAR(255) ,
	phone VARCHAR (20),
	created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE customers (
	system_id VARCHAR(50) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	national_id  VARCHAR(14) UNIQUE NOT NULL,
	email VARCHAR(255) ,
	phone VARCHAR(20),
	created_at TIMESTAMP NOT NULL DEFAULT now()	
);

CREATE TABLE accounts (
	account_number VARCHAR(16)     PRIMARY KEY,
	account_type account_type_enum NOT NULL,
	balance NUMERIC(19,2) NOT NULL DEFAULT 0.00,
	overdraft_limit NUMERIC(19, 2)     DEFAULT NULL,
	customer_id VARCHAR(50)        NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT now(),
	CONSTRAINT fk_account_customer
        FOREIGN KEY (customer_id) REFERENCES customers(system_id)
)

CREATE TABLE transactions (
	transaction_id VARCHAR(50) PRIMARY KEY,
	account_number VARCHAR(16) NOT NULL ,
	transaction_type transaction_type_enum NOT NULL,
	amount NUMERIC(19,2) CHECK (amount > 0) NOT NULL,
	fee NUMERIC(19,2) NOT NULL DEFAULT 0.00,
	total NUMERIC (19,2) NOT NULL CHECK (total > 0) ,
	balance_after NUMERIC (19,2) NOT NULL ,
	timestamp TIMESTAMP NOT NULL DEFAULT now(),
	performed_by_employee_id VARCHAR(50) NOT NULL,
	performed_by_name  VARCHAR(255) NOT NULL ,
	performed_by_role emplyee_role_enum NOT NULL,
	CONSTRAINT fk_transaction_account
		FOREIGN KEY (account_number) REFERENCES accounts(account_number),
	CONSTRAINT fk_transaction_employee
		FOREIGN KEY (performed_by_employee_id) REFERENCES employees(system_id)
);

INSERT INTO employees (system_id, username, national_id, password, role)
	VALUES 
	  (gen_random_uuid()::text,'omar','30212121700915', 'omarPass!','CS'),
	  (gen_random_uuid()::text,'ahmed','30111111700915', 'ahmedPass!','MANAGER'),
	  (gen_random_uuid()::text, 'mohamed', '30111111700916', 'mohamedPass!', 'TELLER'),
	  (gen_random_uuid()::text, 'manager', '29505051234567', 'manager123', 'MANAGER'),
	  (gen_random_uuid()::text, 'teller', '29505051234568', 'teller123', 'TELLER'),
	  (gen_random_uuid()::text, 'cs', '29505051234569', 'cs123456', 'CS');

