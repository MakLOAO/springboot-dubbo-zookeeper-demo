一个简单的Spring Boot整合Dubbo Zookeeper

环境：VMware CentOS7 docker

使用时搭好zookeeper环境，修改zookeeper地址即可

安装zookeeper： `docker pull zookeeper`
运行： `docker run --name zk01 -p 2181:2181 --restart always -d zookeeper`

首先创建一个空项目： `File -> New -> Project -> Empty Project`
然后分别创建2个module，一个消费者，一个提供者
`File -> New -> Module -> Spring Initializr`
都只选择 `Spring Web` 包

![](https://i.imgur.com/umDI3Ll.png)

![](https://i.imgur.com/CyRs61E.png)

创建后创建两个Service，一个提供服务，一个消费服务，目录树如下：

![](https://i.imgur.com/edwNuEN.png)

```xml
<!-- 2020/2/13 Apache官方的稳定的最新版，消费者和生产者都要导入 -->
<!-- Dubbo Spring Boot Starter -->
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
    <version>2.7.4.1</version>
</dependency>

<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-dependencies-zookeeper</artifactId>
    <version>2.7.4.1</version>
    <type>pom</type>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

编写生产者：

```xml
dubbo.application.name=provider-ticket
dubbo.scan.base-packages=com.mkl.ticket.service
dubbo.registry.address=zookeeper://192.168.186.128:2181

# 这两个参数之前版本出现过timeout，加了才成功，不过现在使用Apache的2.7.4.1的版本，去掉也可以正常使用
dubbo.config-center.timeout=50000
dubbo.registry.timeout=40000

server.port=8081
```

```java
package com.mkl.providerticket.service;
public interface TicketService {

    public String getTicket();
}
```

```java
package com.mkl.providerticket.service;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

// 注意是dubbo包的service，它将服务发布出去
@Component
@Service(version = "1.2.3")
public class TicketServiceImpl implements TicketService {

    @Override
    public String getTicket() {
        return "ready to get a ticket...";
    }
}
```

```java
// 需要添加 @EnableDubbo
@SpringBootApplication
@EnableDubbo
public class ProviderTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderTicketApplication.class, args);
    }

}
```

启动项目，去注册中心可以看到有生产者，命令：

```
docker exec -it zk01 bash
/apache-zookeeper-3.5.6-bin/bin/zkCli.sh
ls /dubbo
就可以查看到生产者提供的服务
ls /dubbo/com.mkl.providerticket.service.TicketService
可以看他是否有消费者和查看生产者
ls /dubbo/com.mkl.providerticket.service.TicketService/providers
ls /dubbo/com.mkl.providerticket.service.TicketService/consumers
分别查看生产者和消费者
```

dubbo-admin也可以，但使用略

消费者编写如下：

```xml
dubbo.application.name=consumer-user

dubbo.registry.address=zookeeper://192.168.186.128:2181

dubbo.config-center.timeout=50000
dubbo.registry.timeout=40000
```

```java
package com.mkl.consumeruser.service;

import com.mkl.providerticket.service.TicketService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Reference(version = "1.2.3")
    TicketService ticketService;

    public void hello() {
        System.out.println(ticketService.getTicket());
        System.out.println("already got the ticket!!!");
    }
}
```

主程序可以不用添加 `@EnableDubbo`

```java
@SpringBootTest
class ConsumerUserApplicationTests {

    @Autowired
    UserService userService;

    @Test
    void contextLoads() {
        userService.hello();
    }
}
```