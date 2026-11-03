package com.example.ecommerce.dto;

import java.math.BigDecimal;

public class CartItemResponse {
    //购物车记录id，对应cart_item表的主键id
    private Long id;
    //所属用户id，标识这个购物车项属于哪个用户
    private Long userId;
    //商品id
    private Long productId;
    //商品名称
    private String productName;
    //商品单价
    private BigDecimal price;
    //商品图片
    private String image;
    //商品当前的库存
    private Integer stock;
    //购买数量
    private Integer quantity;
    //小计金额
    private BigDecimal subtotal;

    public CartItemResponse() {

    }

    public CartItemResponse(Long id, Long userId, Long productId, String productName, BigDecimal price, String image, Integer stock, Integer quantity, BigDecimal subtotal) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.image = image;
        this.stock = stock;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "CartItemResponse{" +
                "id=" + id +
                ", userId=" + userId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                ", image='" + image + '\'' +
                ", stock=" + stock +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
