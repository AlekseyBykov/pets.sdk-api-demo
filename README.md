# SDK API Demo (Rate Limiter + Scheduled Sender)

Demonstrates how to **enforce API request limits** with a simple **fixed-window rate limiting strategy**.  
This project integrates a **client SDK** with two Spring Boot services, using a custom rate limiter and scheduled message sending.

Key ideas:
- The **SDK client** contains all HTTP + rate-limiting logic.
- Business services (`sender-service`, `receiver-service`) depend only on the SDK, not on HTTP details.
- Clear separation of concerns: services remain simple, while SDK enforces request limits.

## **Overview**

The demo consists of three modules:

1. **receiver-service** — REST API that receives messages (`POST /api/messages`).
2. **sender-service** — periodically sends batches of messages to the receiver.
3. **sdk-client** — a thin SDK library with a fixed-window **rate limiter**.

## **Workflow**
```
[sender-service] --(HTTP POST)--> [receiver-service]
[sdk-client] --(rate limiter)--> [HTTP client]
```
- **Receiver Service** — accepts messages and logs them.
- **Sender Service** — scheduled job that creates messages in parallel.
- **SDK Client** — wraps HTTP calls and enforces request limits.

## **How It Works**

- The **sender-service** has a scheduled job (`@Scheduled`) that fires every few seconds.
- For each batch, it spawns multiple parallel requests to the **receiver-service**.
- The **sdk-client** enforces a maximum number of requests per time window (default: **5 requests per second**) using a **custom rate limiter** based on `Semaphore`.
- The **receiver-service** logs every incoming message.

## **Configuration**

Example `sender-service/src/main/resources/application.yml`:

```yaml
sdk:
  base-url: http://localhost:8080
  window:
    unit: SECONDS
    amount: 1
  limit-per-window: 5

sender:
  batch:
    fixed-rate-ms: 5000
```

This means: at most 5 requests per second. If the sender produces more, the extra requests wait for the next time window.

## **Running locally**
1. Build the project:
```bash
mvn clean package 
```
2. Start the receiver-service:
```bash
mvn -pl receiver-service spring-boot:run
```
3. Start the sender-service (in another terminal): 
```
mvn -pl sender-service spring-boot:run
```
The sender will start sending batches of messages every 5 seconds.

## Example Logs

Receiver Service:
```bash
2025-08-22 15:41:14 INFO MessageController : Received: Message 36
2025-08-22 15:41:14 INFO MessageController : Received: Message 37
2025-08-22 15:41:14 INFO MessageController : Received: Message 38
2025-08-22 15:41:14 INFO MessageController : Received: Message 39
2025-08-22 15:41:14 INFO MessageController : Received: Message 40
--- next second (new window) ---
2025-08-22 15:41:15 INFO MessageController : Received: Message 41
2025-08-22 15:41:15 INFO MessageController : Received: Message 42
2025-08-22 15:41:15 INFO MessageController : Received: Message 43
2025-08-22 15:41:15 INFO MessageController : Received: Message 44
2025-08-22 15:41:15 INFO MessageController : Received: Message 45
```

Sender Service:
```bash
2025-08-22 15:41:14 INFO SenderJob : Sending batch...
2025-08-22 15:41:14 INFO SenderJob : Resp: OK
2025-08-22 15:41:14 INFO SenderJob : Resp: OK
2025-08-22 15:41:14 INFO SenderJob : Resp: OK
2025-08-22 15:41:14 INFO SenderJob : Resp: OK
2025-08-22 15:41:14 INFO SenderJob : Resp: OK
--- next second (window reset) ---
2025-08-22 15:41:15 INFO SenderJob : Resp: OK
2025-08-22 15:41:15 INFO SenderJob : Resp: OK
2025-08-22 15:41:15 INFO SenderJob : Resp: OK
2025-08-22 15:41:15 INFO SenderJob : Resp: OK
2025-08-22 15:41:15 INFO SenderJob : Resp: OK
```

Notice how only 5 requests per second are processed — the rest wait until the next time window.

## **Tech Stack**
- Java 21
- Spring Boot 3.3.x
- Maven
- Custom Rate Limiter (Semaphore-based)
- Scheduled Tasks

## License
MIT License
