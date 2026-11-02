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
