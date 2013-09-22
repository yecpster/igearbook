CREATE TABLE jforum_recommendation (
  recommend_id INT NOT NULL auto_increment,
  recommend_type tinyint(3) default '0',
  image_url varchar(255) default NULL,
  topic_id INT NOT NULL default '0',
  topic_title varchar(100) NOT NULL default '',
  create_user_id INT NOT NULL default '0',
  last_update_user_id INT NOT NULL default '0',
  create_time datetime default null,
  last_update_time datetime default null,
  PRIMARY KEY  (recommend_id)
) ENGINE=InnoDB;