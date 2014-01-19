ALTER TABLE jforum_users DROP apiUser;
ALTER TABLE jforum_users DROP apiUserActive;
ALTER TABLE jforum_users MODIFY user_email varchar(255) NULL;