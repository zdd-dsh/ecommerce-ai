package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Product;

/*
 * 封装关于商品的返回给前端的视图数据
 * 使用场景：首页"猜你喜欢" "推销推荐"等功能模块，需要返回商品信息以及推荐算法的输出（评分、标签、理由）
 * DTO组合：
 * -- product: Product   来自数据库的实体数据
 * -- score: Double      算法计算的推荐评分
 * -- tag: String        算法生成的标签（"热销爆款" "新品推荐"）
 * -- reason: String     算法生成的推荐理由
 */
public class ProductShowcaseResponse {
    /*
     * 商品实体信息（包含id name price image等字段）
     */
    private Product product;

    /*
     * 算法计算的推荐评分
     */
    private Double score;

    private String tag;

    private String reason;

    public ProductShowcaseResponse(Product product) {

    }

    public ProductShowcaseResponse(Product product, Double score, String tag, String reason) {
        this.product = product;
        this.score = score;
        this.tag = tag;
        this.reason = reason;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}