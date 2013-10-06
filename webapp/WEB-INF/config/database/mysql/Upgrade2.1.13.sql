ALTER TABLE jforum_user_groups
ADD CONSTRAINT unique_user_group UNIQUE (group_id,user_id);

ALTER TABLE jforum_forums
ADD COLUMN forum_type TINYINT DEFAULT 0;