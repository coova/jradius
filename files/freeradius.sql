CREATE TABLE radippool (
  id                    serial,
  pool_name             varchar(30) NOT NULL,
  framedipaddress       varchar(15) NOT NULL default '',
  nasipaddress          varchar(15) NOT NULL default '',
  calledstationid       VARCHAR(30) NOT NULL,
  callingstationid      VARCHAR(30) NOT NULL,
  expiry_time           DATETIME NULL default NULL,
  username              varchar(64) NOT NULL default '',
  pool_key              varchar(30) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE nas (
  id serial,
  nasname varchar(128) NOT NULL,
  nastype varchar(30) DEFAULT 'other',
  calledstationid varchar(64),
  shortname varchar(32),
  ports int(5),
  secret varchar(60),
  community varchar(50),
  description varchar(200) DEFAULT 'RADIUS Client',
  latitude double,
  longitude double,
  PRIMARY KEY (id),
  KEY nasname (nasname),
  KEY calledstationid (calledstationid)
);

CREATE TABLE radacct (
  id serial,
  acctsessionid varchar(64) NOT NULL default '',
  acctuniqueid varchar(64) NOT NULL default '',
  username varchar(64) NOT NULL default '',
  groupname varchar(64) NOT NULL default '',
  realm varchar(64) default '',
  nasipaddress varchar(64) NOT NULL default '',
  nasportid varchar(15) default NULL,
  nasporttype varchar(32) default NULL,
  acctstarttime datetime NULL default NULL,
  acctstoptime datetime NULL default NULL,
  acctsessiontime integer default NULL,
  acctauthentic varchar(32) default NULL,
  connectinfo_start varchar(64) default NULL,
  connectinfo_stop varchar(64) default NULL,
  acctinputoctets bigint(20) default NULL,
  acctoutputoctets bigint(20) default NULL,
  calledstationid varchar(64) NOT NULL default '',
  callingstationid varchar(64) NOT NULL default '',
  acctterminatecause varchar(32) NOT NULL default '',
  servicetype varchar(32) default NULL,
  framedprotocol varchar(64) default NULL,
  framedipaddress varchar(64) NOT NULL default '',
  acctstartdelay integer default NULL,
  acctstopdelay integer default NULL,
  xascendsessionsvrkey varchar(10) default NULL,
  PRIMARY KEY  (id),
  KEY username (username),
  KEY framedipaddress (framedipaddress),
  KEY acctsessionid (acctsessionid),
  KEY acctsessiontime (acctsessiontime),
  KEY acctuniqueid (acctuniqueid),
  KEY acctstarttime (acctstarttime),
  KEY acctstoptime (acctstoptime),
  KEY nasipaddress (nasipaddress)
);

CREATE TABLE radcheck (
  id serial,
  username varchar(64) NOT NULL default '',
  attribute varchar(64)  NOT NULL default '',
  op char(2) NOT NULL DEFAULT '==',
  value varchar(253) NOT NULL default '',
  PRIMARY KEY  (id),
  KEY username (username)
);

CREATE TABLE radgroupcheck (
  id serial,
  groupname varchar(64) NOT NULL default '',
  attribute varchar(64)  NOT NULL default '',
  op char(2) NOT NULL DEFAULT '==',
  value varchar(253)  NOT NULL default '',
  PRIMARY KEY  (id),
  KEY groupname (groupname)
);

CREATE TABLE radgroupreply (
  id serial,
  groupname varchar(64) NOT NULL default '',
  attribute varchar(64)  NOT NULL default '',
  op char(2) NOT NULL DEFAULT '=',
  value varchar(253)  NOT NULL default '',
  PRIMARY KEY  (id),
  KEY groupname (groupname)
);

CREATE TABLE radreply (
  id serial,
  username varchar(64) NOT NULL default '',
  attribute varchar(64) NOT NULL default '',
  op char(2) NOT NULL DEFAULT '=',
  value varchar(253) NOT NULL default '',
  PRIMARY KEY  (id),
  KEY username (username)
);

CREATE TABLE radusergroup (
  id serial,
  username varchar(64) NOT NULL default '',
  groupname varchar(64) NOT NULL default '',
  priority integer NOT NULL default '1',
  PRIMARY KEY  (id),
  KEY username (username)
);

CREATE TABLE radpostauth (
  id serial,
  username varchar(64) NOT NULL default '',
  pass varchar(64) NOT NULL default '',
  reply varchar(64) NOT NULL default '',
  authdate timestamp(14) NOT NULL,
  PRIMARY KEY  (id),
  KEY username (username)
);

CREATE TABLE uihelp (
  id serial,
  keyid varchar(64),
  helptext varchar(1000),
  PRIMARY KEY  (id),
  KEY keyid (keyid)
);

