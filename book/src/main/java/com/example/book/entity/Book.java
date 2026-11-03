package com.example.book.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
    public class Book implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 主键自增
         */
        private Long id;

        /**
         * 图书名称，不能为空
         */
        private String bookName;

        /**
         * 作者
         */
        private String author;

        /**
         * 图书ISBN编号，唯一
         */
        private String isbn;

        /**
         * 馆藏可借数量 ≥0
         */
        private Integer stock;

        /**
         * 图书分类
         */
        private String category;

        /**
         * 录入时间
         */
        private Date createTime;
    }

