package com.example.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookAddDTO {

    /**
     * 图书名称，非空
     */
    @NotBlank(message = "图书名称不能为空")
    private String bookName;

    /**
     * 作者
     */
    private String author;

    /**
     * 图书ISBN编号，非空，唯一
     */
    @NotBlank(message = "ISBN不能为空")
    private String isbn;

    /**
     * 馆藏可借数量，不能小于0
     */
    @Min(value = 0, message = "库存数量不能小于0")
    private Integer stock;

    /**
     * 图书分类
     */
    private String category;
}