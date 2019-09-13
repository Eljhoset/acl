
CREATE TABLE IF NOT EXISTS acl_sid (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  principal tinyint(1) NOT NULL,
  sid varchar(100) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY unique_uk_1 (sid,principal)
);

CREATE TABLE IF NOT EXISTS acl_class (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  class varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY unique_uk_2 (class)
);

CREATE TABLE IF NOT EXISTS acl_entry (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  acl_object_identity bigint(20) NOT NULL,
  ace_order int(11) NOT NULL,
  sid bigint(20) NOT NULL,
  mask int(11) NOT NULL,
  granting tinyint(1) NOT NULL,
  audit_success tinyint(1) NOT NULL,
  audit_failure tinyint(1) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY unique_uk_4 (acl_object_identity,ace_order)
);

CREATE TABLE IF NOT EXISTS acl_object_identity (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  object_id_class bigint(20) NOT NULL,
  object_id_identity bigint(20) NOT NULL,
  parent_object bigint(20) DEFAULT NULL,
  owner_sid bigint(20) DEFAULT NULL,
  entries_inheriting tinyint(1) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY unique_uk_3 (object_id_class,object_id_identity)
);

ALTER TABLE acl_entry
ADD FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity(id);

ALTER TABLE acl_entry
ADD FOREIGN KEY (sid) REFERENCES acl_sid(id);

--
-- Constraints for table acl_object_identity
--
ALTER TABLE acl_object_identity
ADD FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id);

ALTER TABLE acl_object_identity
ADD FOREIGN KEY (object_id_class) REFERENCES acl_class (id);

ALTER TABLE acl_object_identity
ADD FOREIGN KEY (owner_sid) REFERENCES acl_sid (id);



INSERT INTO consortium
    (id,name,status)
VALUES
    (1,'consortium test 1','ACTIVE'),
    (2,'consortium test 2','ACTIVE'),
    (3,'consortium test 3','DELETED');

INSERT INTO bettingshop
    (id,consortium,name,status)
VALUES
    (1,1,'bettingshop test 1','ACTIVE'),
    (2,1,'bettingshop test 2','ACTIVE'),
    (3,2,'bettingshop test 3','ACTIVE'),
    (4,2,'bettingshop test 4','DELETED');


INSERT INTO pos
    (id,bettingshop,name,status)
VALUES
    (1,1,'pos test 1','ACTIVE'),
    (2,1,'pos test 2','ACTIVE'),
    (3,3,'pos test 3','ACTIVE'),
    (4,3,'pos test 4','DELETED');

INSERT INTO acl_sid
    (id, principal, sid)
VALUES
  (1, 1, 'username');

INSERT INTO acl_class
    (id, class)
VALUES
  (1, 'com.example.testAcl.Consortium'),
  (2, 'com.example.testAcl.Bettingshop');


INSERT INTO acl_object_identity
  (id, object_id_class, object_id_identity,
  parent_object, owner_sid, entries_inheriting)
  VALUES
  (1, 2, 1, NULL, 1, 1),
  (2, 1, 2, NULL, 1, 1);

INSERT INTO acl_entry
    (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
VALUES
    (1,1,1,1,16,1,1,0),
    (2,1,2,1,1,1,1,0),
    (3,2,1,1,16,1,1,0),
    (4,2,2,1,1,1,1,0),
    (5,2,3,1,2,1,1,0);