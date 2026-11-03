package com.example.ecommerce.common;
/*
* 为什么要自定义异常类
* 业务功能违反了不是系统错误，需要返回友好的错误信息
* 1 系统错误 数组下标越界 空指针异常
* 2不是系统错误 用户名不正确 密码不正确 商品不存在
*
*
* */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    //业务状态码，与Result中的常量对应
    private  int code;

    //构造一个默认状态码为400的业务异常
    //@param message 异常信息
    public BusinessException(String message) {
    //使用this关键字调用当前类中其他的构造方法
        this(Result.BAD_REQUEST_CODE, message);
    }
    //构造指定状态码的业务异常
    //@param code
    //@param message
    public BusinessException(int code, String message) {
        super(message);//执行父类的构造完成消息的设置
        this.code = code;//直接赋值状态码
    }
    //给code添加get set方法
    //因为code被final修饰了,所以无法添加setCode方法，final修饰的变量不可变
    public int getCode() {
        return code;
    }


}
