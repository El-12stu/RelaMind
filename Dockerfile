# 多阶段构建：构建阶段
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app

# 复制 Maven 配置文件（利用 Docker 缓存层）
COPY pom.xml .
# 下载依赖（如果 pom.xml 没变，这层会被缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests -B

# 运行阶段：使用更小的 Alpine 镜像
FROM amazoncorretto:21-alpine
WORKDIR /app

# 安装 wget 用于健康检查（需要在创建用户之前，因为需要 root 权限）
RUN apk add --no-cache wget

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 从构建阶段复制 jar 文件
COPY --from=build /app/target/RelaMind-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8123

# 健康检查（检查应用是否响应）
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8123/api/swagger-ui.html || exit 1

# 启动应用
# 添加 JVM 参数以解决网络连接问题（强制使用 IPv4，避免 DNS 解析问题）
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-Djava.net.useSystemProxies=false", "-jar", "app.jar"]