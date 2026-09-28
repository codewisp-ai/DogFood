package com.dogfood.certificate;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue certificateQueue() { return new Queue("certificate.generate"); }
    @Bean
    public TopicExchange auditExchange() { return new TopicExchange("dogfood.audit"); }
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
