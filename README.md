# Payment Systems Lab

一个可交互的支付系统知识项目：用状态机、持久化、幂等、Transactional Outbox、Webhook 重试和双式账本解释生产级支付系统的核心问题。

- **Live Demo:** https://xin898.github.io/WechatPayment/
- **Backend:** Java 21 + Spring Boot 3
- **Database:** PostgreSQL + Flyway
- **Local stack:** Docker Compose
- **Safety:** 全部交易由 Mock Provider 处理，不会产生真实扣款

> 仓库根目录原有的 `src/` 是早期微信支付 V2 接入代码，仅作历史参考。新实现位于 [payment-platform](./payment-platform)。

## 现在可以体验什么

在 Payment Lab 中可以：

1. 创建 Payment Intent。
2. 模拟成功、明确拒付或渠道超时。
3. 观察 `REQUIRES_CONFIRMATION → PROCESSING → 终态`。
4. 使用相同 Idempotency Key 重放请求。
5. 修改参数后复用同一个 Key，观察 `409 idempotency_conflict`。
6. 重复确认同一笔支付，验证不会产生第二次渠道请求。
7. 查看每次状态变化对应的领域事件。
8. 对成功支付执行部分或全额退款，观察退款状态机。
9. 检查支付与退款产生的等额借贷分录。
10. 观察 Outbox 从 `PENDING` 到 `PUBLISHED`，以及 Webhook 两次失败后的自动恢复。

GitHub Pages 使用浏览器内 Mock，便于公开体验；本地启动后，同一页面会自动连接 Spring Boot 与 PostgreSQL。

## 架构

~~~mermaid
flowchart TD
    Browser["Payment Lab"] --> API["Payment API"]
    API --> Idempotency["Idempotency Record"]
    API --> Payment["Payment State Machine"]
    Payment --> Provider["Mock / WeChat Provider"]
    Payment --> Events["Payment Events"]
    Idempotency --> DB[("PostgreSQL")]
    Payment --> DB
    Events --> DB
    Payment --> Ledger["Double-entry Ledger"]
    Payment --> Outbox["Transactional Outbox"]
    Outbox --> Webhook["Retrying Webhook"]
~~~

当前采用模块化单体，在代码内保持 API、Application、Domain、Infrastructure 和 Provider 的边界。等吞吐量或团队规模真正需要时再拆服务。

支付成功或退款时，业务状态、账本分录与 Outbox 事件在同一个 PostgreSQL 事务中提交。后台调度器只处理已提交的 Outbox，创建唯一的 Webhook Delivery；Demo 端点前两次返回 `503`，随后成功，用于展示可恢复投递。

## 核心设计

### 金额

所有金额使用最小货币单位，例如人民币分。业务代码禁止使用浮点数计算资金。

### 幂等

`Idempotency-Key` 与规范化请求的 SHA-256 指纹一起持久化：

- Key 相同、参数相同：返回第一次创建的 Payment Intent。
- Key 相同、参数不同：返回 `409 idempotency_conflict`。
- 数据库主键负责最终唯一性约束。

Demo 内还使用单实例锁缩小并发窗口；生产环境需要结合数据库冲突恢复或专门的幂等执行器。

### 状态机

~~~mermaid
stateDiagram-v2
    [*] --> REQUIRES_CONFIRMATION
    REQUIRES_CONFIRMATION --> PROCESSING
    PROCESSING --> SUCCEEDED
    PROCESSING --> FAILED
    PROCESSING --> PROCESSING: 结果未知
~~~

退款状态机为 `PENDING → SUCCEEDED | FAILED`。成功退款会原子地增加 `refunded_amount`，支付进入 `PARTIALLY_REFUNDED` 或 `REFUNDED`，超额退款会被拒绝。

### 双式账本

支付成功产生三条分录：借记渠道应收，贷记商户应付与平台手续费收入；退款产生等额反向资金分录。每个 `ledger_transaction` 在保存前验证借方总额等于贷方总额。

### Transactional Outbox 与 Webhook

领域操作不直接发 HTTP，而是在同一事务写入 `outbox_event`。发布器把事件转换成唯一的 `webhook_delivery`，投递器根据 `next_attempt_at` 重试。当前 Mock 策略用于教学：前两次 `503`，第三次 `200`。

重复确认已进入处理或终态的支付会直接返回当前结果，不会再次调用 Provider。

### 渠道超时

渠道超时不是支付失败。金额尾数为 `77` 时，Mock Provider 返回未知结果，支付保持 `PROCESSING`。真实系统需要由渠道回调、主动查询或对账任务收敛最终状态。

## API

| Method | Path | 说明 |
|---|---|---|
| POST | `/v1/payment-intents` | 创建支付，要求 `Idempotency-Key` |
| POST | `/v1/payment-intents/{id}/confirm` | 确认支付，可安全重试 |
| GET | `/v1/payment-intents/{id}` | 查询支付 |
| GET | `/v1/payment-intents/{id}/events` | 查询事件时间线 |
| POST | `/v1/payment-intents/{id}/refunds` | 创建幂等退款 |
| GET | `/v1/payment-intents/{id}/refunds` | 查询退款 |
| GET | `/v1/ledger/transactions?referenceId=...` | 查询平衡账本交易 |
| GET | `/v1/operations/outbox` | 查看 Outbox 状态 |
| GET | `/v1/operations/webhooks` | 查看 Webhook 投递与重试 |
| POST | `/v1/operations/webhooks/{id}/retry` | 手动重新激活失败投递 |

Mock Provider 规则：

| 金额尾数 | 结果 |
|---|---|
| 普通尾数 | `SUCCEEDED` |
| `02` | `FAILED` |
| `77` | `PROCESSING` |

## 本地运行

要求 Docker 与 Docker Compose：

~~~bash
git clone https://github.com/Xin898/WechatPayment.git
cd WechatPayment/payment-platform
docker compose up --build
~~~

打开 http://localhost:8080 。

也可以使用本地 Java 21 和 Maven 3.9+，但需要先准备 PostgreSQL：

~~~bash
cd payment-platform
mvn verify
mvn spring-boot:run
~~~

默认数据库配置：

~~~text
DATABASE_URL=jdbc:postgresql://localhost:5432/payment
DATABASE_USER=payment
DATABASE_PASSWORD=payment
~~~

这些默认值仅用于本地 Demo；生产环境必须使用 Secret Manager。

## 工程结构

~~~text
payment-platform/
├── src/main/java/com/xin/payment/
│   ├── api/
│   ├── application/
│   ├── domain/
│   ├── infrastructure/
│   └── provider/
├── src/main/resources/
│   ├── db/migration/
│   └── static/
├── src/test/
├── Dockerfile
└── compose.yml
~~~

## 路线图

### 已完成

- [x] Payment Intent 状态机
- [x] Mock Provider
- [x] 创建、确认、查询 API
- [x] PostgreSQL 持久化
- [x] Flyway Migration
- [x] 数据库幂等记录与请求指纹
- [x] 支付事件时间线
- [x] 交互式 Payment Lab
- [x] Docker Compose
- [x] GitHub Actions CI
- [x] GitHub Pages 发布流程
- [x] Transactional Outbox
- [x] Webhook 自动重试与手动重试
- [x] Refund 状态机与退款幂等
- [x] 双式账本与平衡校验

### 下一阶段：可靠性与资金系统

- [ ] 异步任务、重试与 Consumer Inbox
- [ ] Webhook HMAC 签名与密钥轮换
- [ ] 商户余额快照
- [ ] 结算、Payout 与对账
- [ ] OpenTelemetry、指标和审计日志
- [ ] 微信支付 API v3 Connector

## 安全说明

不要在仓库中提交真实卡号、CVV、API Secret、私钥或生产数据库凭证。旧工程历史中曾出现本地数据库密码；任何被复用过的密码都应轮换。
