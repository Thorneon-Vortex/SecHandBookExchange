## 本文用于MySQL到GAUSSDB的迁移
初始开发时使用mysql数据库，现在需要将数据库迁移到华为GaussDB。
需要注意保存建表语句和存储过程，触发器的**创建**语句

在 Spring Initializr 的标准依赖列表中，没有直接提供华为 GaussDB的选项。



### 添加官方GaussDB JDBC驱动

这是最正确、最稳妥的方法。虽然在 Initializr 界面上不能直接选，但我们可以在项目生成后手动添加和配置。

#### 步骤 1: 在 Spring Initializr 生成项目

先勾选 `MySQL Driver` 来让 Spring Boot 帮你把所有和 JDBC 相关的基础配置（如 `spring-boot-starter-jdbc`）都准备好。
*   勾选 `MyBatis Framework`
*   勾选 `MySQL Driver` (作为占位符)

然后下载生成的项目压缩包并解压。

#### 步骤 2: 修改 Maven/Gradle 依赖

打开项目，找到 `pom.xml` (如果是 Maven 项目) 或 `build.gradle` (如果是 Gradle 项目)。

1.  **移除** Spring Initializr 自动添加的 MySQL 驱动依赖。
    *   在 `pom.xml` 中，删除这部分：
        ```xml
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        ```

2.  **添加** 华为官方提供的 GaussDB JDBC 驱动依赖。
    *   你需要从华为云的官方文档或 Maven 仓库中找到最新的驱动坐标。通常是这样的格式：
    *   在 `pom.xml` 中，添加以下依赖（版本号请以官方最新为准）：
        ```xml
        <dependency>
            <groupId>com.huaweicloud.gaussdb</groupId>
            <artifactId>gaussdb-jdbc</artifactId>
            <version>5.0.0</version> <!-- 请检查并使用最新的稳定版本 -->
        </dependency>
        ```

#### 步骤 3: 修改配置文件

现在，打开 `src/main/resources/application.properties` (或 `.yml`) 文件，配置正确的 GaussDB 连接信息。

**`application.properties` 示例:**

```properties
# 使用 GaussDB 官方驱动进行连接
spring.datasource.url=jdbc:gaussdb://[your-gaussdb-host]:[port]/[database-name]?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=[your-gaussdb-username]
spring.datasource.password=[your-gaussdb-password]
# 明确指定使用 GaussDB 的驱动类
spring.datasource.driver-class-name=com.huawei.gauss.jdbc.ZenithDriver
```

**关键变化**:
*   **`spring.datasource.url`**: 前缀变成了 `jdbc:gaussdb://`。
*   **`spring.datasource.driver-class-name`**: 明确指定为 `com.huawei.gauss.jdbc.ZenithDriver`。

