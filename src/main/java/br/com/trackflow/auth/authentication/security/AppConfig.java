package br.com.trackflow.auth.authentication.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:.env", ignoreResourceNotFound = false)
public class AppConfig {

    @Value("${DB_URL}")
    private String databaseUrl;

    @Value("${DB_USERNAME}")
    private String databaseUser;

    @Value("${DB_PASSWORD}")
    private String databasePassword;

    @Value("${DB_DRIVER}")
    private String databaseDriver;

    @Value("${SERVER_PORT:8080}")
    private String serverPort;

    @Value("${JWT_SECRET}")
    private String jwtSecretKey;

    @Value("${JWT_EXPIRATION:3600}")
    private Long jwtAccessTokenTime;

    @Value("${JWT_REFRESH_TOKEN_TIME:604800}")
    private Long jwtRefreshTokenTime;

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public String getServerPort() {
        return serverPort;
    }

    public String getJwtSecretKey() {
        return jwtSecretKey;
    }

    public Long getJwtAccessTokenTime() {
        return jwtAccessTokenTime;
    }

    public Long getJwtRefreshTokenTime() {
        return jwtRefreshTokenTime;
    }
}
