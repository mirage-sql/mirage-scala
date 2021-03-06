CREATE TABLE BOOK (
	BOOK_ID   INT          IDENTITY PRIMARY KEY,
	BOOK_NAME VARCHAR(200) NOT NULL,
	AUTHOR    VARCHAR(200) NOT NULL,
	PRICE     INT
);

CREATE TABLE USER_INFO (
	USER_ID   INT          PRIMARY KEY,
	USER_NAME VARCHAR(200) NOT NULL
);

CREATE SEQUENCE USER_INFO_USER_ID_SEQ AS INTEGER;
