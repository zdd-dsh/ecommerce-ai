package com.example.ecommerce.common;

import java.util.List;

//分页响应封装类
/*
 * 分页包含
 *       items:当前页的数据列表
 *       total：总记录数
 *       page：当前页
 *       size:每页条数
 * */
public class PagedResponse<T> {
    private List<T> items;
    private long total;
    private int page;
    private int size;

    public PagedResponse() {
    }

    public PagedResponse(List<T> items, long total, int page, int size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    // 新增静态of工厂方法，给service调用
    public static <T> PagedResponse<T> of(List<T> records, long total, int page, int size){
        return new PagedResponse<>(records, total, page, size);
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
