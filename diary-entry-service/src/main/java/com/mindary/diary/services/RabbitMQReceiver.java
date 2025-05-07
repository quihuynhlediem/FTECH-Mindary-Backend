package com.mindary.diary.services;

public interface RabbitMQReceiver {
    void receiver(String message);
}
