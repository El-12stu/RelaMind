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

# 运行阶段：使用更小的 JRE 镜像
FROM amazoncorretto:21-alpine-jre
WORKDIR /app

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
ENTRYPOINT ["java", "-jar", "app.jar"]