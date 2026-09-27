package com.dogfood.common.config;

import com.dogfood.common.events.RabbitConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared RabbitMQ configuration declaring all exchanges.
 * Each service imports this config to ensure exchanges exist before publishing.
 * Queue declarations are done by the consuming service only.
 */
@Configuration
public class CommonRabbitConfig {

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(RabbitConstants.AUDIT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange scoresExchange() {
        return new TopicExchange(RabbitConstants.SCORES_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange webhooksExchange() {
        return new TopicExchange(RabbitConstants.WEBHOOKS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(RabbitConstants.NOTIFICATIONS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange certificatesExchange() {
        return new TopicExchange(RabbitConstants.CERTIFICATES_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
