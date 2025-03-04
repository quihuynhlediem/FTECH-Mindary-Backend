package com.mindary.diary.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value(value = "${rabbitmq.queue.analysis.name:application.properties}")
    private String diaryAnalysisQueue;

    @Value(value = "${rabbitmq.queue.analysis_result.name:application.properties}")
    private String diaryAnalysisResultQueue;

    @Value(value = "${rabbitmq.exchange.name:application.properties}")
    private String topicExchange;

    @Value(value = "${rabbitmq.routing_key.analysis.name}")
    private String diaryAnalysisRoutingKey;

    @Value(value = "${rabbitmq.routing_key.analysis_result.name:application.properties}")
    private String diaryAnalysisResultRoutingKey;

    @Bean
    public Queue diaryAnalysisQueue() {
        return new Queue(diaryAnalysisQueue);
    }

    @Bean Queue diaryAnalysisResultQueue() {
        return new Queue(diaryAnalysisResultQueue);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(topicExchange);
    }

    @Bean
    public Binding diaryAnalysisBinding() {
        return BindingBuilder
                .bind(diaryAnalysisQueue())
                .to(exchange())
                .with(diaryAnalysisRoutingKey);
    }

    @Bean
    public Binding diaryAnalysisResultBinding() {
        return BindingBuilder
                .bind(diaryAnalysisResultQueue())
                .to(exchange())
                .with(diaryAnalysisResultRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
