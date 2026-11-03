# 电商系统（Spring Boot + Vue3）｜集成 Spring AI 流式智能导购

一个前后端分离的电商系统，覆盖商品、购物车、订单、地址、鉴权等主流程，并在此基础上接入 **Spring AI** 实现了可流式输出的 **AI 商品导购** 接口。

后端 Spring Boot 3.4 + MyBatis + MySQL + Redis + RabbitMQ，前端 Vue3 + Element Plus。

---

## 功能特性

### 业务功能

- **商品**：分页查询、关键词/类目/价格区间筛选、多字段排序、商品详情、Excel 批量导入
- **用户**：注册登录、JWT 鉴权、个人信息与密码修改、手机号/邮箱更新
- **购物车**：加购、改数量、删除、结算前校验库存
- **订单**：下单（校验并扣减库存）、订单列表、订单详情、状态流转
- **收货地址**：增删改查、默认地址切换
- **支付**：模拟支付（已预留支付宝沙箱接入位）

### 工程能力

- **统一响应体**：`common/Result` + `common/PagedResponse`
- **全局异常处理**：`GlobalExceptionHandler` 统一转换为标准错误结构
- **安全**：Spring Security + 自定义 JWT 过滤器，XSS 过滤器
- **消息队列**：RabbitMQ 订单事件（主交换机 / 重试队列 / 死信队列）
- **缓存**：Redis 缓存热点商品，更新即主动失效
- **接口文档**：springdoc-openapi，访问 `/swagger-ui.html`

### AI 能力（本项目重点）

- `POST /api/ai/chat` **流式返回**（SSE），边生成边推送
- **注入商品上下文**：把在售商品的名称 / 价格 / 库存拼进 system prompt，让模型基于真实数据回答，而不是泛泛而谈
- **防御式装配**：模型未配置时不影响应用启动，调用时降级为明确的业务异常

---

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3.4.0 / JDK 17 |
| 持久层 | MyBatis 3.0.4（XML Mapper）+ MySQL |
| 安全 | Spring Security + 自定义 JWT |
| 缓存 | Redis（Spring Cache） |
| 消息 | RabbitMQ（重试 + 死信） |
| AI | Spring AI 1.0.0（OpenAI 兼容协议） |
| 接口文档 | springdoc-openapi |
| 前端 | Vue3 + vue-router + Element Plus + Axios + Vite |
| 构建 | Maven / npm |

---

## 项目结构

```
.
├── backed/                     # 后端
│   ├── src/main/java/com/example/ecommerce/
│   │   ├── common/             # 统一响应体、异常处理、XSS 过滤
│   │   ├── config/             # Security / Redis / RabbitMQ 配置
│   │   ├── controller/         # Auth / Product / Cart / Order / Address / AI
│   │   ├── dto/  entity/  mapper/
│   │   ├── messaging/          # 订单事件发布与监听
│   │   ├── security/           # JWT 生成、校验、过滤器
│   │   ├── service/            # 业务接口与实现
│   │   └── utils/              # Excel 工具、Redis 工具
│   └── src/main/resources/
│       ├── mappers/            # MyBatis XML
│       ├── application.yml     # 公共配置
│       ├── application-dev.yml # 开发环境配置
│       └── application-local.yml  # 本地私密配置（已 gitignore）
└── frontend/                   # 前端 Vue3
    └── src/{views,components,router,utils}
```

---

## 快速开始

### 1. 环境要求

- JDK 17+、Maven 3.6+
- MySQL 5.7+ / 8.0
- Redis、RabbitMQ（AI 功能不依赖 Redis / RabbitMQ，但业务模块需要）
- Node.js 18+

### 2. 初始化数据库

```bash
mysql -u root -p -e "source /path/to/backed/db.sql"
```

### 3. 配置

数据库账号密码、AI 模型 Key 都放在 `application-local.yml`（该文件已被 `.gitignore` 忽略，不会提交）：

```yaml
spring:
  datasource:
    username: root
    password: 你的数据库密码
  ai:
    openai:
      api-key: sk-你的模型APIKey
```

模型地址与模型名在 `application-dev.yml` 配置，任何 **OpenAI 兼容** 的服务都可以：

```yaml
spring:
  ai:
    openai:
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat
```

### 4. 启动后端

```bash
cd backed
mvn spring-boot:run
```

访问接口文档：http://localhost:8080/swagger-ui.html

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

---

## AI 导购接口

```bash
curl -N -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"有没有降噪耳机推荐"}'
```

响应为 SSE 流式：

```
data:平台
data:上
data:有
data:几
data:款
...
```

**设计说明**

| 点 | 说明 |
|---|---|
| 流式 | `SseEmitter` + Spring AI `ChatClient.stream()`，逐 chunk 推送 |
| 上下文 | 每次请求查询在售商品（最多 20 条）拼进 system prompt |
| 降级 | 无可用模型时抛 `BusinessException`，由全局异常处理器统一返回 |
| 鉴权 | 当前为演示方便匿名放行，生产环境应改为 JWT 鉴权 |

---

## 已知问题 / 后续计划

- [ ] 现有测试为开发期遗留，不具备幂等性（直接写库、硬编码唯一键），计划重构为可重复执行的单元测试与切片测试
- [ ] 商品上下文当前为全表查询后内存过滤，数据量上涨后需改为带条件的 SQL 或加缓存
- [ ] SSE 错误分支目前直接断开连接，计划改为推送 error 事件以便前端展示原因
- [ ] 计划引入 function calling：让模型能查询库存、订单状态
- [ ] 计划引入向量检索（RAG），支持按商品描述语义搜索

---

## License

MIT
