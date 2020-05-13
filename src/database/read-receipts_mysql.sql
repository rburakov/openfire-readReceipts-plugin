CREATE TABLE readReceipt (
    username        VARCHAR(64)         NOT NULL,
    sender          VARCHAR(64)         NOT NULL,
    ts              INT(15),
    PRIMARY KEY pk_readReceipt (username, sender)
);

INSERT INTO ofVersion (name, version) VALUES ('read-receipts', 1);
