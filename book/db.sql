CREATE TABLE `book` (
                        `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键自增',
                        `book_name`   VARCHAR(100) NOT NULL                COMMENT '图书名称，不能为空',
                        `author`      VARCHAR(100) DEFAULT NULL            COMMENT '作者',
                        `isbn`        VARCHAR(50)  NOT NULL                COMMENT '图书ISBN编号，唯一',
                        `stock`       INT          NOT NULL DEFAULT 0      COMMENT '馆藏可借数量 ≥0',
                        `category`    VARCHAR(50)  DEFAULT NULL            COMMENT '图书分类',
                        `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_isbn` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';
-- 插入测试图书数据（共10条，覆盖不同分类，ISBN唯一，stock≥0）
INSERT INTO `book` (`book_name`, `author`, `isbn`, `stock`, `category`) VALUES
                                                                            ('Java编程思想', 'Bruce Eckel', '9787111213826', 12, '计算机'),
                                                                            ('深入理解Java虚拟机', '周志明', '9787111421900', 8, '计算机'),
                                                                            ('MySQL必知必会', 'Ben Forta', '9787115191120', 15, '数据库'),
                                                                            ('Redis设计与实现', '黄健宏', '9787111613459', 6, '数据库'),
                                                                            ('Spring实战', 'Craig Walls', '9787115417305', 10, '计算机'),
                                                                            ('算法导论', 'Thomas H.Cormen', '9787111407010', 3, '计算机'),
                                                                            ('活着', '余华', '9787506365437', 20, '文学'),
                                                                            ('三体', '刘慈欣', '9787536692930', 25, '文学'),
                                                                            ('人类简史', '尤瓦尔·赫拉利', '9787508647357', 7, '历史'),
                                                                            ('经济学原理', '曼昆', '9787301256404', 0, '经济学');
-- 先删表再建，避免已存在报错
DROP TABLE IF EXISTS `book`;

CREATE TABLE `book`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `book_name`   VARCHAR(100) NOT NULL COMMENT '图书名称，不能为空',
    `author`      VARCHAR(100)          DEFAULT NULL COMMENT '作者',
    `isbn`        VARCHAR(50)  NOT NULL COMMENT '图书ISBN编号，唯一',
    `stock`       INT          NOT NULL DEFAULT 0 COMMENT '馆藏可借数量 ≥0',
    `category`    VARCHAR(50)           DEFAULT NULL COMMENT '图书分类',
    `create_time` TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_isbn` (`isbn`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='图书表';

CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键id',
                        `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
                        `age` INT DEFAULT NULL COMMENT '年龄',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

