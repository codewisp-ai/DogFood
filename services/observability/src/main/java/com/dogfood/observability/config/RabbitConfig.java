package com.dogfood.observability.config;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange("dogfood.audit");
    }
    @Bean
    public Queue auditQueue() {
        return new Queue("audit.queue", true);
    }
    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(auditQueue).to(auditExchange).with("#");
    }
}
