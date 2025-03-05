package com.mindary.diary.services.impl;

import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.services.RabbitMQSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQSenderImpl implements RabbitMQSender {
    @Value(value = "${rabbitmq.exchange.name}")
    private String exchange;

    @Value(value = "${rabbitmq.routing_key.analysis.name}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendDiary(DiaryEntity diary) {
        rabbitTemplate.convertAndSend(exchange, routingKey, diary);
    }
}
