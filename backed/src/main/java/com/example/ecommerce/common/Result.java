package com.example.ecommerce.common;

/*
* 统一的响应封装类
* 前端会发送请求的后端，前端会按照此类格式向前端响应
* 响应的格式如下：json格式的数据
* {
*       "success":true 是否成功
*       "code":200 状态码200 404路径错误
*       “message”："密码不正确"  结果的描述
*       "data":{....}  数据
*       "timestamp":1555 响应时间（单位是毫秒）
* }
* */
public class Result<T> {
    //序列化
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS = 200;//成功的状态码
    public static final int BAD_REQUEST_CODE = 400;//参数错误
    public static final int UNAUTHORIZED_CODE = 401;//未登录
    public static final int FORBIDDEN_CODE = 403;//无权限
    public static final int NOT_FOUND_CODE = 404;//路径或资源不存在
    public static final int CONFLICT_CODE = 409;//数据冲突
    public static final int ERROR_CODE = 500;//服务错误


    //实例变量
    private boolean success; //业务请求是否成功
    private int code;//业务状态码
    private String message;//结果的描述
    private T data;//数据
    private long timestamp;//响应时间

    //默认无参构造方法--自动设置时间（设置当前系统时间）
    public Result() {
        timestamp = System.currentTimeMillis();
    }

    //定义一些静态方法
    /*
    * 创建成功响应（携带数据）
    *
    * */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<T>();
        result.setSuccess(true);
        result.setCode(SUCCESS);
        result.setData(data);
        result.setMessage("success");


        return result;
    }
    //创建成功响应（不带数据）
    public static Result<Void> success() {
        return success(null);
    }
    //创建失败响应
    public static <T> Result<T> failure(int code, String message) {
        Result<T> result = new Result<T>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }


    //给当前类的私有属性全部添加get set方法


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
