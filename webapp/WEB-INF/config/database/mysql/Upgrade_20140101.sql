update jforum_users set user_active=0 where user_active is null;
ALTER TABLE jforum_users MODIFY user_active tinyint(1) NOT NULL DEFAULT 0;

update jforum_users set deleted=0 where deleted is null;
ALTER TABLE jforum_users MODIFY deleted tinyint(1) NOT NULL DEFAULT 0;

ALTER TABLE jforum_users MODIFY user_viewemail tinyint(1) NOT NULL DEFAULT 0;

update jforum_users set user_karma=0 where user_karma is null;
ALTER TABLE jforum_users MODIFY user_karma double NOT NULL DEFAULT 0;