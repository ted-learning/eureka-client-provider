# eureka-client-provider

A demo if Spring cloud eureka client with actuator /pause and /resume

Netflix Eureka

服务向 Eureka注册，然后每 30 秒发送一次心跳更新其租约。如果客户端无法续订几次租约，它会在大约 90 秒内从服务器注册表中取出。注册信息和续订被复制到集群中的所有 eureka 节点。来自任何区域的客户端都可以查找注册表信息（每 30 秒发生一次）以定位其服务（可能位于任何区域）并进行远程调用。

Eurka 保证 AP
Eureka Server 各个节点都是平等的，几个节点挂掉不会影响正常节点的工作，剩余的节点依然可以提供注册和查询服务。而 Eureka Client 在向某个 Eureka 注册时，如果发现连接失败，则会自动切换至其它节点。只要有一台 Eureka Server 还在，就能保证注册服务可用(保证可用性)，只不过查到的信息可能不是最新的(不保证强一致性)。
Eureka Client/Server 通信流程：
1. 初始化：Starting Eureka Client
2. 登记：将实例信息注册到Eureka Server（注册发生在第一次心跳后，默认30秒）
3. 更新：每30秒发送一次心跳来更新租约，如果Server在90秒内没有看到续订，它会将实例从其注册表中删除
4. 获取注册表：Eureka Client 从 Server 获取注册信息并在本地缓存（定期30秒，采用增量更新，采用jersey apache压缩Json信息）
5. 取消：在关闭时，Client 向 Server 发送取消请求，将实例从服务器的实例注册表中删除
6. 时滞 Time Lag：Server和Client都是定时刷新，更改传播到所有Eureka Client 需要滞后

Eureka Server 自我保护：
任何连续 3 次心跳更新失败的客户端都被认为是”不干净"的终止，并将被后台驱逐过程驱逐。
当前注册表的 > 15% 处于此状态时，Server将启用自我保护模式。
（”不干净”的终止：没有明确注销操作的终止）

当处于自我保存模式时，Eureka Server将停止驱逐所有实例，直到：
1. 它看到的心跳更新次数回到预期阈值之上
2. 自我保护被禁用（见下文）

确保灾难性的网络事件不会清除 eureka 注册表数据，并将其传播到下游的所有客户端。

Peers 之间的网络中断期间会发生什么？
在对等点之间发生网络中断的情况下，可能会发生以下情况:
* 对等点之间的心跳复制可能会失败，服务器会检测到这种情况并进入保护当前状态的自我保护模式。
* 注册可能发生在孤立的服务器中，一些客户端可能会反映新的注册，而其他客户端可能不会。
在网络连接恢复到稳定状态后，情况会自动更正。当对等点能够正常通信时，注册信息会自动传输到没有它们的服务器。底线是，在网络中断期间，服务器会尽可能地保持弹性，但在此期间客户端可能对服务器有不同的看法。


Spring Cloud Eureka 

Spring Cloud Eureka Client

spring-cloud-starter-netflix-eureka-client






* service ID: ${spring.application.name}
* virtual host: ${spring.application.name}
* non-secure port: ${server.port}

Eureka Server支持TLS或更复杂的身份认证方式



通过Health check检查健康状态
默认通过client的心跳判定client是否健康，可以启动healthcheck，以”UP“状态判定服务的状态



Status & Health
默认使用Actuator的/Info和/Health进行状态和健康检查


Health


Info


 EurekaClient with Jersey
By default, EurekaClient uses Spring’s RestTemplate for HTTP communication. If you wish to use Jersey instead, you need to add the Jersey dependencies to your classpath:
<dependency>
    <groupId>com.sun.jersey</groupId>
    <artifactId>jersey-client</artifactId>
</dependency>
<dependency>
    <groupId>com.sun.jersey</groupId>
    <artifactId>jersey-core</artifactId>
</dependency>
<dependency>
    <groupId>com.sun.jersey.contribs</groupId>
    <artifactId>jersey-apache-client4</artifactId>
</dependency>

Spring Cloud Eureka Server
The Eureka server and client do not have a back end store, all the registrations is in memory.
spring-cloud-starter-netflix-eureka-server



Standalone Mode



Peer Awareness
Availability: Every Eureka server is also a Eureka client and requires (at least one) service URL to locate a peer.





Securing The Eureka Server



JDK 11 Support



Config Ref: https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/appendix.html


Q&A

How to do the rolling update with Eureka? (Avoid the cache issue)



The worst case the cache will be refresh in 90s (30 + 30 + 30).
Solution:
1.Call the Eureka Server API to override the client status to DOWN
PUT - http://localhost:8761/eureka/apps/EUREKA-CLIENT-PROVIDER/c1/status?value=OUT_OF_SERVICE
Note: Be aware that this requires each instance to have a unique instance ID!

2.Call the actuator API to pause the instance status to DOWN
(Actuator /pause /resume need spring cloud dependence. And need restart endpoint enable)
POST - http://localhost:7200/actuator/pause
POST - http://localhost:7200/actuator/shutdown

Flow:
1. De-register the first instance from the Eureka server.
2. Wait 90 seconds.
3. Shut down, deploy, and start the first instance.
4. Wait 90 seconds.
5. De-register the second instance from the Eureka server.
6. Wait 90 seconds.
7. Shut down, deploy, and start the second instance.
8. Traffic will begin flowing to the second instance within 90 seconds.

Diff with K8s:
Kubernetes 提供了部署策略，允许您根据系统的需要以多种方式进行更新。最常见的三种是：
* 滚动更新策略：以更新速度为代价最大限度地减少停机时间。
* 娱乐策略：导致停机但更新迅速。
* 金丝雀策略：为少数用户快速更新，稍后全面推出。

Eureka client unavailable, how to handle with no downtime?
Solution: 
1. Client side retry (Spring Cloud Ribbon retry), server side idempotent.
2. Reduce the heartbeat time period.
