package com.mindary.diary.services.impl;

import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.repositories.DiaryRepository;
import com.mindary.diary.services.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryServiceImpl implements DiaryService {
    private final DiaryRepository diaryRepository;

    @Override
    public DiaryEntity create(UUID userId, String diary) {
        DiaryEntity diaryEntity = DiaryEntity.builder()
                .userId(userId)
                .content(diary)
                .build();

        return diaryRepository.save(diaryEntity);
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
