package ge.epam.aws.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

//    @Bean
//    @Primary
    // Use when need IAM RDS Authentication
    public HikariDataSource dataSource(DataSourceProperties properties) {
        RDSIAMDataSource dataSource = new RDSIAMDataSource();

        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());

        return dataSource;
    }
}
