package com.example.book.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LogAspect {

    // 切点：所有Controller下的方法
    @Pointcut("execution(* com.example.book.controller..*.*(..))")
    public void logPointCut() {}

    // 环绕通知（同时做前置和后置）
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 前置：打印请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        log.info("========== 请求开始 ==========");
        log.info("请求地址：{}", request.getRequestURL());
        log.info("请求方式：{}", request.getMethod());
        log.info("请求参数：{}", Arrays.toString(joinPoint.getArgs()));

        // 执行方法，记录耗时
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        // 后置：打印返回结果和耗时
        log.info("返回结果：{}", result);
        log.info("接口耗时：{} ms", (endTime - startTime));
        log.info("========== 请求结束 ==========");

        return result;
    }
}
