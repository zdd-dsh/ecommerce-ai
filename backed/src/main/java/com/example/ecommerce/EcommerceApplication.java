package com.example.ecommerce;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
 * 后端SpringBoot 启动的主类
 *@SpringBootApplication=@Configuration + @EnableAutoConfiguration + @ComponentScan
 *@EnableScheduling 启动定时任务（缓存的刷新）
 */

@SpringBootApplication
@EnableScheduling
public class EcommerceApplication {
    public static  void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
