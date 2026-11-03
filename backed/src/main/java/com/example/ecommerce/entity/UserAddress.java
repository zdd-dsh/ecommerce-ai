package com.example.ecommerce.entity;

import lombok.Data;

import java.util.Date;
@Data
public class UserAddress {
    private Long id;             // 主键
    private Long userId;         // 用户
    private String receiver;     // 收货
    private String phone;        // 电话
    private String province;     // 省
    private String city;         // 市
    private String district;     // 区
    private String detailAddress;
    private Integer isDefault;
    private Date createTime;
    private Date updateTime;

    public String getFullAddress(){
        StringBuffer buf = new StringBuffer();
        //调用appendPart方法（为空的判断）
        appendPart(buf,province);    //拼接的省
        appendPart(buf,city);        //拼接的市
        appendPart(buf,district);    //拼接的区县
        appendPart(buf, detailAddress); //拼接的详细地址
        return buf.toString();


    }

    private void appendPart(StringBuffer buf, String value){
        if(value == null || value.trim().isEmpty()){
            return;   //空返回，啥也不做，到此结束
        }
        buf.append(value);

    }
}
