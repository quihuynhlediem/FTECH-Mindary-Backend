package com.mindary.diary.services.impl;

import com.mindary.diary.dto.AnalysisResultDto;
import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.repositories.DiaryRepository;
import com.mindary.diary.services.DiaryService;
import com.mindary.diary.services.RabbitMQSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryServiceImpl implements DiaryService {
    private final DiaryRepository diaryRepository;
    private final Map<UUID, CompletableFuture<AnalysisResultDto>> pendingAnalysis = new ConcurrentHashMap<>();
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQSender rabbitMQSender;

    @Value(value = "${rabbitmq.queue.analysis_result.name:application.properties}")
    private String analysisResultQueue;

    @Value(value = "${rabbitmq.queue.analysis.name:application.properties}")
    private String analysisQueue;

    @Override
    public DiaryEntity create(UUID userId, String diary) {
        DiaryEntity diaryEntity = DiaryEntity.builder()
                .userId(userId)
                .content(diary)
                .build();

        return diaryRepository.save(diaryEntity);
    }

    @Override
    public AnalysisResultDto analyze (DiaryEntity savedDiary) {
        CompletableFuture<AnalysisResultDto> future = new CompletableFuture<>();
        pendingAnalysis.put(savedDiary.getId(), future);
        rabbitMQSender.sendDiary(savedDiary);

        return waitForAnalysisResult(savedDiary.getId());
    }

    private AnalysisResultDto waitForAnalysisResult(UUID diaryEntryId) {
        try {
            return pendingAnalysis.get(diaryEntryId).get();
        } catch (Exception e) {
            log.error("Error waiting for analysis result for diary entry ID: {}", diaryEntryId, e);
            throw new RuntimeException("Error waiting for analysis result", e);
        } finally {
            pendingAnalysis.remove(diaryEntryId);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.analysis_result.name:application.properties}")
    public void receiveAnalysisResult(AnalysisResultDto analysisResult) {
        if (analysisResult == null) {
            log.warn("Received null analysis result.");
            return;
        }

        log.info(analysisResult.toString());
        log.info(analysisResult.getDiaryId().toString());

        CompletableFuture<AnalysisResultDto> future = pendingAnalysis.get(analysisResult.getDiaryId());

        if (future != null) {
            future.complete(analysisResult);
        } else {
            log.warn("No pending analysis found for diary ID: {}", analysisResult.getDiaryId());
        }
    }

    @Override
    public DiaryEntity save(DiaryEntity diary) {
        return diaryRepository.save(diary);
    }

    @Override
    public boolean isExist(UUID diaryId) {
        return diaryRepository.existsById(diaryId);
    }

    @Override
    public Optional<DiaryEntity> findOne(UUID diaryId) {
        return diaryRepository.findById(diaryId);
    }

    @Override
    public Page<DiaryEntity> findByUserId(UUID userId, Pageable pageable) {
        return diaryRepository.findByUserId(userId, pageable);
    }

    @Override
    public Optional<DiaryEntity> findByUserIdAndDate(UUID userId, String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        LocalDate currentDate = LocalDate.now(zone);
        return diaryRepository.findByUserIdAndCreatedAtBetween(userId, currentDate.atStartOfDay(), currentDate.plusDays(1).atStartOfDay().minusNanos(1));
    }

    @Override
    public Optional<DiaryEntity> findByUserIdAndDate(UUID userId, LocalDate targetDate) {
        return diaryRepository.findByUserIdAndCreatedAtBetween(
                userId,
                targetDate.atStartOfDay(),
                targetDate.plusDays(1).atStartOfDay().minusNanos(1)
        );
    }

    @Override
    public DiaryEntity partialUpdate(UUID diaryId,DiaryEntity diaryEntity) {
        return diaryRepository.findById(diaryId).map(existingDiary -> {
            Optional.ofNullable(existingDiary.getContent()).ifPresent(existingDiary::setContent);
            return diaryRepository.save(existingDiary);
        }).orElseThrow(() -> new RuntimeException("Diary does not exist"));
    }
}
