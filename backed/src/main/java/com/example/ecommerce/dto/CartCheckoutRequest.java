package com.example.ecommerce.dto;

import java.util.List;

public class CartCheckoutRequest {
    private Long addressId;
    private List<Long> cartItemIdList; // 新增：选中要结算的购物车项ID列表

    public CartCheckoutRequest() {
    }

    public CartCheckoutRequest(Long addressId) {
        this.addressId = addressId;
    }

    public CartCheckoutRequest(Long addressId, List<Long> cartItemIdList) {
        this.addressId = addressId;
        this.cartItemIdList = cartItemIdList;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public List<Long> getCartItemIdList() {
        return cartItemIdList;
    }

    public void setCartItemIdList(List<Long> cartItemIdList) {
        this.cartItemIdList = cartItemIdList;
    }

    @Override
    public String toString() {
        return "CartCheckoutRequest{" +
                "addressId=" + addressId +
                ", cartItemIdList=" + cartItemIdList +
                '}';
    }
}
