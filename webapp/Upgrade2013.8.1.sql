CREATE TABLE jforum_groups_groups (
    group1_id INT NOT NULL,
    group2_id INT NOT NULL,
    relation INT  NOT NULL default '0',
    INDEX idx_group1 (group1_id),
    INDEX idx_group2 (group2_id),
    INDEX idx_relation (relation)
) ENGINE=InnoDB;