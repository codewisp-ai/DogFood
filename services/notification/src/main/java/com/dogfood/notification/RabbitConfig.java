package com.dogfood.notification;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("dogfood.notifications");
    }
    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange("dogfood.audit");
    }
    @Bean
    public Queue notificationQueue() {
        return new Queue("notification.events");
    }
    @Bean
    public Binding binding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with("notification.*");
    }
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
