package com.example.ecommerce.dto;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public class ProductQueryRequest {
    /*
     * 当前页码，从1开始，初始值也是第1页
     */
    private Integer page = 1;

    /*
     * 每页数量，默认值是8条
     */
    private Integer pageSize = 8;

    /*
     * 搜索关键词，匹配商品名称或描述（如果keyword为null或空字符串不过滤）
     */
    private String keyword;

    /*
     * 分类ID筛选
     */
    private Integer categoryId;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private String sortBy ="latest";

    private String sortDirection ="desc";
    public int getNormalizedPage(){
        return page == null || page<1 ? 1 : page;
    }


    public int getNormalizedPageSize(){
        if(pageSize == null || pageSize < 1){
            return 8;
        }
        return Math.min(pageSize, 50);
    }


    public int getOffset(){
        return (getNormalizedPage() -1 ) * getNormalizedPageSize();
    }

    public String getSanitizedKeyword() {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    public String getNormalizedSortBy(){
        if(!StringUtils.hasText(sortBy)){
            return "latest";
        }
        //将sortBy的值去掉两端空格，并转换为小写字母
        String s=sortBy.trim().toLowerCase();
        if("price".equals(s) || "stock".equals(s) || "name".equals(s)){
            return s;
        }
        return "latest";
    }
    public String getNormalizedSortDirection(){
        return "asc".equalsIgnoreCase(sortDirection) ? "asc" : "desc";
    }


    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public String toString() {
        return "ProductQueryRequest{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", keyword='" + keyword + '\'' +
                ", categoryId=" + categoryId +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}
