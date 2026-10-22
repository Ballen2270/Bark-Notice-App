package com.bark.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Flyway 配置
 */
@Configuration
public class FlywayConfig {

    @Autowired
    private Environment environment;

    /**
     * Flyway 配置
     */
    @Bean
    public Flyway flyway(DataSource dataSource) {
        // 优先从环境变量获取locations，如果没配置则使用默认的mysql
        String locations = environment.getProperty("flyway.locations", "classpath:db/migration/mysql");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(true)
                .validateOnMigrate(false)
                .cleanDisabled(true)
                .load();

        System.out.println("✓ 启动 Flyway 迁移 (脚本位置: " + locations + ")");
        flyway.migrate();

        return flyway;
    }
}
