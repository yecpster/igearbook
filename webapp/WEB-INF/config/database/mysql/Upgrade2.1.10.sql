CREATE TABLE jforum_groups_groups (
    group1_id INT NOT NULL,
    group2_id INT NOT NULL,
    relation INT  NOT NULL default '0',
    INDEX idx_group1 (group1_id),
    INDEX idx_group2 (group2_id),
    INDEX idx_relation (relation)
) ENGINE=InnoDB; 

ALTER TABLE jforum_groups ADD COLUMN group_type int DEFAULT 0;

update jforum_roles set name='perm_reply' where name='perm_read_only_forums';
update jforum_roles set name='perm_new_post' where name='perm_reply_only';