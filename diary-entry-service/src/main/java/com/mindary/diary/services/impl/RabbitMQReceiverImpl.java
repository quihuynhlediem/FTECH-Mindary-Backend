package com.mindary.diary.services.impl;

import com.mindary.diary.services.RabbitMQReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${rabbitmq.queue.analysis_result.name:application.properties}")
@Slf4j
public class RabbitMQReceiverImpl implements RabbitMQReceiver {
    @Override
    public void receiver(String message) {
        log.info(message);
    }
}
