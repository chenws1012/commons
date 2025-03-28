[中文](README_zh.md)
#### Commons

## Project Overview
This is a Java foundational library that provides commonly used tools and components. It will be continuously improved. This project is licensed under Apache 2.0, allowing free use and modification.

## Directory Structure
```dtd
commons/
│── common-sharding-dao/    # Database Sharding
│── common-ratelimiter/    
```


## Quick Start
### 1. Unique ID Generation Example
![img.png](img.png)

The Snowflake algorithm generates unique IDs, with `workid` automatically dispatched by Zookeeper to ensure uniqueness. The connection status of Zookeeper is monitored; if the Zookeeper connection is lost, the `workid` is re-registered.
#### Add Dependency:
```xml
<dependency>
    <groupId>com.github.shun</groupId>
    <artifactId>common-sharding-dao</artifactId>
    <version>{last-version}</version>
</dependency>
```

#### Configuration:
```yaml
zk:
  addresses: ${ZK_ADDRESSES:119.3.155.248:21811}
  sessionTimeoutMs: ${ZK_SESSIONTIMEOUTMS:60000}
  connectionTimeoutMs: ${ZK_CONNECTIONTIMEOUTMS:15000}
```

#### Add Annotation to the Startup Class `@EnableZkSnowFlake`
```java
@EnableZkSnowFlake
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### Usage:
```java
    @Autowired
    private SnowflakeKeyGenerator snowflakeKeyGenerator;

    // somewhere generate a unique ID
    Long anyId = snowflakeKeyGenerator.generateKey();
```

### 2. RateLimiter Example

---

### Explanation

1. **Project Description**:
    - The `commons` library is designed to provide reusable utilities and components for Java projects. It includes features like database sharding and unique ID generation using the Snowflake algorithm.

2. **Directory Structure**:
    - The `common-sharding-dao` directory contains the implementation for database sharding functionality.

3. **Quick Start Guide**:
    - **Dependency**: A Maven dependency is provided to include the library in your project.
    - **Configuration**: YAML configuration is required to set up Zookeeper addresses and timeouts.
    - **Annotation**: The `@EnableZkSnowFlake` annotation enables the Snowflake ID generation feature.
    - **Usage**: An example demonstrates how to autowire the `SnowflakeKeyGenerator` and generate a unique ID.

This documentation provides a concise overview of setting up and using the `commons` library for unique ID generation in a Java Spring Boot application.