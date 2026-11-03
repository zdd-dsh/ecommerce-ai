package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressRequest {
    @NotBlank(message = "收货人不能为空")
    @Size(max = 50,message = "收货人长度不能超过50位")
    private String receiver;// 收货
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$",message = "手机号不正确")
    private String phone;
    @NotBlank(message = "省份不能为空")// 电话
    private String province;
    @NotBlank(message = "城市不能为空")// 省
    private String city;         // 市
    @NotBlank(message = "区县不能为空")
    private String district;     // 区
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址不能超过255位")
    private String detailAddress;

    private Boolean isDefault;

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
