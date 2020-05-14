CREATE TABLE readReceipt (
    userJID        VARCHAR(255)         NOT NULL,
    senderJID      VARCHAR(255)         NOT NULL,
    ts             INT(15),
    PRIMARY KEY pk_readReceipt (userJID, senderJID)
);

INSERT INTO ofVersion (name, version) VALUES ('read-receipts', 1);
