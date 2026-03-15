package io.agentlens.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AgentLens Collector 应用入口。
 * <p>
 * 接收来自各 SDK 的 Trace/Span 数据，持久化到数据库（SQLite/PostgreSQL），
 * 并通过 REST API 供 Dashboard 查询与展示。
 * </p>
 */
@SpringBootApplication
@EnableScheduling
public class CollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }
}
