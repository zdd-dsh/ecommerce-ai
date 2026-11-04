# 项目说明

Spring Boot 3.4 + Vue3 前后端分离电商系统。JDK 17，Maven。

仓库结构：backed/（后端）、frontend/（前端）、book/（旧实验模块，勿改）



## 技术栈

- 后端：Spring Boot 3.4 / Spring Security + JWT / MyBatis(XML) / MySQL / Redis / RabbitMQ / Spring AI

- 前端：Vue3 + vue-router + Element Plus + Axios + Vite



## 代码约定

- 分层：Controller → Service(接口) → ServiceImpl → Mapper(接口) + resources/mappers/*.xml

- 统一返回：common/Result；分页：common/PagedResponse

- 异常：业务异常抛 common/BusinessException，由 GlobalExceptionHandler 统一处理

- 金额一律 BigDecimal，禁止 double

- SQL 一律写在 resources/mappers/*.xml，禁止注解拼 SQL



## 常用命令

- 编译：mvn -q -DskipTests compile

- 测试：mvn test

- 启动：mvn spring-boot:run



## 测试约定

现有测试直接写库且无事务回滚，第二次跑会因唯一索引冲突失败。

新增测试必须：加 @Transactional 回滚，或用 @WebMvcTest/@MybatisTest 切片；

唯一键用 UUID 生成，禁止硬编码。



## 禁止

- 不要修改 common/ 下 Result、PagedResponse、GlobalExceptionHandler 的对外契约

- 一次改动不超过 3 个文件；改完必须能编译通过
