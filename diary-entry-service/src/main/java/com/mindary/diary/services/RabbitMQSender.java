package com.mindary.diary.services;

import com.mindary.diary.models.DiaryEntity;

public interface RabbitMQSender {
    void sendDiary(DiaryEntity diary);
}
