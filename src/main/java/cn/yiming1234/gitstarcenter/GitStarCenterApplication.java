package cn.yiming1234.gitstarcenter;

import cn.yiming1234.gitstarcenter.properties.AppProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@MapperScan("cn.yiming1234.gitstarcenter.mapper")
public class GitStarCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitStarCenterApplication.class, args);
    }
}
