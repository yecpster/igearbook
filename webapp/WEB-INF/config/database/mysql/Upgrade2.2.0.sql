ALTER TABLE jforum_user_groups
ADD CONSTRAINT unique_user_group UNIQUE (group_id,user_id);

ALTER TABLE jforum_forums
ADD COLUMN forum_type TINYINT DEFAULT 0;

ALTER TABLE jforum_categories
ADD COLUMN category_type TINYINT DEFAULT 0;

ALTER TABLE jforum_forums CHANGE forum_logo forum_logo VARCHAR(255);