package com.task.service_state_poller.configuration;

import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories
class R2DBCConfiguration extends AbstractR2dbcConfiguration {
    @Value("${DB.host}")
    private String host;

    @Value("${DB.port}")
    private int port;

    @Value("${DB.database}")
    private String database;

    @Value("${DB.username}")
    private String username;

    @Value("${DB.password}")
    private String password;


    @Bean
    public MySqlConnectionFactory connectionFactory() {
        MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
                .host(host)
                .port(port)
                .database(database)
                .username("dev")
                .password("secret").build();
        return MySqlConnectionFactory.from(configuration);
    }

}