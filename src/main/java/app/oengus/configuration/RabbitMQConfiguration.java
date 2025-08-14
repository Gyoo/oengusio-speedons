package app.oengus.configuration;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class RabbitMQConfiguration {

    private final String uri;
    private final String username;
    private final String password;
    private final String virtualHost;

    public RabbitMQConfiguration(
        @Value("${rabbitmq.uri}") String uri,
        @Value("${rabbitmq.username}") String username,
        @Value("${rabbitmq.password}") String password,
        @Value("${rabbitmq.virtual-host}") String virtualHost
    ) {
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.virtualHost = virtualHost;
    }

    @Bean
    public ConnectionFactory rabbitMqConnectionFactory() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        final ConnectionFactory factory = new ConnectionFactory();

        factory.setUri(this.uri);
        factory.setUsername(this.username);
        factory.setPassword(this.password);
        factory.setVirtualHost(this.virtualHost);

        return factory;
    }
}
