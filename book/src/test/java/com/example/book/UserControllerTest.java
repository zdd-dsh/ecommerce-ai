package com.example.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 不传name
    @Test
    void test1() throws Exception {
        String result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/user/add")
                        .contentType("application/json")
                        .content("{\"age\":20}")
        ).andReturn().getResponse().getContentAsString();
        System.out.println("不传name结果：" + result);
    }

    // age负数
    @Test
    void test2() throws Exception {
        String result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/user/add")
                        .contentType("application/json")
                        .content("{\"name\":\"张三\",\"age\":-5}")
        ).andReturn().getResponse().getContentAsString();
        System.out.println("age负数结果：" + result);
    }
}
