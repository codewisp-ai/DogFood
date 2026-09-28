package com.dogfood.webhook;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public TopicExchange webhookExchange() { return new TopicExchange("dogfood.webhooks"); }
    @Bean
    public Queue deliveryQueue() { return new Queue("webhook.delivery"); }
    @Bean
    public Binding binding(Queue deliveryQueue, TopicExchange webhookExchange) {
        return BindingBuilder.bind(deliveryQueue).to(webhookExchange).with("webhook.*");
    }
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
