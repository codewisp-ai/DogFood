package com.dogfood.judging.config;

import com.dogfood.common.config.CommonRabbitConfig;
import com.dogfood.common.events.RabbitConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * RabbitMQ configuration for the judging service.
 * Declares the queue and binding to consume score.submitted events.
 */
@Configuration
@Import(CommonRabbitConfig.class)
public class RabbitConfig {

    /**
     * Queue that receives score.submitted events to trigger normalization recompute.
     */
    @Bean
    public Queue scoreNormalizationQueue() {
        return QueueBuilder
                .durable(RabbitConstants.SCORE_NORMALIZATION_QUEUE)
                .build();
    }

    /**
     * Binds the normalization queue to the scores exchange with score.submitted routing key.
     */
    @Bean
    public Binding scoreNormalizationBinding() {
        return BindingBuilder
                .bind(scoreNormalizationQueue())
                .to(scoresExchange())
                .with(RabbitConstants.SCORE_SUBMITTED);
    }

    /**
     * Import the scores exchange from common config.
     */
    @Bean
    public TopicExchange scoresExchange() {
        return new TopicExchange(RabbitConstants.SCORES_EXCHANGE, true, false);
    }
}